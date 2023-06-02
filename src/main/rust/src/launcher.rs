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

fn get_java_paths() -> io::Result<(String, String, String)> {
    let pathbuf = env::current_exe().unwrap();

    // FIX IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    // let pathbuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\temp\Course-Description-Automation\launcher.exe");
    let files_path = "files"; // FIX: should actually be "files"

    let javadir = pop_n_push_s(&pathbuf, 1, &[files_path, "res", "java"]);

    let (javafx_lib_path, java_exe_path, jar_path) = (
        pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"]),
        pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"]),
        pop_n_push_s(&javadir, 0, &["fancyform.jar"]),
    );
    if !javafx_lib_path.exists() {
        // println!("{}", abs_path_clean(&javafx_lib_path));
        return Err(io::Error::new(io::ErrorKind::NotFound, format!("javafx_lib_path {} not found", (javafx_lib_path.display()))));
    } 
    if !java_exe_path.exists() {
        return Err(io::Error::new(io::ErrorKind::NotFound, format!("java_exe_path {:#?} not found", (java_exe_path).display())));
    }
    if !jar_path.exists() {
        return Err(io::Error::new(io::ErrorKind::NotFound, format!("jar_path {:#?} not found", (jar_path).display())));
    }
    let (javafx_lib_path, java_exe_path, jar_path) = (
        abs_path_clean(pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["fancyform.jar"])),
    );

    /* dbg!(&javafx_lib_path);
    dbg!(&java_exe_path);
    dbg!(&jar_path);
    println!(""); */
    Ok((java_exe_path, javafx_lib_path, jar_path))
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
    let (java_exe_path, javafx_lib_path, jar_path) = get_java_paths()?;

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

use crate::{log_err, log_if_err, para_convert, win_popup, unwrap_or_log};

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
    let gui_out: Output = unwrap_or_log!(launch_gui(), "launch gui, cannot launch gui"); 

    let main_in: String = extract_std(gui_out.stdout);
    // let main_in: String = "".to_owned();
    // println!("main_in: \"{}\"", &main_in);

    // generate markdown
    let main_out = launch_main_scalapp(main_in);
    let err_msg: Option<String> = match main_out {
        Ok(()) => None,
        Err(msg) => {
            let ms = msg.clone();
            error!("during launcher, after launch_main_scalapp {}", &ms);
            Some(msg)
        }
    };

    // if user input incorrect
    let success = err_msg.is_none();
    if (!success) {
        // println!("launching popup");
        let retry = win_popup::main(success, err_msg);
        if retry {
            // println!("retry\n");
            return main();
        } else {
            // dbg!(&err_msg);
            // println!("exit");
            exit(0);
        }
    }
    let main_result = para_convert::main();
    
    if main_result.is_err() {
        let err_msg = main_result
            .err()
            .map(|e| format!("The following error happened.\n  \"{}\"", e));
        error!("during launcher, after par_convert::main() {}", &err_msg.unwrap());
        // dbg!(&err_msg);
    }

    // convert to pdf
    let success = err_msg.is_none();
    // let success = true;

    // asks user to retry
    let retry = win_popup::main(success, err_msg);
    if retry {
        // println!("retry");
        return main();
    } else {
        exit(0);
    }

    Ok(())
}
