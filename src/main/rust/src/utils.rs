use chrono::{FixedOffset, Utc};
use path_clean::PathClean;

use log4rs;
use std::env::current_dir;
use std::error::Error;
use std::fmt::Debug;
use std::path::{Path, PathBuf};
use std::{env, io};
pub const RETRY_AMOUNT: u8 = 0;
pub static LOG_CONFIG_FILE_NAME: &str = "logging_config.yaml";

///////////////////////////////////////////
/// NOTE: ------------- MACROS ------------
///////////////////////////////////////////

/// Formats error, with line and file in msg
#[macro_export]
macro_rules! fr {
    ($msg: expr) => {
        format!("{}.\n\t Line {}, File '{}'.\n", $msg, line!(), file!())
    };
}

#[macro_export]
macro_rules! log_err {
    ( $err:expr  $(, $msg:expr) ? ) => {
        error!("{}\n\t{:?}{}.", $( $msg.to_owned() + )? "", $err, fr!(""));
    }
}

#[macro_export]
/// # Description
/// If given `Result<_,_>` is an error. (`is_err() == true`)
/// panics and logs it with the `error!` macro from `log4rs`
/// otherwise return unwrwapped value.  
///
/// # Params
/// - `fun_res` : a `Result<_,_>` to check
/// - `msg` : an optional error messag to add while logging
///
/// # Returns
/// Panics if given `Result` is an error otherwise return unwrapped value.
macro_rules! unwrap_or_log {
    ( $fun_res:expr  $(, $msg:expr) ? ) => {
        if let Ok(res) = $fun_res { res }
        else {
            log_err!($fun_res.err().unwrap(), $( $msg.to_owned() )?);
            panic!()
        }
    }
}

#[macro_export]
/// # Description
/// If given `Result<_,_>` is an error. (`is_err() == true`)
/// `return` that error and logs it with the `error!` macro from `log4rs`
/// otherwise do nothing.  
/// Used to ensure that a call to `unwrap()` will never `panic`.
///
/// # Params
/// - `fun_res` : a `Result<_,_>` to check
/// - `msg` : an optional error messag to add while logging
///
/// # Returns
/// `Err(fun_res.unwrap_err)` if given `Result` is an error otherwise nothing.
macro_rules! log_if_err{
    ( $fun_res:expr  $(, $msg:expr) ? ) => {
        if $fun_res.is_err() {
            let err = $fun_res.unwrap_err();
            error!("{}\n\t{:?}{}.", $( $msg.to_owned() + )? "", err, fr!(""));
            return Err(err);
        } else { return Ok(()) }
    }
}

#[macro_export]
/// Does the same as `unwrap_or_log`
/// but instead retries `RETRY_AMOUNT` times
/// before returning an error and logging it
/// # Params
/// - `$fun_res`: a `Result<_, _>` which is the return value of calling `$fun`
/// - `$fun`: the function that returned `$fun_res`
/// - `$msg`: (optional) message to give to the logger if `$fun_res`.`is_err()`. `Must be wrapped in a block!` i.e. ` { "..." } `
/// - `args`: (optionnal only if function doesn't require arguments) arguments of the function separated by a comma
macro_rules! unwrap_retry_or_log {
    ( $fun_res:expr, $fun: ident, $msg:expr  $(, $args:expr)* ) => {
        {
            let mut r = 1;
            let x =  $fun( $($args),* );
            while x.is_err() && r < RETRY_AMOUNT {
                let _x = $fun( $($args),* );
                r += 1;
            }
            if r >= RETRY_AMOUNT {
                let err = x.unwrap_err();
                error!("{}\n\t{:?}{}.",$msg, err, fr!(""));
                return Err(err);
            }
            x
        }
    }
}

/// NOTE: ------------- END MACROS ------------

/// # Return
/// Formatted Swiss dateTime as a string
pub fn now() -> String {
    let swiss_offset = FixedOffset::east_opt(2 * 3600).unwrap(); // Switzerland offset is +02:00
    let current_time = Utc::now().with_timezone(&swiss_offset);
    let now = current_time.format("%Y-%m-%d %H:%M:%S");
    now.to_string()
}

/// # Description
/// Pops n times given path. and adds sequentially each one in `to_join`
/// # Params
/// - `path`: path to pop
/// - `n`: Number of time to apply `path.pop()` (i.e. go to parent)
/// - `to_join`: slice of string to append to the path (i.e. `path.push(s) for each s in to_join`)
/// # Return
/// `path` after having applied
/// `for _ in 0..n { path.pop() }` and `for s in to_join { path.push(s) } `
pub fn pop_n_push_s(path: &Path, n: u16, to_join: &[&str]) -> PathBuf {
    let mut path = path.to_path_buf(); // copy to avoid modifying argument
    for _ in 0..n {
        path.pop();
    }
    for s in to_join {
        path.push(s)
    }
    path.clone()
}

pub fn absolute_path<A: AsRef<Path>>(path: A) -> io::Result<PathBuf> {
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

pub fn abs_path_clean<P: AsRef<Path> + Debug>(path: P) -> String {
    let clean_path = absolute_path(&path);
    clean_path
        .expect(&format!(
            "abs_path_clean: absolute_path({:?}) should not fail",
            path
        ))
        .to_str()
        .unwrap()
        .replace(WEIRD_PATTERN, "")
}

/// # Description
/// Wrapper around `std::env::current_exe().expect(...)`
/// particularly useful when debugging and we have to simulate a relative file path to have
/// we can just modify the return by this function instead of doing in 10 diffferent file each
/// time.
///
/// # Return
/// `std::env::current_exe().expect(...)` i.e. `PathBuf` to the full filesystem path of the current running executable.
pub fn current_exe_path() -> PathBuf {
    let path = std::env::current_exe().expect("could not get path of current executable");
    // FIX: comment below for release
    //
    // std::env::set_current_dir(&std::path::PathBuf::from(r"C:/Users/noahm/DocumentsNb/BA4/cda-priv2/")).unwrap();
    // let path = PathBuf::from( r"C:/Users/noahm/DocumentsNb/BA4/cda-priv2/course-description-automation.exe",);
    path
}

pub fn rl_crt_dir(path: &str) -> Result<PathBuf, String> {
    let new_path = wrap_etos(current_dir(), "current dir should work")?;
    Ok(new_path.join(path))
}

/// # Resolve path relative to directory of current executable
/// # Return
/// "`<parent of current_exe_path>`/path"
pub fn rl_crt_exe(path: &str) -> PathBuf {
    let new_path = current_exe_path();
    new_path.parent().unwrap().join(path)
}

/// Function that takes in a string (message) and return function that can be passed
/// to `map_err` to convert the result into a `Result<_, String>`
/// # Returns
/// A function that returns `format!("{additional_msg} : {:?}", err)`
/// i.e. something that can be passed to `map_err`
pub fn e_to_s<E: Error>(additional_msg: &str) -> impl Fn(E) -> String + '_ {
    move |err: E| {
        fr!(format!(
            "{additional_msg}:  {:?}       {}. Source: {:?}",
            err,
            err,
            err.source()
        ))
    }
}

/// Wraps a call to something that returns a result and converts its `Result<T, _>` into a
/// `Result<T, String>` (i.e. calls `e_to_s` on it)
pub fn wrap_etos<T, E: Error>(res: Result<T, E>, message: &str) -> Result<T, String> {
    res.map_err(e_to_s(message))
}

/// # Description
/// function to call initiliaze logging lib, and tell it
/// to log the config file call `log_config_file`
/// defaults to the static variable of the same name if given a `None`
pub fn init_log4rs(log_config_file: Option<String>) {
    let log_config_file = log_config_file.unwrap_or_else(|| {
        let config_path = pop_n_push_s(
            &current_exe_path(),
            1,
            &["files", "res", LOG_CONFIG_FILE_NAME],
        );
        config_path.to_str().unwrap().to_owned()
    });
    // dbg!(&log_config_file);
    log4rs::init_file(log_config_file, Default::default()).unwrap();
}

/// # Description
/// Same as `init_log4rs` but use the log file located at `src/main/rust/` (i.e. in develepment dir) instead of the actual one
pub fn init_log4rs_debug() {
    log4rs::init_file("logging_config.yaml", Default::default()).expect("Cannot find log file");
}
