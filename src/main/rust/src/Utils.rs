use lazy_static::lazy_static;
use path_clean::PathClean;
use std::ffi::OsString;
use std::fs::File;
use std::path::{Path, PathBuf};
use std::{env, io::{self, Write}};


use log::{debug, error, info, trace, warn};
use log4rs;

/// # Description
/// Pops n times given path. and adds sequentially each one in `to_join`
/// # Params
/// - `path`: path to pop
/// - `n`: Number of time to apply `path.pop()` (i.e. go to parent)
/// - `to_join`: slice of string to append to the path (i.e. `path.push(s) for each s in to_join`)
/// # Return
/// `path` after having applied
/// `for _ in 0..n { path.pop() }` and `for s in to_join { path.push(s) } `
pub fn pop_n_push_s(path: &mut PathBuf, n: u16, to_join: &[&str]) -> PathBuf {
    for _ in 0..n { path.pop(); }
    for s in to_join { path.push(s) }
    path.clone()

}

/* fn get_log_file() -> OsString {
    let mut exe_dir: PathBuf = Path::new("C:\\Users\\noahm\\DocumentsNb\\BA4\\Course-Description-Automation\\res\\bin-converters").to_path_buf();
    //path where the actual .exe will be, replace by std:env::current_exe():
    pop_n_push_s(&mut exe_dir, 1, &["log", "rust-convert.log"]);
    //&exe_dir.into_os_string();
    env::current_exe().unwrap().into_os_string()
} */

pub fn absolute_path(path: impl AsRef<Path>) -> io::Result<PathBuf> {
    let path = path.as_ref();
    let absolute_path = if path.is_absolute() {
        path.to_path_buf()
    } else {
        env::current_dir()?.join(path)
    }
    .clean();
    Ok(absolute_path)
}

static WEIRD_PATTERN: &str = "\\\\?\\";
static LOG_CONFIG_FILE: &str = "logging_config.yaml"; 

pub fn abs_path_clean(path: impl AsRef<Path>) -> String {
    let path = absolute_path(path);
    path.expect(&format!("in abs_path_clean"))
        .to_str()
        .unwrap()
        .replace(WEIRD_PATTERN, "")
}

/// # Description
/// function to call initiliaze logging lib, and tell it 
/// to log the config file call `log_config_file` 
/// defaults to the static variable of the same name if given a `None`
pub fn init_log4rs(log_config_file: Option<&str>) {
    let log_config_file = log_config_file.unwrap_or_else(|| LOG_CONFIG_FILE);
    log4rs::init_file(log_config_file, Default::default()).unwrap();
}
