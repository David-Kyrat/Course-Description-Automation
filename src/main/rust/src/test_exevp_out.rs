#![allow(unused)]
#![feature(windows_process_extensions_async_pipes)]
// use crate::{abs_path_clean, execvp, get_resources_path, init_log4rs, pop_n_push_s, RETRY_AMOUNT};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::process::Output;
use std::{env, fs, io};

use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{prelude::*, SysResult, HPIPE, HPROCESS, STARTUPINFO};

use log::{error, warn};

pub fn main() -> io::Result<()> {
    /* let executable = "C:\\Users\\noahm\\bin\\wkhtmltopdf.exe";
    let command_line = "-h";
    let out = execvp(executable, command_line, Some(u32::MAX));
    dbg!(out); */
    let executable = "C:\\Users\\noahm\\bin\\wkhtmltopdf.exe";

    use std::os::windows::process::CommandExt;
    use std::process::{Command, Stdio};

    let program = executable;
    let output: Output = Command::new(executable)
        // .args(["/C", "echo hello"])
        .args(&["-h"])
        .output()
        .expect("failed to execute process");
    let out_str = &String::from_utf8(output.stdout).unwrap();
    dbg!(out_str);
    println!("output: {}", out_str);

    Ok(())
}
