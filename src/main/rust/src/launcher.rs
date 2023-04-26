#![allow(unused)]

use crate::utils::RETRY_AMOUNT;
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log, win_exec::execvp};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::env::temp_dir;
use std::fs::{DirEntry, ReadDir, File};
use std::io::Write;
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
        "-jar  --module-path {} --add-modules javafx.controls,javafx.fxml,javafx.graphics {}", 
        javafx_lib_path, jar_path
    );

    println!("\n {} {}", &java_exe_path, &cmd_line_args);

    /* let cmd_line_args = cmd_line_args.split(" ");
    let vec = cmd_line_args.clone().collect::<Vec<&str>>();

    println!("");
    dbg!(&vec);

    let program = java_exe_path;
    let output = Command::new(program)
        .args(cmd_line_args)
        .spawn()
        // .output()
        .expect("failed to execute process");

    let child_std_out = output.stdout;
    // let out_str = &String::from_utf8(output.stdout.unwrap()).unwrap();
    println!("\n\noutput: {:#?}", child_std_out); */


    let driver_path = extract_driver(abs_path_clean(javadir));

    if cfg!(target_os = "windows") {
        let mut command = String::from("%JAVA_HOME%\\bin\\java -cp ");
        command.push_str(driver_path.as_str());
        command.push_str("%JVM_OPTS% jfxuserform.Main %*");
        let command = format!("{java_exe_path} {cmd_line_args}");
        println!("\n\n{}", command);
        
        Command::new("cmd")
            .args(&["/C", command.as_str()])
            .output()
            .expect("Error spawning Aeron driver process");
    }

    Ok(())
}

fn _main() -> io::Result<()> {
    launch_gui()
}

fn extract_driver(java_dir: String) -> String {
    // let bytes = include_bytes!("aeron-all-1.32.0-SNAPSHOT.jar");
    let bytes = include_bytes!("fancyform.jar");
    let mut driver_path = PathBuf::from(java_dir);
    driver_path.push("fancy2form.jar");
    let mut file = File::create(driver_path.to_owned()).expect("Error extracting Aeron driver jar");
    file.write_all(bytes).unwrap();
    String::from(driver_path.to_str().unwrap())
}


pub fn main() -> io::Result<()> {
    launch_gui()
}
