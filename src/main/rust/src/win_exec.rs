/*
#![allow(non_snake_case)]
#![allow(unused)]

use crate::{abs_path_clean, init_log4rs, pop_n_push_s, fr};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{env, fs, io};

use winsafe::prelude::{kernel_Hpipe, kernel_Hstd};
use winsafe::{prelude::*, SECURITY_DESCRIPTOR};
use winsafe::{co::*, HFILE};
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{prelude::*, SysResult, HPIPE, HPROCESS, STARTUPINFO, SECURITY_ATTRIBUTES, PROCESS_INFORMATION};

use log::{error, warn};
/// # Returns
/// `io::Error::new(Other, message)`. i.e. a custom `io::Error`
fn custom_io_err(message: &str) -> io::Error {
    io::Error::new(Other, message)
}


/// # Description
/// Private wrapper around `HPROCESS::CreateProcess`
///
/// # Return
/// `SysResult<CloseHandlePiGuard>` (the result of `HPROCESS::CreateProcess` this function is
/// really just a wrapper around that function.)
fn exec(app_name: &str, command_line: &str) -> SysResult<CloseHandlePiGuard> {
    let (app_name, command_line) = (app_name.trim(), command_line.trim());
    // NOTE: If command has no arguments (i.e. `command_line == ""`) then
    // command_line_opt should be Some(app_name) and 
    // app_name_opt should be none (because in reality `command_line` is argv)
    let app_name_opt = match command_line {
        "" => None,
        _ => Some(app_name),
    };
    let command_line: &str = &format!("{app_name} {command_line}");
    let command_line = command_line.trim();
    let cmd_line_opt = Some(command_line);
    // first word before space in command line should be app_name
    // (it is ignored either way if app_name is not None because its argv[0])
    HPROCESS::CreateProcess(
        app_name_opt,
        cmd_line_opt,
        None,
        None,
        true,
        CREATE::NO_WINDOW | CREATE::INHERIT_PARENT_AFFINITY,
        None, //inherits
        None, // inherits
        &mut STARTUPINFO::default())
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
    let close_handle_res = exec(app_name, command_line);
    
    if close_handle_res.is_err() {
        return Err(custom_io_err(&format!(
            "WinErr: could not start process '{app_name} {command_line}', {}.   Line {}, File '{}'",
            close_handle_res.map(|_| ()).unwrap_err(),
            line!(),
            file!()
        )));
    }

    let wait_time = match wait_time {
        Some(amount) => Some(amount),
        None => Some(10_000), //10 sec by default
    };

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
*/
