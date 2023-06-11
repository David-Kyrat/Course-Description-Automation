#![allow(dead_code)]

use crate::message_dialog;
use crate::net::{async_runtime_wrap, download_file, join_parallel, rls, url_tail_s};
use crate::{
    fr,
    utils::{self, wrap_etos},
};
use std::fs::read_to_string;
use std::{
    self, env, fs,
    path::{Path, PathBuf},
};

use native_dialog::MessageType;
use reqwest::Client;

extern crate url;
use url::Url;

pub const REPO: &str =
    "https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master";

/// # Returns
/// Url to github repository (with `raw.githubusercontent` prepended to it)
/// to be able to directly download files from repo to bootstrap project installation
pub fn repo() -> Result<Url, url::ParseError> {
    Url::parse(REPO) //.expect("Github url repo should be parsable by url::parse")
}

/// # Params
/// - `rel_path`: relative path to the root of the repo i.e. "`Course-Description-Automation/<branch>`" (and also download path relative to `parent_dir` )
/// - `parent_dir`: path to directory to download the file to. (`parent_dir` must point to an existing directory)
///
/// # Exemple
/// if we want to download `https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master/files/res/logging_config.yaml`
/// just enter `files/res/logging_config.yaml` as `rel_path` (also give in a `parent_dir`), and the function will
/// - prepend "`https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master/`"
/// to the url to download from (to target the correct file in the repo), (gives `https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master/files/res/yaml`)
/// - prepend `parent_dir` to download the file at the path
/// `<parent_dir>/files/res/logging_config.yaml`
async fn dl_file(client: &Client, rel_path: String, parent_dir: &Path) -> Result<(), String> {
    // let file_url = &rl(rel_path)?;
    let file_url = rls(&rel_path); // prepend repo url
    let _x = file_url.clone();
    let file_path = parent_dir.join(rel_path); // prepend actual parent download path
    create_parent_dirs(&file_path).unwrap_or_else(|_| {
        eprintln!(
            "we should be able to create the parents of {}",
            file_path.display()
        )
    });
    download_file(client, file_url, &file_path).await?;

    /* eprintln!(
        "\nDownloading \n\"{}\" \nto \"{}\"",
        x,
        &file_path.display()
    ); */
    Ok(())
}

/// # Description
/// Creates all directory in the path of the parent of given `PathBuf`if they do point to an existing path.
/// (i.e. `mkdir -p "path/.."`)
fn create_parent_dirs(path: &Path) -> Result<(), String> {
    wrap_etos(
        fs::create_dir_all(path.parent().unwrap()),
        &fr!(format!(
            "could not create all dirs in {}/..",
            path.display()
        )),
    )
}

async fn test_dl_config_log(client: &Client) -> Result<(), String> {
    dl_file(
        client,
        format!("files/res/{}", utils::LOG_CONFIG_FILE_NAME),
        &std::env::current_dir().unwrap(),
    )
    .await
}
use lazy_static::lazy_static;

/// Making a struct to be able to keep the 2 variable below in a lazy static bloc
/// to be able to pass them to the async bloc in join_parallel below
struct ClientExt {
    client: Client,
    dl_parent_dir: PathBuf,
}

/// # Returns
/// Vector containing trimmed lines of file at `to_dl_filepath`
fn get_resources_to_dl(to_dl_filepath: &Path) -> Result<Vec<String>, String> {
    Ok(wrap_etos(
        read_to_string(to_dl_filepath).map(|content| content.trim().to_string()),
        &fr!(format!(
            "resources_to_dl: cannot read file containing what to download. Path:{}",
            to_dl_filepath.display()
        )),
    )?
    .split('\n')
    .map(|line| line.trim().to_string())
    .collect::<Vec<String>>())
}

pub fn main() -> Result<(), String> {
    // let client: Client = Client::new();
    /* let resources_to_dl = vec![
        "files/res/logging_config.yaml",
        "files/res/abbrev.tsv",
        "files/res/app-info-logo.svg",
        "files/res/readme-example2.png",
        "files/res/cda-icon-mac.icns",
    ]; */
    let resources_to_dl: Vec<String> = get_resources_to_dl(&PathBuf::from("to_dl.txt"))?;
    // eprintln!("{:#?}", resources_to_dl);
    lazy_static! {
        static ref CLIENT: ClientExt = ClientExt {
            client: Client::new(),
            dl_parent_dir: env::current_dir().unwrap()
        };
    }

    // FIXME: If url is not found doesnt return an error
    // it will just download a file with as only content "404: Not Found"
    //
    // FIXME: Pandoc and jdk are not on github, find solution

    message_dialog::quick_message_dialog("Press ok to start downloading.", "The program will be downloading itself.\n Please wait and do not close the program or your internet connection during the process.\n\nPress ok to start downloading.", Some(MessageType::Warning)).ok();

    let results: Vec<Result<(), String>> = async_runtime_wrap(
        //async {
        // test_dl_config_log(&client).await
        join_parallel(resources_to_dl.into_iter().map(|rel_path| async move {
            dl_file(&CLIENT.client, rel_path.to_string(), &CLIENT.dl_parent_dir).await
            // .map_err(e_to_s("cannot"))
        })), // .await
    );
    let mut is_err: bool = false;
    let err_msg: String = results
        .iter()
        .filter(|res| res.is_err())
        .map(|res| {
            if !is_err {
                is_err = true;
            }
            res.as_ref().err().unwrap().to_string() + "\n\n"
        })
        .collect();

    if is_err {
        Err(err_msg)
    } else {
        Ok(())
    }
}

// ------------------------------------------------
// ------------------- UNUSED ---------------------
// ------------------------------------------------

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
