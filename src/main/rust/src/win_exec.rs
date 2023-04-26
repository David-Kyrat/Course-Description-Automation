#![allow(non_snake_case)]
#![allow(unused)]

use crate::{abs_path_clean, init_log4rs, pop_n_push_s};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{env, fs, io};

use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{prelude::*, SysResult, HPIPE, HPROCESS, STARTUPINFO};

use log::{error, warn};

/// # Returns
/// `io::Error::new(Other, message)`. i.e. a custom `io::Error`
fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
}
/// Formats error, with line and file in msg
macro_rules! fr {
    ($msg: expr) => {
        format!("{}.\n\t Line {}, File '{}'.\n", $msg, line!(), file!())
    };
}

const RETRY_AMOUNT: u8 = 5;

/// # Description
/// "Overload" of `execvp` but that returns the output of the started process.
/// Copied doc from `execvp`:
///
/// Creates a process using winsafe api. (safe wrapper around windows sdk api).
/// On Windows, Creating a new process always comes with the execution of some executable in a new
/// thread / in that child process (there is no "just" fork.)
/// That's why this function take an absolute_path to an executable
/// and the the arguments to pass to it (argv).
///
/// # Params
/// - `app_name`: Absolute path to an executable
/// - `command_line`: Argument to program (equivalent of `argv`)
/// - `wait_time`: time to wait for the process to finish (in milliseconds)
///
/// # NB
/// This functions returns the output of the "child" process after having waited on it.
/// (Although the wait is not mandatory to avoid zombies thanks to the winsafe api,
/// here we just want to wait for the completion of the job.)
pub fn execvp_out(app_name: &str, command_line: &str, wait_time: Option<u32>) -> io::Result<String> {
    let mut si: STARTUPINFO = STARTUPINFO::default();
    let (app_name, command_line) = (app_name.trim(), command_line.trim());
    let wait_time = match wait_time {
        Some(amount) => Some(amount),
        None => Some(10_000), //10 sec by default
    };

    // NOTE: If command has no arguments (i.e. `command_line == ""`) then
    // command_line_opt should be Some(app_name) and
    // app_name_opt should be none (because in reality `command_line` is argv, )
    let app_name_opt = match command_line {
        "" => None,
        _ => Some(app_name),
    };
    let command_line: &str = &format!("{app_name} {command_line}"); //append name of program
    let cmd_line_opt = Some(command_line);
    // first word before space in command line should be app_name
    // (it is ignored either way if app_name is not None because its argv[0])

    let close_handle_res: SysResult<CloseHandlePiGuard> = HPROCESS::CreateProcess(
        app_name_opt,
        cmd_line_opt,
        None,
        None,
        true,
        CREATE::NO_WINDOW | CREATE::INHERIT_PARENT_AFFINITY,
        None, //inherits
        None, // inherits
        &mut si,
    );
    if close_handle_res.is_err() {
        return Err(custom_io_err(&format!(
            "WinErr: could not start process '{app_name} {command_line}', {}.   Line {}, File '{}'",
            close_handle_res.map(|_| ()).unwrap_err(),
            line!(),
            file!()
        )));
    }

    let outpPipe: HPIPE = si.hStdOutput;
    /* let close_handle = close_handle_res.unwrap();
    let wait_res = HPROCESS::WaitForSingleObject(&close_handle.hProcess, wait_time);
    // waits 10 sec at most. Its ok to wait this long because the calls are often made in parallel so they're not actually blocking each other
    if wait_res.is_err() {
        warn!(
            "Could not wait on child process: {app_name} {command_line}\n\t{}",
            wait_res.unwrap_err()
        );
    } */
    let mut contentBuffer: [u8; 4000] = [0; 4000]; //Creating 4kb buffer
    let mut content_total: Vec<u8> = Vec::new();
    let (mut byte_read, mut attempt) = (1, 0);

    while (byte_read > 0) {
        let res = outpPipe.ReadFile(&mut contentBuffer, None);

        byte_read = if res.is_err() {
            error!("{}\n\t{:?}", fr!(&format!( "Could not read output from : {app_name} {command_line}")), &res);
            if attempt < RETRY_AMOUNT {
                attempt += 1;
                1
            } else { 0 }
        } else {
            content_total.extend_from_slice(&contentBuffer);
            contentBuffer = [0; 4000];
            res.unwrap()
        };
    }

    let content_total = String::from_utf8(content_total).unwrap();
    Ok(content_total)
}


/// # Description
/// Creates a process using winsafe api. (safe wrapper around windows sdk api).
/// On Windows, Creating a new process always comes with the execution of some executable in a new
/// thread / in that child process (there is no "just" fork.)
/// That's why this function take an absolute_path to an executable
/// and the the arguments to pass to it (argv)
///
/// # Params
/// - `app_name`: Absolute path to an executable
/// - `command_line`: Argument to program (equivalent of `argv`)
/// - `wait_time`: time to wait for the process to finish (in milliseconds)
///
/// # NB
/// This functions returns after having waited on the "child" process.
/// (Although the wait is not mandatory to avoid zombies thanks to the winsafe api,
/// here we just want to wait for the completion of the job.)
pub fn execvp(app_name: &str, command_line: &str, wait_time: Option<u32>) -> io::Result<()> {
    let mut si: STARTUPINFO = STARTUPINFO::default();
    
    let wait_time = match wait_time {
        Some(amount) => Some(amount),
        None => Some(10_000), //10 sec by default
    };

    let (app_name, command_line) = (app_name.trim(), command_line.trim());

    // NOTE: If command has no arguments (i.e. `command_line == ""`) then
    // command_line_opt should be Some(app_name) and
    // app_name_opt should be none (because in reality `command_line` is argv, )
    let app_name_opt = match command_line {
        "" => None,
        _ => Some(app_name),
    };
    let command_line: &str = &format!("{app_name} {command_line}"); //append name of program
    let cmd_line_opt = Some(command_line);
    // first word before space in command line should be app_name
    // (it is ignored either way if app_name is not None because its argv[0])

    let close_handle_res: SysResult<CloseHandlePiGuard> = HPROCESS::CreateProcess(
        app_name_opt,
        cmd_line_opt,
        None,
        None,
        true,
        CREATE::NO_WINDOW | CREATE::INHERIT_PARENT_AFFINITY,
        None, //inherits
        None, // inherits
        &mut si,
    );
    if close_handle_res.is_err() {
        return Err(custom_io_err(&format!(
            "WinErr: could not start process '{app_name} {command_line}', {}.   Line {}, File '{}'",
            close_handle_res.map(|_| ()).unwrap_err(),
            line!(),
            file!()
        )));
    }

    let close_handle = close_handle_res.unwrap();
    let wait_res = HPROCESS::WaitForSingleObject(&close_handle.hProcess, wait_time); // waits 10 sec at most
                                                                                        // its ok to wait this long because the calls are often made in parallel so they're not actually blocking each other
    if wait_res.is_err() {
        warn!(
            "Could not wait on child process: {app_name} {command_line}\n\t{}",
            wait_res.unwrap_err()
        );
    }
    Ok(())
}

