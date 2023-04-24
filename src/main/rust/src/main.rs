#![allow(non_snake_case)]
#![allow(unused)]

pub mod utils;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s};

use io::ErrorKind::Other;
use rayon::iter::*;
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
/// Formats error, with line and file in msg
macro_rules! fr {
    ($msg: expr) => {
        format!("{}.\t Line {}, File '{}'.\n", $msg, line!(), file!())
    };
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
fn execvp(app_name: &str, command_line: &str) -> io::Result<()> {
    let mut si: STARTUPINFO = STARTUPINFO::default();
    let (app_name, command_line) = (app_name.trim(), command_line.trim());

    // NOTE: If command has no arguments (i.e. `command_line == ""`) then
    // command_line_opt should be Some(app_name) and
    // app_name_opt should be none (because in reality `command_line` is argv, )
    let app_name_opt = match command_line {
        "" => None,
        _ => Some(app_name),
    };
    let command_line: &str = &format!("{app_name} {command_line}"); //append name of program
    let cmd_line_opt = Some(command_line);
    // first word before space in command line should be app_name
    // (it is ignored either way if app_name is not None because its argv[0])

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
    if close_handle_res.is_err() {
        return Err(custom_io_err(&format!(
            "WinErr: could not start process '{app_name} {command_line}', {}.   Line {}, File '{}'",
            close_handle_res.map(|_| ()).unwrap_err(),
            line!(),
            file!()
        )));
    }

    let close_handle = close_handle_res.unwrap();
    let wait_res = HPROCESS::WaitForSingleObject(&close_handle.hProcess, Some(10_000)); // waits 10 sec at most
                                                                                        // its ok to wait this long because the calls are often made in parallel so they're not actually blocking each other
    if wait_res.is_err() {
        warn!(
            "Could not wait on child process: {app_name} {command_line}\n\t{}",
            wait_res.unwrap_err()
        );
    }
    Ok(())
}

#[macro_export]
/// If given `Result<_,_>` is an error. (`is_err() == true`)
/// `return` that error in the function where this macro is called,
/// otherwise do nothing.  
/// Used to ensure that a call to `unwrap()` will never `panic`.
macro_rules! unwrap_or_log{
    ( $fun_res:expr  $(, $msg:expr) ? ) => {
        if $fun_res.is_err() {
            let err = $fun_res.unwrap_err();

            error!("{}\n\t{:?}{}.", $( $msg.to_owned() + )? "", err, fr!(""));
            return Err(err);
        }
    }
}

#[macro_export]
/// Does the same as `unwrap_or_log`
/// but instead retries `RETRY_AMOUNT` times
/// before returning an error and logging it
/// # Params
/// - `$fun_res`: a `Result<_, _>` which is the return value of calling `$fun`
/// - `$fun`: the function that returned `$fun_res`
/// - `$msg`: (optional) message to give to the logger if `$fun_res`.`is_err()`. `Must be wrapped in a block!` i.e. ` { "..." } `
/// - `args`: (optionnal only if function doesn't require arguments) arguments of the function separated by a comma
macro_rules! unwrap_retry_or_log {
    ( $fun_res:expr, $fun: ident, $msg:expr  $(, $args:expr)* ) => {
        {
            let mut r = 1;
            let mut x =  $fun( $($args),* );
            while x.is_err() && r < RETRY_AMOUNT {
                let x = $fun( $($args),* );
                r += 1;
            }
            if r >= RETRY_AMOUNT {
                let err = x.unwrap_err();
                error!("{}\n\t{:?}{}.",$msg, err, fr!(""));
                return Err(err);
            }
            x
        }
    }
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
    // FIX simulating relative path where the executable will be :
    // let rust_exe_path = Ok(PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\Course-Description-Automation\res\bin-converters\rust_para_convert-mdToPdf.exe"));

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
fn pandoc_fill_template(
    md_filename: &String,
    pandoc_path: &str,
    md_path: &str,
    templates_path: &str,
) -> io::Result<PathBuf> {
    let template: String = templates_path.to_owned() + "\\template.html";

    let md_filepath: &String = &format!("{md_path}\\{md_filename}");
    let out_html = templates_path.to_owned() + "\\" + &md_filename.replace(".md", ".html");
    let out_html_path = Path::new(&out_html);
    if out_html_path.exists() {
        fs::remove_file(out_html_path).unwrap_or_default();
    }
    let cmd_line: &str = &format!("{md_filepath} -t html --template={template} -o {out_html}");

    let exec_res = execvp(pandoc_path, cmd_line);
    let exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!("", execvp, "execvp", pandoc_path, cmd_line)
    } else {
        exec_res
    };

    let out_html: &Path = Path::new(&out_html);

    if out_html.exists() {
        Ok(out_html.to_path_buf())
    } else {
        let msg = &format!(
            "pandoc_fill_template: Could not generate html file {md_path}\\{md_filename} \n
            ||  template: {templates_path}    ||  md_path: {md_path}       ||   templates_path: {templates_path}        
            ||  pandoc_path: {pandoc_path}"
        );
        Err(custom_io_err(msg))
    }
}

/// # Description
/// Converts html generated by pandoc to pdf
///
/// # Params
/// - `wk_path`: Absolute path to the 'wkhtmltopdf' executable.
///
/// # Returns
/// Path of the generated pdf (usually dir of executable i.e. `env::current_exe()`)
fn wkhtmltopdf(out_html: &Path, wk_path: &str) -> io::Result<PathBuf> {
    let mut out_pdf: PathBuf = env::current_exe().expect("wkhtmltopdf: could not get current_dir");
    // FIX simulating relative path where the executable will be :
    // let mut out_pdf: PathBuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\Course-Description-Automation\res\bin-converters\rust_para_convert-mdToPdf.exe");
    out_pdf = pop_n_push_s(&mut out_pdf, 2, &["pdf"]);

    let new_name: &str = &out_html
        .file_name()
        .unwrap()
        .to_str()
        .unwrap()
        .replace(".html", ".pdf");
    out_pdf.push(new_name);

    let cmd_line: &str = &format!(
        "--enable-local-file-access -T 2 -B 0 -L 3 -R 0 {} {}",
        out_html.to_str().unwrap(),
        &out_pdf.to_str().unwrap()
    );

    let exec_res = execvp(wk_path, cmd_line);
    let exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!(exec_res, execvp, "execvp(wkhtml)", wk_path, cmd_line)
    } else {
        exec_res
    };

    let out_pdf = out_pdf; // removes mut? i.e. makes out_pdf immutable ?
    if out_pdf.exists() {
        Ok(out_pdf)
    } else {
        let msg = &format!(
            "Could not convert html file to pdf: {} {}",
            wk_path, cmd_line
        )
        .to_string();
        Err(custom_io_err(msg))
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
pub fn fill_template_convert_pdf(
    md_filename: &String,
    pandoc_path: &str,
    wk_path: &str,
    md_path: &str,
    templates_path: &str,
) -> io::Result<PathBuf> {
    let out_html = pandoc_fill_template(md_filename, pandoc_path, md_path, templates_path);
    let out_html = if (out_html.is_err()) {
        let msg = format!(
            "pandoc_fill_template: pandoc_path = {:?},  md_filename ={:?},  md_path={:?}",
            pandoc_path, md_filename, md_path
        );

        unwrap_retry_or_log!(
            out_html,
            pandoc_fill_template,
            { msg },
            md_filename,
            pandoc_path,
            md_path,
            templates_path
        )
    } else {
        out_html
    };

    let tmp = &out_html.unwrap();
    let out_html: &Path = Path::new(tmp);

    let exec_res = wkhtmltopdf(out_html, wk_path);

    if exec_res.is_err() {
        let msg = format!(
            "wkhtmltopdf: wk_path = {:?},  out_hmtl ={:?}",
            wk_path, out_html
        );
        unwrap_retry_or_log!(exec_res, wkhtmltopdf, { msg }, out_html, wk_path)
    } else {
        exec_res
    }
}

/// # Description
/// Calls 'pandoc' cmd with `execvp` that will convert the given markdown file according
/// to the predefined html template and then convert it to a pdf with 'wkhtmltopdf' as a pdf-engine.
///
/// Do so in one command 
///
/// # Params
/// - `md_filename`: filename of a markdown document in `/res/md/` directory.
/// i.e. `desc-2022-11X001.md` for `/res/md/desc-2022-11X001.md`
/// - `pandoc_path`: Absolute path to the pandoc executable.
/// - `md_path`: Absolute path to the `/res/md` directory.
/// - `templates_path`: Absolute path to the `/res/templates` directory.
///
/// # Returns
/// Path of the generated pdf (usually  `res/pdf/<markdown_filename.pdf>`.
fn pandoc_md_to_pdf(
    md_filename: &String,
    pandoc_path: &str,
    md_path: &str,
    templates_path: &str,
) -> io::Result<PathBuf> {
    let tmpl_path = PathBuf::from(templates_path.to_owned());
    let md_pathbuf = PathBuf::from(md_path.to_owned());
    let (template, css_path, out_pdf, md_filepath) = (
        pop_n_push_s(&tmpl_path, 0, &["template.html"]),
        pop_n_push_s(&tmpl_path, 0, &["course-desc.css"]),
        pop_n_push_s(
            &md_pathbuf,
            1,
            &["pdf", &md_filename.replace(".md", ".pdf")],
        ),
        pop_n_push_s(&md_pathbuf, 0, &[md_filename]),
    );
    let out_pdf_path = out_pdf.as_path();
    if out_pdf_path.exists() {
        fs::remove_file(out_pdf_path).unwrap_or_default();
    }
    let (template_s, css_path_s, out_pdf_s, md_filepath_s) = (
        template.to_str().unwrap(),
        css_path.to_str().unwrap(),
        out_pdf.to_str().unwrap(),
        md_filepath.to_str().unwrap(),
    );

    let cmd_line: &str = &format!(
    "{md_filepath_s} -t html5 --template={template_s} --pdf-engine wkhtmltopdf -V margin-top=2 -V margin-left=3 -V margin-right=0 -V margin-bottom=0 --css {css_path_s} -o {out_pdf_s}");

    let exec_res = execvp(pandoc_path, cmd_line);
    let exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!("", execvp, "execvp", pandoc_path, cmd_line)
    } else {
        exec_res
    };

    if out_pdf.exists() && exec_res.is_ok() {
        Ok(out_pdf)
    } else {
        let msg = &format!(
            "pandoc_fill_template: Could not generate pdf file of {md_path}\\{md_filename} \n
            ||  template: {template_s}    ||  md_path: {md_path}       ||   templates_path: {templates_path}        
            ||  pandoc_path: {pandoc_path}    ||   css_path: {css_path_s}    ||   out_pdf: {out_pdf_s}"
        );
        error!(
            "pandoc_fill_template: Could not generate pdf file of {md_path}\\{md_filename} \n
            ||  template: {template_s}    ||  md_path: {md_path}       ||   templates_path: {templates_path}        
            ||  pandoc_path: {pandoc_path}    ||   css_path: {css_path_s}    ||   out_pdf: {out_pdf_s}"
        );
        Err(custom_io_err(msg))
    }
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
pub fn ftcp_parallel(
    pandoc_path: &str,
    wk_path: &str,
    md_path: &str,
    templates_path: &str,
) -> io::Result<()> {
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

                let name = md_file.file_name();
                let name = name.to_str();
                if (name.is_none()) {
                    let message = &format!(
                        "ftcp_parallel, getting file {md_path}\\{:?} line:{}",
                        name,
                        line!()
                    );
                    error!("{}", message);
                    return Err(custom_io_err(message));
                }
                let name = name.unwrap().to_owned();
                pandoc_md_to_pdf(&name, q.0, q.2, q.3)
                // fill_template_convert_pdf(&name, q.0, q.1, q.2, q.3)
            },
        )
        .filter_map(|x| x.err())
        .map(|e| format!("ftcp_parallel {:?}", e))
        .collect();

    match &err_messages.len() {
        0 => Ok(()),
        _ => Err(custom_io_err(&format!(
            "ftcp_parallel {}",
            err_messages.join("\n")
        ))),
    }
}

fn _main() -> io::Result<()> {
    let mut r = 0;
    let rp = get_resources_path();
    let rp = if (rp.is_err()) {
        unwrap_retry_or_log!(&x, get_resources_path, "get_resources_path")
    } else {
        rp
    };

    let (pandoc_path, wk_path, md_path, templates_path) = rp.unwrap();
    let out: Result<(), io::Error> =
        ftcp_parallel(&pandoc_path, &wk_path, &md_path, &templates_path);
    let out = if out.is_err() {
        unwrap_retry_or_log!(
            &out,
            ftcp_parallel,
            "ftcp_parallel",
            &pandoc_path,
            (&wk_path),
            &md_path,
            &templates_path
        )
    } else {
        out
    };
    Ok(())
}

pub fn main() -> io::Result<()> {
    init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS
    _main()
}
