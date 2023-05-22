#![allow(unused)]


use crate::utils::RETRY_AMOUNT;
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::env::temp_dir;
use std::fs::{DirEntry, File, ReadDir};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::{Command, Output};
use std::{env, fs, io};

use log::error;

/// # Descriptionn
/// Get the absolute path to the bundled version of java and javafx as well as the path
/// to the jar executable
/// #  Returns
/// Triple `(java_exe_path, javafx_lib_path, jar_path)`
fn get_java_paths() -> (String, String, String) {
    let pathbuf = PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\Course-Description-Automation\launcher.exe");
    // simulate "files" path
    //let pathbuf = env::current_exe().unwrap();
    // FIX: IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    let files_path = "files"; // FIX: should actually be "files"

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
fn extract_std(out: Vec<u8>) -> String { String::from_utf8(out).expect("output didn't return a valid utf8 string") }

fn launch_gui() {
    let (java_exe_path, javafx_lib_path, jar_path) = get_java_paths();
 
    let output = Command::new(java_exe_path)
        .args(format!(
            "-jar --module-path {} --add-modules javafx.controls,javafx.fxml,javafx.graphics {}",
            javafx_lib_path, jar_path
        )
            .split(" "))
        .output()
        .expect("failed to execute process");
    let stdout = extract_std(output.stdout);
    println!("GUI parsed user input:\t\"{stdout}\"");
    println!("GUI stderr:\t\"{}\"", extract_std(output.stderr));
}


pub fn main() -> io::Result<()> {
    let output = launch_gui();
    Ok(())
}

/* fn extract_gui_jar(java_dir: String) -> String {
    // FIX IMPLEMENT ACTUAL PATH WITH FILE DIRECTORY THAT WRAPS EVERYTHING!
    let bytes = include_bytes!("..\\..\\..\\..\\res\\java\\fancyform.jar");
    let mut driver_path = PathBuf::from(java_dir);
    driver_path.push("fancy2form.jar");
    let mut file = File::create(driver_path.to_owned()).expect("Error extracting Aeron driver jar");
    file.write_all(bytes).unwrap();
    String::from(driver_path.to_str().unwrap())
} */



/* fn launch_gui() -> io::Result<()> {
    /* Command::new("cmd")
        .args(&["/C", command.as_str()])
        .output()
        .expect("Error spawning Aeron driver process");
*/
    /* let output = Command::new(program)
        .args(cmd_line_args)
        .output()
        .expect("failed to execute process"); */
    /* let output = Command::new("cmd")
        .args(cmd_line_args)
        .output()
        .expect("failed to execute process");

    let child_std_out = output.stdout;
    // let out_str = &String::from_utf8(output.stdout.unwrap()).unwrap();
    println!("\n\noutput: {:#?}", child_std_out);

    let driver_path = extract_gui_jar(abs_path_clean(javadir)); */

    /* if cfg!(target_os = "windows") {
        let mut command = String::from("%JAVA_HOME%\\bin\\java -cp ");
        command.push_str(driver_path.as_str());
        command.push_str("%JVM_OPTS% jfxuserform.Main %*");
        let command = format!("{java_exe_path} {cmd_line_args}");

        Command::new("cmd")
            .args(&["/C", command.as_str()])
            .output()
            .expect("Error spawning Aeron driver process");
    } */

    Ok(())
} */
