#![allow(unused_imports)]

use lazy_static::lazy::*;
use path_clean::PathClean;
use std::borrow::Borrow;
use std::f32::consts::LOG2_E;
use std::io::{BufWriter, ErrorKind, IoSlice, Write};
use std::path::{Path, PathBuf};
use std::{env, fs, fs::*, io};
use std::fs::File;

use lazy_static::*;
use once_cell::sync;
use once_cell::sync::Lazy;

// static P: &Path = Path::new("");

/* lazy_static! {
    static P: &Path = Path::new("");
} */
//Path::new("");

/// # Description
/// Pops n times given path. and adds sequentially each one in `to_join`
/// # Params
/// - `path`: path to pop 
/// - `n`: Number of time to apply `path.pop()` (i.e. go to parent)
/// - `to_join`: slice of string to append to the path (i.e. `path.push(s) for each s in to_join`)
/// # Return
/// `path` after having applied
/// `for _ in 0..n { path.pop() }` and `for s in to_join { path.push(s) } `
pub fn pop_n_push_s(path: &PathBuf, n: u16, to_join : &[&str]) -> () {
    for _ in 0..n { path.pop(); }
    for s in to_join { path.push(s) }
}


pub fn lul() -> File {
    let mut tmp: PathBuf = Path::new( "C:\\Users\\noahm\\DocumentsNb\\BA4\\Course-Description-Automation\\res\\bin-converters",).to_path_buf(); 
    //path where the actual .exe will be, replace by std:env::current_exe():
    tmp.pop(); // /res
    tmp.push("log"); // /res/log
    tmp.push("rust-convert.log");
    dbg!(&tmp);
    let log_file_path: &PathBuf = tmp.borrow(); // immutable
    return File::create(log_file_path).expect("aakjsdasd");
}
static LOG_FILE: File = {
    let mut tmp: PathBuf = Path::new( "C:\\Users\\noahm\\DocumentsNb\\BA4\\Course-Description-Automation\\res\\bin-converters",).to_path_buf(); 
    //path where the actual .exe will be, replace by std:env::current_exe():

    // pop_n_push_s(tmp, 1, &["log", "rust-convert.log"]);

    let path = tmp;
    File::new("");
};

/* static LOG_FILE: Lazy<File> =
    Lazy::new(|| {
        //let mut tmp: PathBuf = env::current_exe().unwrap(); // /res/bin-converter
        let mut tmp: PathBuf = Path::new("C:\\Users\\noahm\\DocumentsNb\\BA4\\Course-Description-Automation\\res\\bin-converters").to_path_buf(); //path where the actual .exe will be
        tmp.pop(); // /res
        tmp.push("log"); // /res/log
        tmp.push("rust-convert.log");
        let log_file_path: &PathBuf = tmp.borrow(); // immutable
        File::create(log_file_path).ok().unwrap() // already remove path if exists
                                                  //log_file_path.to_path_buf()
    });
*/
// static P2: &Path = OnceCell::new();
// static P = Lazy::new(|| Path::new("/"));

pub fn absolute_path(path: impl AsRef<Path>) -> io::Result<PathBuf> {
    let path = path.as_ref();
    // let p = unsync::

    let absolute_path = if path.is_absolute() {
        path.to_path_buf()
    } else {
        env::current_dir()?.join(path)
    }
    .clean();
    Ok(absolute_path)
}

static WEIRD_PATTERN: &str = "\\\\?\\";

pub fn abs_path_clean(path: impl AsRef<Path>) -> String {
    let path = absolute_path(path);
    path.expect(&format!("in abs_path_clean"))
        .to_str()
        .unwrap()
        .replace(WEIRD_PATTERN, "")
}

/* pub fn get_log_file2() -> Option<&'static std::fs::File> {
    match Lazy::get(&LOG_FILE) {
        Some(log_file) => Some(&*log_file),
        None => Lazy::get(&LOG_FILE), // if lazy hasn't been evaluated yet, evaluates it
    }
}

pub fn get_log_file() -> &'static File {
    //let x:Lazy<File> = (*LOG_FILE);
    match Lazy::get(&LOG_FILE) {
        Some(log_file) => &*log_file,
        None => Lazy::get(&LOG_FILE).unwrap(), // if lazy hasn't been evaluated yet, evaluates it
    }
} */

pub fn write_to_log(msg: &str) -> io::Result<()> {
    let mut bw = BufWriter::new(get_log_file());
    let res = bw.write_all(msg.as_bytes());
    if res.is_err() {
        return Err(res.unwrap_err());
    }
    Ok(())
}

pub fn write_vec_to_log(msgs: Vec<&str>) -> io::Result<()> {
    let mut bw = BufWriter::new(get_log_file());
    let res = bw.write_vectored(
        msgs.iter()
            .map(|s| IoSlice::new(s.as_bytes()))
            .collect::<Vec<IoSlice>>()
            .as_slice(),
    );
    if res.is_err() {
        return Err(res.unwrap_err());
    }
    Ok(())
}
