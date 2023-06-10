use futures_util::StreamExt;
use reqwest::{Client, IntoUrl, Response};
use std::cmp::min;
use std::fs::{self, File};
use std::io::{self, Seek, Write};
use std::path::{self, PathBuf};
use std::result::Result;

use crate::utils::wrap_etos;


/// # Returns
/// last component of url i.e. from the last apperance of `/` until the end.
/// Useful to extract filenames from url
/// # Example
/// let url = Url::parse("http://stuff.org/some/long/path/foo.text").unwrap();
/// assert_eq!(url_tail(&url), "foo.text");
pub fn url_tail(url: String) -> String {
    url.rsplitn(2, '/').next().unwrap_or("").to_string()
}

/// # Returns
/// last component of url i.e. from the last apperance of `/` until the end.
/// Useful to extract filenames from url
/// # Example
/// let url = Url::parse("http://stuff.org/some/long/path/foo.text").unwrap();
/// assert_eq!(url_tail(&url), "foo.text");
pub fn url_tail_s(url: &str) -> String {
    url.rsplitn(2, '/').next().unwrap_or("").to_string()
}

/// # Description
/// Asynchronously download file  at given `url` and save it at given `path`
pub async fn download_file<U: IntoUrl + std::fmt::Display>(
    client: &Client,
    url: U,
    path: &PathBuf,
) -> Result<(), String> {
    let res: Response = wrap_etos(client.get(url).send().await, "Failed to GET")?;
    let total_size = res
        .content_length()
        .ok_or(format!("Failed to get content length"))?;

    let mut file;
    let mut downloaded: u64 = 0;
    let mut stream = res.bytes_stream();

    if path::Path::new(&path).exists() {
        file = fs::OpenOptions::new()
            .read(true)
            .append(true)
            .open(path)
            .unwrap();

        let file_size = fs::metadata(path).unwrap().len();
        file.seek(io::SeekFrom::Start(file_size)).unwrap();
        downloaded = file_size;
    } else {
        file = wrap_etos(
            File::create(path),
            &format!("Failed to create file '{:?}'", path),
        )?
    }

    while let Some(item) = stream.next().await {
        let chunk = item.or(Err(format!("Error while downloading file")))?;
        wrap_etos(
            file.write_all(&chunk),
            "dl file: error in while, writting to file.",
        )?;

        /* file.write_all(&chunk)
        .map_err(e_to_s("dl file: error in while, writting to file."))?; */

        let new = min(downloaded + (chunk.len() as u64), total_size);
        downloaded = new;
    }
    return Ok(());
}


/// # Returns
/// "`$CWD/childPath`" as a `PathBuf`. Where `$CWD` is `std::env::current_directory()`
/* fn resolve_from_cwd(child_path: &str) -> Result<PathBuf, String> {
    Ok(std::env::current_dir()
        .map_err(|cause| format!("resolve_from_cwd: cannot get cwd. {:?}", cause))?
        .join(child_path))
} */

// fn main() {
pub fn main() -> Result<(), String> {
    let client = Client::new();
    let url = std::env::args()
        .last()
        .ok_or_else(|| ("usage: rget <url>".to_string()))?;
    let name = url_tail_s(&url);
    // println!("\nDownloading \"{name}\"\nfrom \"{url}\"");
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .unwrap()
        .block_on(async { download_file(&client, url, &name).await })
}
