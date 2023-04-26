#![allow(unused)]

use crate::utils::RETRY_AMOUNT;
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{env, fs, io};

use log::error;

fn launch_gui() -> io::Result<()> {
    // FIX: simulating relative path where the executable will be :
    let pathbuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\launcher.exe"); // simulate "files" path
    //let mut pathbuf = env::current_exe().unwrap();
    // FIX: IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    let files_path = "Course-Description-Automation"; // should actually be "files"

    let javadir = pop_n_push_s(&pathbuf, 1, &[files_path, "res", "java"]);
    let (javafx_lib_path, java_exe_path) = (
        abs_path_clean(pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"]))
    );
    dbg!(&javafx_lib_path);
    dbg!(&java_exe_path);

    Ok(())
}

pub fn main() -> io::Result<()> {

    launch_gui()
}
