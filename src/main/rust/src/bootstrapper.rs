#![allow(dead_code, unused_imports)]

use crate::utils::{self, current_exe_path, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log};

use io::ErrorKind::Other;
use native_dialog::MessageType;
use rayon::iter::*;
use std::any::Any; //use std::env::temp_dir;
use std::error::Error;
use std::fs::{DirEntry, File, ReadDir};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::{exit, Command, ExitStatus, Output};
use std::{env, fs, io, panic, thread};

extern crate url;
use url::{Url, ParseError};

const REPO: &str = "https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master";


/// # Returns
/// Url to github repository (with `raw.githubusercontent` prepended to it) 
/// to be able to directly download files from repo to bootstrap project installation
pub fn repo() -> Result<Url, url::ParseError> {
    Url::parse(REPO) //.expect("Github url repo should be parsable by url::parse")
}

/// # Returns
/// last component of url i.e. from the last apperance of `/` until the end.
/// Useful to extract filenames from url
/// # Example
/// let url = Url::parse("http://stuff.org/some/long/path/foo.text").unwrap();
/// assert_eq!(url_tail(&url), "foo.text");
///
fn url_tail(url: &Url) -> String {
    url.path().rsplitn(2, '/').next().unwrap_or("").to_string()
}


/// Resolves given `rel_path` agains the url of the repo given by `repo()`
/// # Returns
/// new resolved url i.e. `repo()/rel_path`
fn rl(rel_path: String) -> Result<Url, url::ParseError> {
    repo()?.join(&rel_path)
}

/// # Params
/// - `rel_path`: relative path to the root of the repo i.e. "`Course-Description-Automation/<branch>`"
/// - `parent_dir`: path to directory to download the file to
/// - `name`: optional name to rename the file. If `None`, => defaults to the name contained in
/// `rel_path`
fn dl_file(rel_path: String, parent_dir: PathBuf, name: Option<String>) -> Result<(), ParseError> {
    let file_url = &rl(rel_path)?;
    let file_name = if let Some(value) = name { value } else { url_tail(file_url) };


    Ok(())
}

pub fn main() -> Result<(), url::ParseError> {
    let url = Url::parse("http://my.com/dir1/dir2/file.ext")?;
    dbg!(url_tail(&url));

    Ok(())
}
