#![allow(unused_imports)]

use path_clean::PathClean;
use std::borrow::Borrow;
use std::io::{BufWriter, ErrorKind, IoSlice, Write};
use std::path::{Path, PathBuf};
use std::{env, fs, fs::*, io};

use lazy_static::lazy::*;
use lazy_static::*;
use once_cell::sync;
use once_cell::sync::Lazy;

// static P: &Path = Path::new("");

/* lazy_static! {
    static P: &Path = Path::new("");
} */
//Path::new("");


pub fn lul() -> File {
    let mut tmp: PathBuf = Path::new("C:\\Users\\noahm\\DocumentsNb\\BA4\\Course-Description-Automation\\res\\bin-converters").to_path_buf(); //path where the actual .exe will be
    tmp.pop(); // /res
    tmp.push("log"); // /res/log
    tmp.push("rust-convert.log");
    dbg!(&tmp);
    let log_file_path: &PathBuf = tmp.borrow(); // immutable
    return File::create(log_file_path).expect("aakjsdasd");
}

static LOG_FILE: Lazy<File> = Lazy::new(|| 
lul()
);

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

fn get_log_file() -> &'static File {
    /* match Lazy::get(&LOG_FILE) {
        Some(log_file) => log_file,
        None => Lazy::get(&LOG_FILE).unwrap(), // if lazy hasn't been evaluated yet, evaluates it
                                               // and return its value
    } */
    &lul()
}

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
