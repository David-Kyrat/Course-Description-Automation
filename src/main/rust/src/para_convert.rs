use crate::{
    exec::execvp,
    fr, log_err,
    utils::{abs_path_clean, current_exe_path, e_to_s, pop_n_push_s},
};

use log::{error, warn};
use rayon::{self, iter::*};

use std::path::PathBuf;
use std::{fs, io};

/// # Returns
/// `io::Error::new(Other, message)`. i.e. a custom `io::Error`
/* fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
} */

/// # Description
/// Modify this process' environment variables
/// # Errors
/// This function will return an error if it failed to joined the current `$PATH` with `to_add`
/* fn add_to_path(to_add: PathBuf) -> Result<(), env::JoinPathsError> {
    let path = env::var_os("PATH").unwrap();
    let mut paths = env::split_paths(&path).collect::<Vec<_>>();
    paths.push(to_add);
    let new_path = env::join_paths(paths)?;
    env::set_var("PATH", &new_path);
    Ok(())
} */
/*
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
pub fn execvp(exe_path: &str, cmd_line: &[&str]) -> io::Result<Output> {
    Command::new(exe_path)
        .args(cmd_line.iter().map(OsString::from))
        .stdout(Stdio::null())
        .env(
            "PATH",
            format!("/usr/local/bin:/usr/local/sbin:{}:/bin:/sbin", env!("PATH")),
        )
        .spawn()?
        .wait_with_output()
} */

/// # Description
/// Return a (`Result` of) 4-tuple containing the paths to the executables
/// of pandoc and wkhtmltopdf, and the paths to the markdown
/// and templates resource directory ('res/md' and 'res/templates')
///
/// # Returns
/// `Result<(pandoc_path, md_path, templates_path), io::Error>`
fn get_resources_path() -> io::Result<(String, String, String)> {
    let res_path = pop_n_push_s(&current_exe_path(), 1, &["files", "res"]);
    let exes_path = pop_n_push_s(&res_path, 0, &["bin-converters"]);

    let pandoc_bin_name = if cfg!(target_os = "windows") {
        "pandoc.exe"
    } else {
        "pandoc"
    };
    let (pandoc, md, templates) = (
        pop_n_push_s(&exes_path, 0, &[pandoc_bin_name]),
        pop_n_push_s(&res_path, 0, &["md"]),
        pop_n_push_s(&res_path, 0, &["templates"]),
    );
    if !pandoc.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("pandoc path {} not found", pandoc.display()),
        ));
    }
    if !md.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("md path {} not found", md.display()),
        ));
    }
    if !templates.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("templates path {} not found", templates.display()),
        ));
    }
    Ok((
        abs_path_clean(pandoc),
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
    md_filename: &str,
    pandoc_path: &str,
    md_path: &str,
    templates_path: &str,
) -> Result<PathBuf, String> {
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
        "--quiet",
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
    ];

    let _exec_res = execvp(pandoc_path, cmd_line).map_err(|error| {
            // let err_msg = &error.to_string();
            log_err!(format!("{error}"), format!("pandoc_md_to_pdf. conversion to pdf execvp({}, {:?}) returned an error", &pandoc_path, &cmd_line));
            warn!(
            "pandoc_md_to_pdf:  {md_path}/{md_filename}\n\n
            ---------------------------------------------------------------
            \n|| template: {template_s}    \n||  md_path: {md_path}       \n||  templates_path: {templates_path} 
            \n||  pandoc_path: {pandoc_path}    \n||  css_path: {css_path_s}    \n||  out_pdf: {out_pdf_s}");
            error
    })?;

    if out_pdf.exists() {
        Ok(out_pdf)
    } else {
        // let er = format!("std_err: \n-----\n\t{}", extract_std(exec_res.stderr));
        let msg = &format!(
            "pandoc_md_to_pdf: PDF Not generated: {md_path}/{md_filename}\n \n
            ---------------------------------------------------------------
            \n|| template: {template_s}    \n||  md_path: {md_path}       \n||  templates_path: {templates_path}        
            \n||  pandoc_path: {pandoc_path}    \n||  css_path: {css_path_s}    \n||  out_pdf: {out_pdf_s}",

        );
        error!("{}", &msg);
        Err(msg.to_string())
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
fn ftcp_parallel(pandoc_path: &str, md_path: &str, templates_path: &str) -> io::Result<()> {
    let dir_ent_vec: Vec<PathBuf> = fs::read_dir(md_path)?
        .filter_map(|dir_entry| {
            dir_entry
                .map(|ok_value| ok_value.path())
                .ok()
                .and_then(|dir_entry| {
                    if dir_entry.extension().map_or(false, |ext| ext == "md") {
                        Some(dir_entry)
                    } else {
                        None
                    }
                })
        })
        .collect();

    // filters only dir-entry that were successfully read and map each of theme to their path at the same time
    let mdfiles = dir_ent_vec
        .par_iter()
        .filter(|dir_entry| dir_entry.extension().map_or(false, |ext| ext == "md")) // now that we only have valid values (and not options) we can filter the non-markdown files
        .for_each(|md_file_path| {
            pandoc_md_to_pdf(
                md_file_path.file_name().unwrap().to_str().unwrap(),
                pandoc_path,
                md_path,
                templates_path,
            )
            .ok();
        });

    Ok(mdfiles)
    // NB: No need to recover any error, since they'd have already been logged in the methods above.
    // (and since there is no specific action we can really do about it other than logging it)
}

pub fn main() -> io::Result<()> {
    let rp = get_resources_path();
    let (pandoc_path, md_path, templates_path) = rp.map_err(|err| {
        log_err!(err, "main, cannot get resources_path");
        err
    })?;

    // add_to_path(PathBuf::from(&wk_path)).map_err(|c| custom_io_err(&format!("{:#?}", c)))?;
    // dbg!(env!("PATH"));
    // let out: Result<(), io::Error> = ftcp_parallel(&pandoc_path, &wk_path, &md_path, &templates_path).map_err(|err|)
    ftcp_parallel(&pandoc_path, &md_path, &templates_path).map_err(|err| {
        let msg = fr!(e_to_s("in main, calling Ftcp_parallel.")(err));
        error!("--------\n\n {}", msg);
        io::Error::new(io::ErrorKind::Other, msg)
    })
}
