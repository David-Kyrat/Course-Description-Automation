#![allow(unused)]

use crate::utils::RETRY_AMOUNT;
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::process::Command;
use std::{env, fs, io};

use log::error;

fn launch_gui() -> io::Result<()> {
    // FIX: simulating relative path where the executable will be :
    let pathbuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\launcher.exe");
    // simulate "files" path
    //let pathbuf = env::current_exe().unwrap();
    // FIX: IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    let files_path = "Course-Description-Automation"; // should actually be "files"

    let javadir = pop_n_push_s(&pathbuf, 1, &[files_path, "res", "java"]);
    let (javafx_lib_path, java_exe_path, jar_path) = (
        abs_path_clean(pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["fancyform.jar"])),
    );
    dbg!(&javafx_lib_path);
    dbg!(&java_exe_path);
    let cmd_line_args = format!(
        "-jar  --module-path {} --add-modules 'javafx.controls,javafx.fxml,javafx.graphics,javafx.web,javafx.media' {}", 
        javafx_lib_path, jar_path
    );

    println!("\n {} {}", &java_exe_path, &cmd_line_args);

    let cmd_line_args = cmd_line_args.split(" ");
    let vec = cmd_line_args.clone().collect::<Vec<&str>>();

    println!("");
    dbg!(&vec);

    let program = java_exe_path;
    let output = Command::new(program)
        .args(cmd_line_args)
        .output()
        .expect("failed to execute process");

    let out_str = &String::from_utf8(output.stdout).unwrap();
    println!("\n\noutput: {}", out_str);

    Ok(())
}

pub fn main() -> io::Result<()> {
    launch_gui()
}
