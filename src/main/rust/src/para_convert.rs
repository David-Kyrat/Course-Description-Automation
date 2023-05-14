#![allow(non_snake_case)]
#![allow(dead_code)]

use crate::utils::{current_exe_path, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{fs, io};

use log::error;
/// # Returns
/// `io::Error::new(Other, message)`. i.e. a custom `io::Error`
fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
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
        pop_n_push_s(&exes_path, 0, &["pandoc.exe"]),
        pop_n_push_s(&exes_path, 0, &["wkhtmltopdf.exe"]),
        pop_n_push_s(&res_path, 0, &["md"]),
        pop_n_push_s(&res_path, 0, &["templates"]),
    );
    if !pandoc.exists() {
        return Err(io::Error::new(io::ErrorKind::NotFound, format!("pandoc path {:#?} not found", pandoc.display()))); 
    }
    if !md.exists() {
        return Err(io::Error::new(io::ErrorKind::NotFound, format!("md path {:#?} not found", md.display()))); 
    }
    if !templates.exists() {
        return Err(io::Error::new(io::ErrorKind::NotFound, format!("templates path {:#?} not found", templates.display())));
    }
    Ok((
        abs_path_clean(pandoc),
        abs_path_clean(wkhtml),
        abs_path_clean(md),
        abs_path_clean(templates),
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

    let exec_res = execvp(pandoc_path, cmd_line, None);
    let _exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!("", execvp, "execvp", pandoc_path, cmd_line, None)
    } else {
        exec_res
    };

    let out_html: &Path = Path::new(&out_html);

    if out_html.exists() {
        Ok(out_html.to_path_buf())
    } else {
        let msg = &format!(
            "pandoc_fill_template: Could not generate html file {md_path}\\{md_filename} \n
            \n||  template: {templates_path}    \n||  md_path: {md_path}       \n||   templates_path: {templates_path}        
            \n||  pandoc_path: {pandoc_path}"
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
    /* let mut out_pdf: PathBuf = env::current_exe().expect("wkhtmltopdf: could not get current_dir");
    let mut out_pdf: PathBuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\Course-Description-Automation\res\bin-converters\rust_para_convert-mdToPdf.exe"); */
    let mut out_pdf = pop_n_push_s(&current_exe_path(), 2, &["pdf"]);

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

    let exec_res = execvp(wk_path, cmd_line, None);
    let _exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!(exec_res, execvp, "execvp(wkhtml)", wk_path, cmd_line, None)
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
fn fill_template_convert_pdf(
    md_filename: &String,
    pandoc_path: &str,
    wk_path: &str,
    md_path: &str,
    templates_path: &str,
) -> io::Result<PathBuf> {
    let out_html = pandoc_fill_template(md_filename, pandoc_path, md_path, templates_path);
    let out_html = if out_html.is_err() {
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

    if out_html.is_err() {
        return out_html;
    } // do not try conversion of html to pdf if md to html failed

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
        fs::remove_file(out_pdf_path).unwrap();
    }
    let (template_s, css_path_s, out_pdf_s, md_filepath_s) = (
        template.to_str().unwrap(),
        css_path.to_str().unwrap(),
        out_pdf.to_str().unwrap(),
        md_filepath.to_str().unwrap(),
    );

    let cmd_line: &str = &format!(
    "{md_filepath_s} -t html5 --template={template_s} --pdf-engine wkhtmltopdf -V margin-top=2 -V margin-left=3 -V margin-right=0 -V margin-bottom=0 --css {css_path_s} -o {out_pdf_s}");

    let exec_res = execvp(pandoc_path, cmd_line, None);
    let exec_res = if exec_res.is_err() {
        unwrap_retry_or_log!("", execvp, "execvp", pandoc_path, cmd_line, None)
    } else {
        exec_res
    };

    if out_pdf.exists() && exec_res.is_ok() {
        Ok(out_pdf)
    } else {
        let er = if !out_pdf.exists() { "".to_owned() } else { format!("\n{:?}", &exec_res) };
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
    let _r = 0;
    let rp = get_resources_path();
    let rp = if rp.is_err() {
        unwrap_retry_or_log!(&x, get_resources_path, "get_resources_path")
    } else {
        rp
    };

    let (pandoc_path, wk_path, md_path, templates_path) = rp.unwrap();
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
