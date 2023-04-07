#![allow(non_snake_case)]

pub mod utils;

use utils::{abs_path_clean, init_log4rs};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::error::Error;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{env, fs, io};

use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{prelude::kernel_Hprocess, SysResult, HPROCESS, STARTUPINFO};

use log::{error, warn};

/// # Returns
/// `io::Error::new(Other, message)`. i.e. a custom `io::Error`
fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
}

const RETRY_AMOUNT: u8 = 5;

/// # Description
/// Creates a process using winsafe api. (safe wrapper around windows sdk api).
/// On Windows, Creating a new process always comes with the execution of some executable in a new
/// thread / in that child process (there is no "just" fork.)
/// That's why this function take an absolute_path to an executable
/// and the the arguments to pass to it (argv)
///
/// # Params
/// - `app_name`: Absolute path to an executable
/// - `command_line`: Argument to program (equivalent of `argv`)
///
/// # NB
/// This functions returns after having waited on the "child" process.
/// (Although the wait is not mandatory to avoid zombies thanks to the winsafe api,
/// here we just want to wait for the completion of the job.)
pub fn execvp(app_name: &str, command_line: &str) -> io::Result<()> {
    try_execvp(app_name, command_line, 0)
}

fn try_execvp(app_name: &str, command_line: &str, retry: u8) -> io::Result<()> {
    let mut si: STARTUPINFO = STARTUPINFO::default();
    let app_name_opt = Some(app_name);
    let command_line: &str = &format!("{app_name} {command_line}"); //append name of program
    let cmd_line_opt = Some(command_line);
    // first word before space in command line should be app_name
    // (I think its ignored either way if app_name is not None because its argv[0])
    let close_handle_res: SysResult<CloseHandlePiGuard> = HPROCESS::CreateProcess(
        app_name_opt,
        cmd_line_opt,
        None,
        None,
        true,
        CREATE::NO_WINDOW | CREATE::INHERIT_PARENT_AFFINITY,
        None, //inherits
        None, // inherits
        &mut si,
    );

    //TODO: CHECK IF ERROR! MACRO IS THREAD SAFE 
    if close_handle_res.is_err() {
        if retry < RETRY_AMOUNT {
            return try_execvp(app_name, command_line, retry + 1);
        } else {
            let err: winsafe::co::ERROR = close_handle_res.map(|_| ()).unwrap_err(); // discards results to match io::Result<()> type
            error!(
                "Windows Error: could not start process {app_name} {command_line}\n\t{}",
                err
            );
            return Err(custom_io_err(&format!(
                "WinErr: could not start process {app_name} {command_line}\n\t{}",
                err 
            )));
        }
    }
    let close_handle = close_handle_res.unwrap();
    let wait_res = HPROCESS::WaitForSingleObject(&close_handle.hProcess, Some(10_000)); // waits 10 sec at most
    // its ok to wait this long because the calls are often made in parallel so they're not actually blocking each other
    if wait_res.is_err() {
        warn!("Could not wait on child process: {app_name} {command_line}\n\t{}", wait_res.unwrap_err());
    }
    Ok(())
}

/* fn unwrap_or_log<T, E>(fun_res: Result<T, E>, msg: &str){

} */

#[macro_export]
/// If given `Result<_,_>` is an error. (`is_err() == true`) 
/// `return` that error in the function where this macro is called, 
/// otherwise do nothing.  
/// Used to ensure that a call to `unwrap()` will never `panic`.
macro_rules! unwrap_or_log{
    ( $fun_res:expr $(, $msg:expr) ? ) => { 
        if $fun_res.is_err() {
            let err = $fun_res.unwrap_err();
            $( error!("{} {:#?}", $msg, err); )?

            return Err(err);
        }
    }
}

fn test_macro() -> io::Result<()> {
    let x: io::Result<()> = Err(custom_io_err("test"));
    // return Err(x);
    // unwrap_or_log!(x, "lul");
    unwrap_or_log!(x);
    Ok(())
}

/// # Description
///
/// Return a (`Result` of) 4-tuple containing the paths to the executables
/// of pandoc and wkhtmltopdf, and the paths to the markdown
/// and templates resource directory ('res/md' and 'res/templates')
///
/// # Returns
///
/// `Result<(pandoc_path, wkhtmltopdf_path, md_path, templates_path), io::Error>`
fn get_resources_path() -> Result<(String, String, String, String), std::io::Error> {
    let rust_exe_path = env::current_exe();
    if rust_exe_path.is_err() {
        let err = rust_exe_path.unwrap_err();
        error!("could not get_resources_path {:#?}", err);
        return Err(err);
    }

    let mut rust_exe_path: PathBuf = rust_exe_path.unwrap();

    rust_exe_path.pop(); // /res/bin-converters
    let exes_path: String = abs_path_clean(rust_exe_path.clone());

    rust_exe_path.pop(); // /res
    let res_path: PathBuf = rust_exe_path;
    let res_path: String = abs_path_clean(&res_path);
    Ok((
        exes_path.to_owned() + "\\pandoc.exe",
        exes_path + "\\wkhtmltopdf.exe",
        res_path.to_owned() + "\\md",
        res_path + "\\templates",
    ))
}

/// # Description
/// Calls pandoc cmd with `execvp` to convert the given markdown file according
/// to the predefined html template.
///
/// # Params
/// - `md_filename`: filename of a markdown document in `/res/md/` directory.
/// i.e. `desc-2022-11X001.md` for `/res/md/desc-2022-11X001.md`
/// - `pandoc_path`: Absolute path to the pandoc executable.
/// - `md_path`: Absolute path to the `/res/md` directory.
/// - `templates_path`: Absolute path to the `/res/templates` directory.
///
/// # NB
/// The output file is saved in `/res/templates/<md_filename>.html` (without the '.md' extension)
//
fn pandoc_fill_template(md_filename: &String, pandoc_path: &str, md_path: &str, templates_path: &str) -> Result<PathBuf, String> {
    let template: String = templates_path.to_owned() + "\\desc-template.html";

    let md_filepath: &String = &format!("{md_path}\\{md_filename}");
    let out_html = templates_path.to_owned() + "\\" + &md_filename.replace(".md", ".html");
    let out_html_path = Path::new(&out_html);
    if out_html_path.exists() {
        fs::remove_file(out_html_path).unwrap_or_default();
    }

    let cmd_line: &str = &format!("{md_filepath} -t html --template={template} -o {out_html}");
    execvp(pandoc_path, cmd_line);

    let out_html: &Path = Path::new(&out_html);

    if out_html.exists() {
        Ok(out_html.to_path_buf())
    } else {
        Err(format!(
            "pandoc_fill_template: Could not generate html file for {md_path}\\{md_filename}"
        ))
    }
}

/// # Description
/// Converts html generated by pandoc to pdf
///
/// # Params
/// - `wk_path`: Absolute path to the 'wkhtmltopdf' executable.
///
/// # Returns
/// Path of the generated pdf (usually calling dir i.e. `env::current_dir()`)
fn wkhtmltopdf(out_html: &Path, wk_path: &str) -> Result<PathBuf, String> {
    let mut out_pdf = env::current_dir().expect("wkhtmltopdf: could not get current_dir");
    let new_name: &str = &out_html
        .file_name()
        .unwrap()
        .to_str()
        .unwrap()
        .replace(".html", ".pdf");

    out_pdf.push(new_name);

    let out_pdf_s: &str = &out_pdf.to_str().unwrap();
    let cmd_line: &str = &format!(
        "--enable-local-file-access -T 2 -B 0 -L 3 -R 0 {} {}",
        out_html.to_str().unwrap(),
        out_pdf_s
    );

    execvp(wk_path, cmd_line);

    let out_pdf = out_pdf; // removes mut? i.e. makes out_pdf immutable ?
    if out_pdf.exists() {
        Ok(out_pdf)
    } else {
        Err(format!("Could not convert html file to pdf, for {:#?}", out_html).to_string())
    }
}


/// # Description
/// Calls 'pandoc' cmd with `execvp` to convert the given markdown file according
/// to the predefined html template.
/// Then do the same with 'wkhtmltopdf' to convert the html into a pdf
///
/// # Params
/// - `md_filename`: filename of a markdown document in `/res/md/` directory.
/// i.e. `desc-2022-11X001.md` for `/res/md/desc-2022-11X001.md`
/// - `pandoc_path`: Absolute path to the pandoc executable.
/// - `wk_path`: Absolute path to the 'wkhtmltopdf' executable.
/// - `md_path`: Absolute path to the `/res/md` directory.
/// - `templates_path`: Absolute path to the `/res/templates` directory.
///
/// # Returns
/// Path of the generated pdf (usually  `<calling_directory/markdown_filename.pdf>` where `calling_directory` is `env::current_dir()`)
pub fn fill_template_convert_pdf(md_filename: &String, pandoc_path: &str, wk_path: &str, md_path: &str, templates_path: &str) 
-> Result<PathBuf, String> {
    let out_html = pandoc_fill_template(md_filename, pandoc_path, md_path, templates_path);
    if out_html.is_err() {
        return Err(out_html.unwrap_err().to_string());
    }
    let tmp = &out_html.unwrap();
    let out_html: &Path = Path::new(tmp);
    wkhtmltopdf(out_html, &wk_path)
}


/// # Description
/// Creates a pdf for each markdown course description document in "/res/md". (in parallel)
/// i.e. calls `fill_template_convert_pdf` in a `par_iter().for_each()` for each file in the directory  
///
/// --------  
/// # Returns
/// - Ok(()) if no error happened.
/// - Err(_) where _ is an `std::io::Error<ErrorKind, String>`. Its actual content is a `String`.
/// I.e. error message which is a concatenation of each error message returned by
/// the parallel calls to `fill_template_convert_pdf`.
/// i.e. if `err_messages` is a vector of each message (string) then error will be :
/// ```Rust
///     let err_messages:Vec<String> = md_files.par_iter(). [...] .filter_map(|x| x.err()).collect();
///     
///     return Err(std::io::Error::new(io::ErrorKind::Other, err_messages.join("\n")));
///
/// x.err() discards the value T from a Result<T, E> and extracts the E (type of error, here string).
/// ```
pub fn ftcp_parallel(pandoc_path: &str, wk_path: &str, md_path: &str, templates_path: &str) -> io::Result<()> {
    let dir_ent_it: io::Result<ReadDir> = fs::read_dir(md_path); // iterator over DirEntry
    if dir_ent_it.is_err() {
        return dir_ent_it.map(|_| ());
    }
    let mdfiles_vec: Vec<DirEntry> = dir_ent_it
        .map(|read_dir| {
            read_dir
                .filter_map(Result::ok)
                .filter(|dir_ent| match dir_ent.file_name().to_str() {
                    Some(name) => name.ends_with(".md"),
                    None => false,
                })
                .collect() // ignore dir entry error and collect only Ok ones that refer to a markdown file in vector
        })
        .unwrap();

    let err_messages: Vec<String> = mdfiles_vec
        .par_iter()
        .map_with(
            (pandoc_path, wk_path, md_path, templates_path),
            |q, md_file| {
                // we can directly unwrap since the path on wich to_str() would return None have been filtered
                let name = md_file.file_name().to_str().unwrap().to_owned();
                fill_template_convert_pdf(&name, q.0, q.1, q.2, q.3)
            },
        )
        .filter_map(|x| x.err())
        .collect();

    match &err_messages.len() {
        0 => Ok(()),
        _ => Err(io::Error::new(Other, err_messages.join("\n"))),
    }
}

fn _main() -> io::Result<()> {
    init_log4rs(None);
    let mut r = 0;
    let mut tmp = get_resources_path();
    
    while tmp.is_err() && r < RETRY_AMOUNT {
        tmp = get_resources_path();
        r += 1;
    }
    if r >= RETRY_AMOUNT {
        return Err(tmp.unwrap_err());
    }

    let (pandoc_path, wk_path, md_path, templates_path) = tmp.unwrap(); 
    let out: Result<(), io::Error> =
        ftcp_parallel(&pandoc_path, &wk_path, &md_path, &templates_path);
    if out.is_err() {
        error!("{}", &out.unwrap_err().to_string());
    }
    Ok(())
}

//pub mod test;

pub fn main() -> io::Result<()> {
    println!("\n\n");

    // use test::test_winsafe_error_description;
    // test_winsafe_error_description();
    test_macro()
    // Ok(())
}
