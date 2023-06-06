#![allow(unused)]

use crate::message_dialog::*;
use crate::utils::{self, current_exe_path, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log};

use io::ErrorKind::Other;
use native_dialog::MessageType;
use rayon::iter::*;
use std::any::Any;
//use std::env::temp_dir;
use std::error::Error;
use std::fs::{DirEntry, File, ReadDir};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::{exit, Command, ExitStatus, Output};
use std::{env, fs, io, panic, thread};

use log::error;
fn get_java_paths() -> io::Result<(String, String, String, String)> {
    let pathbuf = current_exe_path();

    let javadir = pop_n_push_s(&pathbuf, 1, &["files", "res", "java"]);

    let (javafx_lib_path, java_exe_path, jar_path) = (
        pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"]),
        pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"]),
        pop_n_push_s(&javadir, 0, &["fancyform.jar"]),
    );
    if !javafx_lib_path.exists() {
        // println!("{}", abs_path_clean(&javafx_lib_path));
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("javafx_lib_path {} not found", (javafx_lib_path.display())),
        ));
    }
    if !java_exe_path.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("java_exe_path {:#?} not found", (java_exe_path).display()),
        ));
    }
    if !jar_path.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("jar_path {:#?} not found", (jar_path).display()),
        ));
    }
    let (javafx_lib_path, java_exe_path, jar_path, scala_jar_path) = (
        abs_path_clean(pop_n_push_s(&javadir, 0, &["javafx-sdk-19", "lib"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["jdk-17", "bin", "java.exe"])),
        abs_path_clean(pop_n_push_s(&javadir, 0, &["fancyform.jar"])),
        abs_path_clean(pop_n_push_s(
            &javadir,
            0,
            &["Course-Description-Automation.jar"],
        )),
    );

    /* dbg!(&javafx_lib_path);
    dbg!(&java_exe_path);
    dbg!(&jar_path);
    dbg!(&scala_jar_path); */
    // println!("");
    Ok((java_exe_path, javafx_lib_path, jar_path, scala_jar_path))
}

/// Computes the path of the file that should be at '/files/res/abbrev.tsv'
///
/// # Errors
///
/// This function will return an error if .
fn get_abbrev_file_path() -> String {
    let ab_fp = pop_n_push_s(&current_exe_path(), 1, &["files", "res", "abbrev.tsv"]);
    abs_path_clean(ab_fp)
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
    let (java_exe_path, javafx_lib_path, jar_path, scala_jar_path) = get_java_paths()?;
    let abbrevfile_path = get_abbrev_file_path();
    // dbg!(&abbrevfile_path);

    Command::new(java_exe_path)
        .args(
            format!(
            "-jar --module-path {} --add-modules javafx.controls,javafx.fxml,javafx.graphics {} {}",
            javafx_lib_path, jar_path, abbrevfile_path
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
fn launch_main_scalapp(args: &String) -> io::Result<Output> {
    let (java_exe_path, javafx_lib_path, jar_path, scala_jar_path) = get_java_paths()?;
    let cmd = &[java_exe_path.as_str(), "-jar", scala_jar_path.as_str(), args];
    // dbg!(&cmd.join(" "));

    Command::new(java_exe_path)
        .args(&["-jar", scala_jar_path.as_str(), args])
        .output()
}

use crate::{log_err, log_if_err, para_convert, unwrap_or_log};

/// # Description
/// Wrapper function around the program to able to simply propagate any amount
/// of errors up to `main()` (with `?`), to be dealt with only once.
/// (the first if there is one will just stop the execution and go back to `main()` with the error message)
/// # Returns
/// Nothing or Error message
fn sub_main() -> Result<(), String> {
    fn err_fmter<T: std::fmt::Debug>(add_msg: &str, cause: &T) -> String {
        format!(
            "The following error happened:\nLauncher: {add_msg} : {:#?}",
            cause
        )
    }

    let gui_out: String =
        panic::catch_unwind(|| unwrap_or_log!(launch_gui(), "launch gui, cannot launch gui"))
            .map(|output| extract_std(output.stdout))
            .map_err(|cause| err_fmter("Cannot launch gui", &cause))?;

    //Propagate error (i.e. return an `Err(...)` if returned value is not an `Ok(...)`)

    thread::spawn(|| quick_message_dialog("Generating", "Generating pdfs please wait...", None));
    // generate markdown
    let main_out: Output = panic::catch_unwind(|| {
        unwrap_or_log!(launch_main_scalapp(&gui_out), "cannot launch scala app")
    })
    .map_err(|cause| err_fmter("Cannot launch app", &cause))?;

    // dbg!(&main_out.status);
    let y = extract_std(main_out.clone().stdout);
    // println!("[src\\launcher.rs:157] &main_out.stdout = {y}");
    // if user input incorrect or other unexpected error
    let main_success: &bool = &main_out.status.success();
    if !main_success {
        return Err(extract_std(main_out.stderr));
    }

    let main_result = para_convert::main()
        .map_err(|cause| err_fmter("Not all pdf could be generated", &cause))?;

    Ok(())
}

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
pub fn main() {
    fn ok_popup() -> bool {
        let err_msg = "main could not display popup";
        quick_yesno_dialog(
            "Success",
            &format!("PDF generation successful.\n Do you want to generate anything else?"),
            None,
        )
        .expect(err_msg)
    }

    fn error_popup(message: String) -> bool {
        let err_msg = "main could not display popup";
        quick_yesno_dialog(
            "Error",
            &format!("{message}\n Do you want to retry?"),
            Some(MessageType::Error),
        )
        .expect(err_msg)
    }

    let prog_result = sub_main();
    let retry = match prog_result {
        Ok(()) => ok_popup(), // Ok popup
        Err(message) => error_popup(message),
    };

    if retry {
        return main();
    }
}
