use std::{
    ffi::OsString,
    process::{Command, Stdio},
};

use crate::{fr, utils::e_to_s};

/// # Description
/// Wrapper arround a `std::process::Command::new(...).args(...).spawn().wait()` i.e.
/// * Creates a new `std::process::Command` instance with the executable path given by `exe_path`.  
/// * Sets its arguments to `cmd_line`.
/// * `spawn()` the child (returning the error if their were any.)
/// * Then `wait()` on said child.  
/// # Params
/// * `exe_path` - Path of the executable to give to the constructor of `Command`
/// * `cmd_line` - arguments to that comand (e.g. for "`ls -la`", `args` = `["-la"]`)
/// # Returns
/// Result containing `ExitStatus` of child process (or the error)
// pub fn execvp(exe_path: &str, cmd_line: &[&str]) -> io::Result<()> {
pub fn execvp(exe_path: &str, cmd_line: &[&str]) -> Result<(), String> {
    Command::new(exe_path)
        .args(cmd_line.iter().map(OsString::from))
        .stdout(Stdio::null())
        .env(
            "PATH",
            format!("/usr/local/bin:/usr/local/sbin:{}:/bin:/sbin", env!("PATH")),
        )
        .spawn()
        .unwrap()
        .wait_with_output()
        .map(|_| ())
        .map_err(|err| {
            let msg = e_to_s(&fr!(format!(
                "cannot launch \"{}\" \"{:?}\"",
                exe_path, cmd_line
            )))(err);
            log::error!("{msg}");
            msg
        })
}
