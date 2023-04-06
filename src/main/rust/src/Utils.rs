use lazy_static::lazy_static;
use path_clean::PathClean;
use std::ffi::OsString;
use std::fs::File;
use std::path::{Path, PathBuf};
use std::{env, io::{self, Write}};


//OnceCell::new().get_or_init(|| File::create(get_log_file()).expect("Could not create log file."));

//pub static LOG_FILE: &File = OnceCell::new().get_or_init(|| File::create(get_log_file()).expect("Could not create log file."));
// File::create(get_log_file()).expect("Could not create log file.");
//pop_n_push_s(env::current_exe(), 1, &["log", "rust-convert.log"]));
//static LOG_FILE: File = File::create("").expect("Could not create log file.");

/// # Description
/// Pops n times given path. and adds sequentially each one in `to_join`
/// # Params
/// - `path`: path to pop
/// - `n`: Number of time to apply `path.pop()` (i.e. go to parent)
/// - `to_join`: slice of string to append to the path (i.e. `path.push(s) for each s in to_join`)
/// # Return
/// `path` after having applied
/// `for _ in 0..n { path.pop() }` and `for s in to_join { path.push(s) } `
pub fn pop_n_push_s(path: &mut PathBuf, n: u16, to_join: &[&str]) -> PathBuf {
    for _ in 0..n { path.pop(); }
    for s in to_join { path.push(s) }
    path.clone()

}

fn get_log_file() -> OsString {
    let mut exe_dir: PathBuf = Path::new("C:\\Users\\noahm\\DocumentsNb\\BA4\\Course-Description-Automation\\res\\bin-converters").to_path_buf();
    //path where the actual .exe will be, replace by std:env::current_exe():
    pop_n_push_s(&mut exe_dir, 1, &["log", "rust-convert.log"]);
    //&exe_dir.into_os_string();
    env::current_exe().unwrap().into_os_string()
}

pub fn absolute_path(path: impl AsRef<Path>) -> io::Result<PathBuf> {
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

pub fn abs_path_clean(path: impl AsRef<Path>) -> String {
    let path = absolute_path(path);
    path.expect(&format!("in abs_path_clean"))
        .to_str()
        .unwrap()
        .replace(WEIRD_PATTERN, "")
}

/* pub fn write_to_log(msg: &str) -> io::Result<()> {
    let res = LOG_FILE.try_clone().expect("msg").write_all(msg.as_bytes()); // BufWriter::new(LOG_FILE);
    if res.is_err() {
        return Err(res.unwrap_err());
    }
    Ok(())
} */

/* pub fn write_vec_to_log(msgs: Vec<&str>) -> io::Result<()> {
    let res =  LOG_FILE.try_clone().expect("msg").write_vectored(
        msgs.iter()
            .map(|s| IoSlice::new(s.as_bytes()))
            .collect::<Vec<IoSlice>>()
            .as_slice(),
    );
    if res.is_err() {
        return Err(res.unwrap_err());
    }
    Ok(())
} */
