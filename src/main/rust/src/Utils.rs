use path_clean::PathClean;

use log4rs;
use std::path::{Path, PathBuf};
use std::{env, io};

/// # Description
/// Pops n times given path. and adds sequentially each one in `to_join`
/// # Params
/// - `path`: path to pop
/// - `n`: Number of time to apply `path.pop()` (i.e. go to parent)
/// - `to_join`: slice of string to append to the path (i.e. `path.push(s) for each s in to_join`)
/// # Return
/// `path` after having applied
/// `for _ in 0..n { path.pop() }` and `for s in to_join { path.push(s) } `
pub fn pop_n_push_s(path: &PathBuf, n: u16, to_join: &[&str]) -> PathBuf {
    let mut path = path.clone(); // copy to avoid modifying argument
    for _ in 0..n {
        path.pop();
    }
    for s in to_join {
        path.push(s)
    }
    path.clone()
}

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
static LOG_CONFIG_FILE_NAME: &str = "logging_config.yaml";

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
pub fn init_log4rs(log_config_file: Option<String>) {
    let log_config_file = log_config_file.unwrap_or_else(|| {
        let mut tmp = std::env::current_exe().unwrap();
        // FIX simulating relative path where the executable will be :
        // let mut tmp= PathBuf::from(r"C:\Users\noahm\DocumentsNb\BA4\Course-Description-Automation\res\bin-converters\rust_para_convert-mdToPdf.exe");
        let config_path = pop_n_push_s(&mut tmp, 2, &[LOG_CONFIG_FILE_NAME]);
        config_path.to_str().unwrap().to_owned()
    });
    log4rs::init_file(log_config_file, Default::default()).unwrap();
}
