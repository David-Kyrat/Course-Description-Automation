#![allow(dead_code)] // allowing dead code since this is a test file

use crate::utils::init_log4rs;
use crate::{execvp, fill_template_convert_pdf, ftcp_parallel, get_resources_path};

use std::{
    env,
    io::{
        self,
        ErrorKind::{self, Other},
    },
    path::{Path, PathBuf},
};

fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
}

pub fn test_execvp() {
    let args: Vec<String> = env::args().collect();
    let mut exe_path: PathBuf = env::current_exe().expect("Could not get executable path");
    exe_path.pop();
    exe_path.pop();
    let pandoc_path: &_ = &exe_path;

    let app_name: &str = pandoc_path.to_str().expect("cannot parse path");
    let inp_md = &args[1];
    let out_html: &str = "";
    let template = "../../desc-template.html";
    // let cmd_line = &args[1..].join(" ");
    // let cmd_line = &format!("cc {cmd_line}");
    let cmd_line: &str = &format!("{inp_md} -t html --template={template} -o {out_html}");
    dbg!(cmd_line);

    execvp(app_name, cmd_line);
}

pub fn test_get_resources_path() {
    let (pandoc_path, wk_path, md_path, templates_path) = get_resources_path();
    println!(
        "pandoc_path:\n{:#?}, exists? {}",
        pandoc_path,
        Path::new(&pandoc_path).exists()
    );

    println!("--------------------\n");
    println!(
        "wk_path:\n{:#?}, exists? {}",
        wk_path,
        Path::new(&wk_path).exists()
    );

    println!("--------------------\n");
    println!(
        "md_path:\n{:#?}, exists? {}",
        md_path,
        Path::new(&md_path).exists()
    );

    println!("--------------------\n");
    println!(
        "templates_path:\n{:#?}, exists? {}",
        templates_path,
        Path::new(&templates_path).exists()
    );
}

pub fn test_ftcp() -> io::Result<()> {
    let args: Vec<String> = env::args().collect();
    if args.len() != 2 {
        return Err(custom_io_err(
            "Expecting 1 argument (name of markdown file in /res/md)",
        ));
        //io::Error::new(ErrorKind::Other, "Expecting 1 argument (name of markdown file in /res/md)".to_string()));
    }
    // WARN: md input should be only a filename (of a file in res/md/)
    println!("--------------------\n\n");

    let (pandoc_path, wk_path, md_path, templates_path) = get_resources_path();

    let tmp =
        fill_template_convert_pdf(&args[1], &pandoc_path, &wk_path, &md_path, &templates_path)
            .unwrap();
    let out_pdf: &Path = Path::new(&tmp);

    println!("out_pdf:\n{:#?}, exists? {}", out_pdf, out_pdf.exists());
    Ok(())
}

pub fn test_ftcp_parallel() -> io::Result<()> {
    let (pandoc_path, wk_path, md_path, templates_path) = get_resources_path();

    let out = ftcp_parallel(&pandoc_path, &wk_path, &md_path, &templates_path);
    out
}

pub fn test_write_to_log() {
    use log::{debug, error, info, trace, warn};
    init_log4rs(None);
    trace!("detailed tracing info");
    debug!("debug info");
    info!("relevant general info");
    warn!("warning this program doesn't do much");
    error!("error message here");
}

/// NB: each print exactly the same thing
pub fn test_winsafe_error_description() {
    use winsafe::co::ERROR;
    use winsafe::prelude::*;
    use winsafe::{co, AnyResult, SysResult};

    let sys_result: SysResult<()> = Err(co::ERROR::SUCCESS);
    dbg!(&sys_result);

    let err_result: AnyResult<()> = sys_result.map_err(|err| err.into());
    dbg!(&err_result);

    /* println!("{}", ERROR-::LOCKED);
    println!("---------------");
    println!("{:?}", ERROR-::LOCKED);
    println!("---------------");
    println!("{:#?}", ERROR-::LOCKED);
    println!("---------------"); */
}
