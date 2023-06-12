use std::{
    self,
    cmp::min,
    env,
    fs::{self, File},
    io::{self, Seek, Write},
    path::{self, PathBuf},
    result::Result,
};

use crate::utils::wrap_etos;
use crate::{
    fr, {repo, REPO},
};
use futures_util::{future, Future, StreamExt};
use reqwest::{Client, IntoUrl, Response};
use url::Url;

/// Resolves given `rel_path` agains the url of the repo given by `repo()`
/// # Returns
/// new resolved url i.e. `repo()/rel_path`
pub fn rl(rel_path: String) -> Result<Url, url::ParseError> {
    repo()?.join(&rel_path)
}

/// Resolves (as string) given `rel_path` agains the url (as string) of the repo given by `repo()`
/// # Returns
/// new resolved url (as string) i.e. `repo()/rel_path`
pub fn rls(rel_path: &str) -> String {
    format!("{REPO}/{rel_path}")
}

/// # Returns
/// last component of url i.e. from the last apperance of `/` until the end.
/// Useful to extract filenames from url
/// # Example
/// let url = Url::parse("http://stuff.org/some/long/path/foo.text").unwrap();
/// assert_eq!(url_tail(&url), "foo.text");
pub fn url_tail(url: String) -> String {
    url.rsplit('/').next().unwrap_or("").to_string()
}

/// # Returns
/// last component of url i.e. from the last apperance of `/` until the end.
/// Useful to extract filenames from url
/// # Example
/// let url = Url::parse("http://stuff.org/some/long/path/foo.text").unwrap();
/// assert_eq!(url_tail(&url), "foo.text");
pub fn url_tail_s(url: &str) -> String {
    url.rsplit('/').next().unwrap_or("").to_string()
}

/// # Description
/// Asynchronously download file  at given `url` and save it at given `path`
pub async fn download_file<U: IntoUrl + std::fmt::Display>(
    client: &Client,
    url: U,
    path: &PathBuf,
) -> Result<(), String> {
    let res: Response = wrap_etos(client.get(url).send().await, &fr!("Failed to GET"))?;
    /* let total_size = res
        .content_length()
        .ok_or(fr!("Failed to get content length")); */

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
            &fr!(format!("Failed to create file '{}'", path.display())),
        )?
    }

    while let Some(item) = stream.next().await {
        let chunk = item.or(Err(("Error while downloading file").to_string()))?;
        wrap_etos(
            file.write_all(&chunk),
            &fr!("dl file: error in while, writting to file."),
        )?;

        /* file.write_all(&chunk)
        .map_err(e_to_s("dl file: error in while, writting to file."))?; */

        let new = downloaded + (chunk.len() as u64);
        downloaded = new;
    }
    Ok(())
}

/// # Returns
/// "`$CWD/childPath`" as a `PathBuf`. Where `$CWD` is `std::env::current_directory()`
/* fn resolve_from_cwd(child_path: &str) -> Result<PathBuf, String> {
    Ok(std::env::current_dir()
        .map_err(|cause| format!("resolve_from_cwd: cannot get cwd. {:?}", cause))?
        .join(child_path))
} */

pub async fn join_parallel<T: Send + 'static>(
    futs: impl IntoIterator<Item = impl Future<Output = T> + Send + 'static>,
) -> Vec<T> {
    let tasks: Vec<_> = futs.into_iter().map(tokio::spawn).collect();
    future::join_all(tasks)
        .await
        .into_iter()
        .map(Result::unwrap)
        .collect()
}

/// # Description
/// Takes a `Future` as argument, runs it in async block in a new built tokio runtime with
/// `block_on(future)`.
///
/// Must be called the least amount of time possible since it creates a new `Tokio new_multi_thread runtime` each
/// time and I have no clue what that actually means (aside from the fact that its multi-threaded)
/// # Returns
/// Future output
/// # Example
/// `async_runtime_wrap(async { ... } )`
pub fn async_runtime_wrap<F: Future>(future: F) -> <F as Future>::Output {
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .unwrap()
        .block_on(future)
}

pub fn main() -> Result<(), String> {
    let client = Client::new();
    let url = std::env::args()
        .last()
        .ok_or_else(|| ("usage: rget <url>".to_string()))?;
    let mut name = wrap_etos(env::current_dir(), "current_dir() should not fail")?;
    name.push(url_tail_s(&url));

    // println!("\nDownloading \"{name}\"\nfrom \"{url}\"");
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .unwrap()
        .block_on(async { download_file(&client, url, &name).await })
}
