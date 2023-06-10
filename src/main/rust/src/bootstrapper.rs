#![allow(dead_code, unused_imports)]

use crate::net::{self, url_tail};
use crate::utils::{self, current_exe_path, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log};

use io::ErrorKind::Other;
use native_dialog::MessageType;
use rayon::iter::*;
use reqwest::Client;
use std::any::Any; //use std::env::temp_dir;
use std::error::Error;
use std::fs::{DirEntry, File, ReadDir};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::{exit, Command, ExitStatus, Output};
use std::{env, fs, io, panic, thread};

extern crate url;
use url::{ParseError, Url};

const REPO: &str =
    "https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master";

/// # Returns
/// Url to github repository (with `raw.githubusercontent` prepended to it)
/// to be able to directly download files from repo to bootstrap project installation
pub fn repo() -> Result<Url, url::ParseError> {
    Url::parse(REPO) //.expect("Github url repo should be parsable by url::parse")
}

/// Resolves given `rel_path` agains the url of the repo given by `repo()`
/// # Returns
/// new resolved url i.e. `repo()/rel_path`
fn rl(rel_path: String) -> Result<Url, url::ParseError> {
    repo()?.join(&rel_path)
}

/// Resolves (as string) given `rel_path` agains the url (as string) of the repo given by `repo()`
/// # Returns
/// new resolved url (as string) i.e. `repo()/rel_path`
fn rls(rel_path: String) -> String {
    format!("{REPO}/rel_path")
}

/// # Params
/// - `rel_path`: relative path to the root of the repo i.e. "`Course-Description-Automation/<branch>`"
/// - `parent_dir`: path to directory to download the file to
/// - `name`: optional name to rename the file. If `None`, => defaults to the name contained in
/// `rel_path`
fn dl_file(
    client: &Client,
    rel_path: String,
    parent_dir: PathBuf,
    name: Option<String>,
) -> Result<(), ParseError> {
    // let file_url = &rl(rel_path)?;
    let file_url = rls(rel_path);
    let file_name = match name {
        Some(val) => val,
        None => url_tail(rel_path),
    };
    let path = PathBuf::from("");
    net::download_file(client, file_url, &path);

    Ok(())
}

pub fn main() -> Result<(), url::ParseError> {
    let url = Url::parse("http://my.com/dir1/dir2/file.ext")?;
    let client = Client::new();

    Ok(())
}
