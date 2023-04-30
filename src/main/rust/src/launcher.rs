#![allow(unused)]

use crate::utils::RETRY_AMOUNT;
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::env::temp_dir;
use std::fs::{DirEntry, File, ReadDir};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::{exit, Command, Output};
use std::{env, fs, io};

use log::error;

fn get_java_paths() -> (String, String, String) {
    let pathbuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\launcher.exe");
    // simulate "files" path
    //let pathbuf = env::current_exe().unwrap();
    // FIX: IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    let files_path = "Course-Description-Automation"; // FIX: should actually be "files"

    let javadir = pop_n_push_s(&pathbuf, 1, &[files_path, "res", "java"]);
    let (javafx_lib_path, java_exe_path, jar_path) = (
        abs_path_clean(pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["fancyform.jar"])),
    );
    /* dbg!(&javafx_lib_path);
    dbg!(&java_exe_path);
    println!(""); */
    (java_exe_path, javafx_lib_path, jar_path)
}

/// # Descriptionn
/// Takes in a byte vector and return the underlying represented utf8 string.
/// `Vec<u8>` is the type returned by `outuput.stdout` or `output.stderr`
/// # Param
/// - out: byte vector to convert
/// # Returns
/// Underlying string
/// # Panics
/// If the given byte vector is not a valid utf8 strings
fn extract_std(out: Vec<u8>) -> String {
    String::from_utf8(out).expect("output didn't return a valid utf8 string")
}

fn launch_gui() -> io::Result<Output> {
    let (java_exe_path, javafx_lib_path, jar_path) = get_java_paths();

    Command::new(java_exe_path)
        .args(
            format!(
            "-jar --module-path {} --add-modules javafx.controls,javafx.fxml,javafx.graphics {}",
            javafx_lib_path, jar_path
        )
            .split(" "),
        )
        .output()
    // .expect("failed to execute process");
}

/// # Desc
/// Launch main scala application, that will query the unige database
/// and generate the markdown
///
fn launch_main_scalapp(args: String) -> Result<(), String> {
    Ok(())
}

use crate::{para_convert, win_popup};

pub fn main() -> io::Result<()> {
    let gui_out = launch_gui()?;
    let main_in: String = extract_std(gui_out.stdout);
    dbg!(&main_in);

    let main_out = launch_main_scalapp(main_in);
    let err_msg: Option<String> = match main_out {
        Ok(()) => None,
        Err(msg) => Some(msg),
    };
    dbg!(&err_msg);
    let success = err_msg.is_none();
    if (!success) {
        let retry = win_popup::main(success, err_msg);
        if retry {
            println!("retry");
            return main();
        } else {
            exit(0);
        }
    }
    // let para_convert_res = para_convert::main();
    // TODO: create meaningfull message for user if error
    let err_msg: Option<String> = None;
    // let success = para_convert_res.is_err();
    let success = true;
    let retry = win_popup::main(success, err_msg);
    if retry {
        println!("retry");
        return main();
    } else {
        exit(0);
    }

    Ok(())
}
