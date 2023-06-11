#![allow(dead_code)]

use crate::net::{self, async_runtime_wrap, join_parallel, rl, rls, url_tail, url_tail_s};
use crate::utils::{self, current_exe_path, wrap_etos, RETRY_AMOUNT};
use crate::{abs_path_clean, fr, pop_n_push_s, unwrap_retry_or_log};

use fs::{DirEntry, File, ReadDir};
use path::{Path, PathBuf};
use process::{exit, Command, ExitStatus, Output};
use std::{self, env, fs, io, panic, path, process, thread};
use std::{
    any::Any,
    error::Error,
    io::{ErrorKind::Other, Write},
};

use rayon::iter::*;
use reqwest::Client;

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
/// - `parent_dir`: path to directory to download the file to. (`parent_dir` must point to an existing directory)
/// - `name`: optional name to rename the file. If `None`, => defaults to the name contained in
/// `rel_path`.
///
/// # Exemple
/// if we want to download `https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master/files/res/logging_config.yaml`
/// just enter `files/res/logging_config.yaml` as `rel_path` (also give in a `parent_dir` and leave name to `None`), and the function will
/// - prepend "`https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master/`"
/// to the url to download from (to target the correct file in the repo), (gives `https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master/files/res/yaml`)
/// - prepend `parent_dir` to download the file at the path
/// `<parent_dir>/files/res/logging_config.yaml`
async fn dl_file(
    client: &Client,
    rel_path: String,
    parent_dir: &PathBuf,
    name: Option<String>,
) -> Result<(), String> {
    // let file_url = &rl(rel_path)?;
    let file_url = rls(&rel_path); // prepend repo url
    let x = file_url.clone();
    let file_name = match name {
        Some(val) => val,
        None => url_tail_s(&rel_path),
    };
    let file_path = parent_dir.join(file_name); // prepend actual parent download path
    create_parent_dirs(&file_path).expect(&format!(
        "we should be able to create the parents of {}",
        file_path.display()
    ));
    net::download_file(client, file_url, &file_path).await?;

    eprintln!("Downloading {} to {}", x, &file_path.display());
    Ok(())
}

/// # Description
/// Creates all directory in the path of the parent of given `PathBuf`if they do point to an existing path.
/// (i.e. `mkdir -p "path/.."`)
fn create_parent_dirs(path: &PathBuf) -> Result<(), String> {
    wrap_etos(
        fs::create_dir_all(path.parent().unwrap()),
        &format!("could not create all dirs in {}/..", path.display()),
    )
}

async fn test_dl_config_log(client: &Client) -> Result<(), String> {
    dl_file(
        &client,
        format!("files/res/{}", utils::LOG_CONFIG_FILE_NAME),
        &std::env::current_dir().unwrap(),
        None,
    )
    .await
}
use lazy_static::lazy_static;

/// Making a struct to be able to keep the 2 variable below in a lazy static bloc
/// to be able to pass them to the async bloc in join_parallel below
struct ClientExt {
    pub client: Client,
    pub dl_parent_dir: PathBuf,
}

pub fn main() -> Result<(), String> {
    // let client: Client = Client::new();
    let resources_to_dl = vec![
        "files/res/logging_config.yaml",
        "files/res/abbrev.tsv",
        "files/res/app-info-logo.svg",
        "files/res/readme-example2.png",
        "files/res/cda-icon-mac.icns",
    ];
    lazy_static! {
        static ref CLIENT: ClientExt = ClientExt {
            client: Client::new(),
            dl_parent_dir: env::current_dir().unwrap()
        };
    }

    let _results: Vec<_> = async_runtime_wrap(async {
        // test_dl_config_log(&client).await
        join_parallel(resources_to_dl.into_iter().map(|rel_path| async {
            dl_file(
                &CLIENT.client,
                rel_path.to_string(),
                &CLIENT.dl_parent_dir,
                // env::current_dir().unwrap(),
                None,
            )
        }))
        .await
    });

    Ok(())
}

/* /// # Params
///
/// - `rel_paths`: relative path (from the repo root) to the file to download.
/// these relative paths will also be used to infer the directory they must be in.
///
/// - `parent_dir`: path to the directoy that will be the "root" of all the downloaded content
/// the rest of the paths will be infered from the given urls (i.e. `rel_paths`)
/// and they will be created if they do not exists.
/// If not given, defaults to "`current_exe()`"
///
/// For exemple, for `rel_path: files/res/java/form.jar` and `parent_dir: None `.
/// The file will be downloaded into the (created) directory "current_exe_path/files/res/java" as "form.jar"
///
async fn dl_files(
    client: &Client,
    rel_paths: Vec<String>,
    parent_dir: Option<PathBuf>,
) -> Result<(), String> {
    let parent_dir: PathBuf = match parent_dir {
        Some(dir) => dir,
        None => {
            let mut path = current_exe_path();
            path.pop();
            path
        }
    };

    /* match dl_file(client, rel_path, dl_dir_path, None).await {
        Ok(()) => (),
        Err(cause) => (),
    }; */
    rel_paths.par_iter().for_each(|rel_path| {
        // let url = rl(rel_path)
        // Removes file name from rel_path to get the complete directory name
        // let path: PathBuf = parent_dir.join(rel_path.rsplit('/').skip(1).collect::<String>());
    });

    Ok(())
} */

/* /// # Params
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
} */
