#![allow(dead_code, unused_imports)]

use crate::net::{self, async_runtime_wrap, rl, rls, url_tail, url_tail_s};
use crate::utils::{self, current_exe_path, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log};

use futures_util::{TryFutureExt, Future};
use futures_util::__private::async_await;
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

pub const REPO: &str =
    "https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master";

/// # Returns
/// Url to github repository (with `raw.githubusercontent` prepended to it)
/// to be able to directly download files from repo to bootstrap project installation
pub fn repo() -> Result<Url, url::ParseError> {
    Url::parse(REPO) //.expect("Github url repo should be parsable by url::parse")
}

/// # Params
/// - `rel_path`: relative path to the root of the repo i.e. "`Course-Description-Automation/<branch>`"
/// - `parent_dir`: path to directory to download the file to
/// - `name`: optional name to rename the file. If `None`, => defaults to the name contained in
/// `rel_path`
async fn dl_file(
    client: &Client,
    rel_path: String,
    parent_dir: PathBuf,
    name: Option<String>,
) -> Result<(), String> {
    // let file_url = &rl(rel_path)?;
    let file_url = rls(&rel_path);
    let file_name = match name {
        Some(val) => val,
        None => url_tail_s(&rel_path),
    };
    let file_path = parent_dir.join(file_name);
    net::download_file(client, file_url, &file_path).await?;
    Ok(())
}

/// # Params
/// - `rel_path`: relative path to the root of the repo i.e. "`Course-Description-Automation/<branch>`"
/// - `exact_path`: exact path to download the file to
/// `rel_path`
async fn dl_file_exact(
    client: &Client,
    rel_path: &String,
    parent_dir: PathBuf,
    name: Option<String>,
) -> Result<(), String> {
    // let file_url = &rl(rel_path)?;
    let file_url = rls(&rel_path);
    let file_name = match name {
        Some(val) => val,
        None => url_tail_s(&rel_path),
    };
    let file_path = parent_dir.join(file_name);
    net::download_file(client, file_url, &file_path).await?;
    Ok(())
}

/// # Params
///
/// - `rel_paths`: relative path (from the repo root) to the file to download.
/// these relative paths will also be used to infer the directory they must be in.
///
/// - `parent_dir`: path to the directoy that will be the "root" of all the downloaded content
/// the rest of the paths will be infered from the given urls (i.e. `rel_paths`)
/// and they will be created if they do not exists.
/// If not given, defaults to "`current_exe()`".
///
/// For exemple, for `rel_path: files/res/java/form.jar` and `parent_dir: None `.
/// The file will be downloaded into the (created) directory "current_exe_path/files/res/java" as "form.jar"
///
async fn dl_files(client: &Client, rel_paths: Vec<String>, parent_dir: Option<PathBuf>) -> Result<(), String> {
    let parent_dir: PathBuf = match parent_dir {
        Some(dir) => dir,
        None => {
            let mut path = current_exe_path();
            path.pop();
            path
        }
    };

    let rel_path = "";
            let dl_dir_path: PathBuf = parent_dir.join(&rel_path);
            match dl_file_exact(client, rel_path, dl_dir_path, None).await {
                Ok(()) => (),
                Err(cause) => ()
            };
    rel_paths.par_iter().for_each(|rel_path|{
            // let url = rl(rel_path)
            // Removes file name from rel_path to get the complete directory name
            // let path: PathBuf = parent_dir.join(rel_path.rsplit('/').skip(1).collect::<String>());
    });

    Ok(())
}

pub fn main() -> Result<(), String> {
    let client = Client::new();
    async_runtime_wrap(async {
        dl_file(
            &client,
            format!("files/res/{}", utils::LOG_CONFIG_FILE_NAME),
            std::env::current_dir().unwrap(),
            None,
        )
        .await
    })
}
