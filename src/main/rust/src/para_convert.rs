#![allow(non_snake_case)]
#![allow(dead_code)]

use crate::utils::{current_exe_path, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::ffi::OsString;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::process::{Command, ExitStatus, Stdio};
use std::{fs, io};

use log::error;
/// # Returns
/// `io::Error::new(Other, message)`. i.e. a custom `io::Error`
fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
}

use std::env;


/// # Description
/// Modify this process' environment variables
/// # Errors
/// This function will return an error if it failed to joined the current `$PATH` with `to_add`
pub fn add_to_path(to_add: PathBuf) -> Result<(), env::JoinPathsError> {
    let path = env::var_os("PATH").unwrap();
    let mut paths = env::split_paths(&path).collect::<Vec<_>>();
    paths.push(to_add);
    let new_path = env::join_paths(paths)?;
    env::set_var("PATH", &new_path);
    Ok(())
}

/// # Description
/// Wrapper arround a `std::process::Command::new(...).args(...).spawn().wait()` i.e.
/// * Creates a new `std::process::Command` instance with the executable path given by `exe_path`.  
/// * Sets its arguments to `cmd_line`.
/// * `spawn()` the child (returning the error if their were any.)
/// * Then `wait()` on said child.  
/// # Params
/// * `exe_path` - Path of the executable to give to the constructor of `Command`
/// * `cmd_line` - arguments to that comand (e.g. for "`ls -la`", `args` = `["-la"]`)
/// # Returns
/// Result containing `ExitStatus` of child process (or the error)
pub fn execvp(exe_path: &str, cmd_line: &[&str]) -> io::Result<ExitStatus> {
    Command::new(exe_path)
        .args(cmd_line.iter().map(|s| OsString::from(s)))
        // .stdout(Stdio::null())
        // .stderr(Stdio::null())
        .spawn()?
        .wait()
}

/// # Description
/// Return a (`Result` of) 4-tuple containing the paths to the executables
/// of pandoc and wkhtmltopdf, and the paths to the markdown
/// and templates resource directory ('res/md' and 'res/templates')
///
/// # Returns
/// `Result<(pandoc_path, wkhtmltopdf_path, md_path, templates_path), io::Error>`
fn get_resources_path() -> io::Result<(String, String, String, String)> {
    let res_path = pop_n_push_s(&current_exe_path(), 1, &["files", "res"]);
    let exes_path = pop_n_push_s(&res_path, 0, &["bin-converters"]);
    let (pandoc, wkhtml, md, templates) = (
        pop_n_push_s(&exes_path, 0, &["pandoc"]),
        pop_n_push_s(&exes_path, 0, &["wkhtmltopdf"]),
        pop_n_push_s(&res_path, 0, &["md"]),
        pop_n_push_s(&res_path, 0, &["templates"]),
    );
    if !pandoc.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("pandoc path {:#?} not found", pandoc.display()),
        ));
    }
    if !md.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("md path {:#?} not found", md.display()),
        ));
    }
    if !templates.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("templates path {:#?} not found", templates.display()),
        ));
    }
    Ok((
        abs_path_clean(pandoc),
        abs_path_clean(wkhtml),
        abs_path_clean(md),
        abs_path_clean(templates),
    ))
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
        fs::remove_file(out_pdf_path).unwrap();
    }
    let (template_s, css_path_s, out_pdf_s, md_filepath_s) = (
        template.to_str().unwrap(),
        css_path.to_str().unwrap(),
        out_pdf.to_str().unwrap(),
        md_filepath.to_str().unwrap(),
    );

    let cmd_line: &[&str] = &[
        md_filepath_s,
        "-t",
        "html5",
        &format!("--template={template_s}"),
        "--pdf-engine",
        "wkhtmltopdf",
        "-V",
        "margin-top=2",
        "-V",
        "margin-left=3",
        "-V",
        "margin-right=0",
        "-V",
        "margin-bottom=0",
        "--css",
        css_path_s,
        "-o",
        out_pdf_s,
        "--quiet",
    ];

    let exec_res = execvp(pandoc_path, cmd_line);
    let exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!("", execvp, "execvp", pandoc_path, cmd_line)
    } else {
        exec_res
    };

    if out_pdf.exists() && exec_res.is_ok() {
        Ok(out_pdf)
    } else {
        let er = if !out_pdf.exists() {
            "".to_owned()
        } else {
            format!("\n{:?}", &exec_res)
        };
        let msg = &format!(
            "pandoc_md_to_pdf: PDF Not generated: {md_path}\\{md_filename} {er}
            ---------------------------------------------------------------
            \n|| template: {template_s}    \n||  md_path: {md_path}       \n||  templates_path: {templates_path}        
            \n||  pandoc_path: {pandoc_path}    \n||  css_path: {css_path_s}    \n||  out_pdf: {out_pdf_s}",

        );
        error!("{}", &msg);
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
                if name.is_none() {
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

pub fn main() -> io::Result<()> {
    let rp = get_resources_path();
    let rp = if rp.is_err() {
        unwrap_retry_or_log!(&x, get_resources_path, "get_resources_path")
    } else {
        rp
    };

    let (pandoc_path, wk_path, md_path, templates_path) = rp.unwrap();
    // add_to_path(PathBuf::from(&wk_path)).map_err(|c| custom_io_err(&format!("{:#?}", c)))?;
    // dbg!(env!("PATH"));

    let out: Result<(), io::Error> =
        ftcp_parallel(&pandoc_path, &wk_path, &md_path, &templates_path);
    if out.is_err() {
        unwrap_retry_or_log!(
            &out,
            ftcp_parallel,
            "ftcp_parallel",
            &pandoc_path,
            &wk_path,
            &md_path,
            &templates_path
        )
    } else {
        out
    }
    // Ok(())
}
