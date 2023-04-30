#![allow(unused)]

use crate::utils::RETRY_AMOUNT;
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::env::temp_dir;
use std::error::Error;
use std::fs::{DirEntry, File, ReadDir};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::{exit, Command, Output};
use std::{env, fs, io};

extern crate native_windows_derive as nwd;
extern crate native_windows_gui as nwg;

use log::error;

fn get_java_paths() -> (String, String, String) {
    let pathbuf = env::current_exe().unwrap();

    // FIX IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    // let pathbuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\temp\Course-Description-Automation\launcher.exe");
    let files_path = "files"; // FIX: should actually be "files"

    let javadir = pop_n_push_s(&pathbuf, 1, &[files_path, "res", "java"]);
    let (javafx_lib_path, java_exe_path, jar_path) = (
        abs_path_clean(pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["fancyform.jar"])),
    );
    /* dbg!(&javafx_lib_path);
    dbg!(&java_exe_path);
    dbg!(&jar_path);
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

use crate::{log_err, log_if_err, para_convert, win_popup};

/// # Desc
/// Launcher for the whole project. There are several steps.
///
/// 1. Launch gui to ask for user input
/// 2) If user input is correct launch the "main" part (scala app that
/// will generate the markdown documents)
///     2. if its not, asks user if he wants to retry
/// 3. Launch conversion of markdown files to pdf
/// 4) Display message to inform of success / error of conversion to pdf
/// and asks to user whether he wants to retry
///
///
/// # Returns
/// `Ok(())` i.e. Nothing if success. The error of the function that failed otherwise.
pub fn main() -> io::Result<()> {
    // gui input

    let gui_out = launch_gui();
    let gui_out = if let Ok(output) = gui_out {
        output
    } else {
        log_err!(gui_out.err().unwrap(), "cannot launch gui");
        panic!()
    };

    // let err: Result<(), String> = log_if_err!(gui_out, "cannot launch gui2");

    // let gui_out = if let Err(e) = err { panic!(); } else { gui_out.unwrap() };

    // gui_out = if let err.is_ok() == true { gui_out.unwrap() } ;
    //else { panic!("")};

    let main_in: String = extract_std(gui_out.stdout);
    // let main_in: String = "".to_owned();
    println!("main_in: \"{}\"", &main_in);

    // generate markdown
    let main_out = launch_main_scalapp(main_in);
    let err_msg: Option<String> = match main_out {
        Ok(()) => None,
        Err(msg) => Some(msg),
    };
    dbg!(&err_msg);

    // if user input incorrect
    let success = err_msg.is_none();
    if (!success) {
        println!("launching popup");
        let retry = win_popup::main(success, err_msg);
        if retry {
            println!("retry\n");
            return main();
        } else {
            println!("exit");
            exit(0);
        }
    }
    let err_msg: Option<String> = para_convert::main()
        .err()
        .map(|e| format!("The following error happened.\n  \"{}\"", e));
    dbg!(&err_msg);
    // let err_msg: Option<String> = None;

    // convert to pdf
    let success = err_msg.is_none();
    // let success = true;

    // asks user to retry
    let retry = win_popup::main(success, err_msg);
    if retry {
        println!("retry");
        return main();
    } else {
        exit(0);
    }

    Ok(())
}
