#![allow(unused_imports, dead_code)]
#![allow(non_snake_case)]

pub mod launcher;
pub mod message_dialog;
pub mod para_convert;
pub mod utils;

use std::error::Error;
use std::path::PathBuf;
use std::process::Command;

use pandoc::OutputKind;
use pandoc::PandocOutput::*;
use pandoc::*;

use launcher::{extract_std, extract_stderr, extract_stdout};
use utils::{abs_path_clean, init_log4rs, pop_n_push_s};

fn default_pandoc_options_md_to_pdf() -> Vec<PandocOption> {
    use pandoc::PandocRuntimeSystemOption::MaximumHeapMemory;
    use PandocOption as PO;
    use PO::Var;
    //PO::PdfEngine("wkhtmltopdf".into())

    fn wo(s: &str) -> Option<String> {
        Some(s.to_owned())
    }
    fn w(s: &str) -> String {
        s.to_string()
    }

    /// Wraps endless boilerplate to generate `PandocOption::Var`
    fn var(key: &str, value: &str) -> PandocOption {
        Var(w(key), wo(value))
    }

    vec![
        PO::Template("templates/template.html".into()),
        Var(w("margin-top"), wo("2")),
        Var(w("margin-left"), wo("3")),
        Var(w("margin-right"), wo("0")),
        Var(w("margin-bottom"), wo("0")),
        // PO::Css(w("templates/course-desc.css")),
        PO::RuntimeSystem(vec![MaximumHeapMemory(w("8192M"))]),
        PO::PdfEngine("wkhtmltopdf".into()),
    ]
}

fn test_pandoc(
    md_filename: &str,
    _path: Option<PathBuf>,
) -> Result<pandoc::PandocOutput, pandoc::PandocError> {

    let out_pdf = md_filename.replace(".md", ".pdf");
    let mut pandoc = pandoc::new();
    pandoc
        .add_input(md_filename)
        .set_output_format(OutputFormat::Html, vec![])
        .add_options(&default_pandoc_options_md_to_pdf())
        .set_output(OutputKind::File(out_pdf.into()))
        .set_show_cmdline(true)
        .clone()
        .execute()
}

/**
# Params
- pattern: executable to link in `$PATH` for
# Returns
First line of "`where.exe <pattern>`" on Windows and "`which <pattern>`" elsewhere
*/
fn which(pattern: &str) -> PathBuf {
    let cmd = if cfg!(windows) { "where.exe" } else { "which" };
    extract_stdout(Command::new(cmd).arg(pattern).output().unwrap())
        .split("\n")
        .take(1)
        .collect()
}

pub fn main() {
    /* init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS
    // init_log4rs_debug();
    let paths = utils::current_exe_path();
    dbg!(paths);
    launcher::main(); */

    // let pd_path = which("pandoc");

    let _name = "hello_world.md";
    let name = "desc-2022-11X001.md";
    std::fs::remove_file(name.replace(".md", ".pdf")).expect("can't remove pdfs");

    match test_pandoc(name, None) {
        Ok(_) => println!(
            "dir:\n{}",
            extract_stdout(Command::new("ls").output().unwrap())
        ),
        Err(pd_err) => eprintln!("pandoc error:\n\t{:#?}", pd_err),
    };
}

/* if path.is_some() {
    let mut found = path.unwrap();
    pandoc.add_pandoc_path_hint(&found);
    found.pop();
    pandoc.add_pandoc_path_hint(&found);
}
let mut found = which("miktex-latex.exe");
pandoc.add_latex_path_hint(&found);
found.pop();
pandoc.add_latex_path_hint(&found); */
