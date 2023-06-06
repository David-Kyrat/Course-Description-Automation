use std::{
    env,
    ffi::{OsStr, OsString},
    path::{Path, PathBuf},
    process::Command,
};

pub fn add_to_path(to_add: PathBuf) -> Result<(), env::JoinPathsError> {
    let path = env::var_os("PATH").unwrap();
    let mut paths = env::split_paths(&path).collect::<Vec<_>>();
    paths.push(to_add);
    let new_path = env::join_paths(paths)?;
    env::set_var("PATH", &new_path);
    Ok(())
}
fn extract_std(out: Vec<u8>) -> String {
    String::from_utf8(out).expect("output didn't return a valid utf8 string")
}

fn main() {
    let _crt_path: OsString = OsString::from(env::current_dir().expect("cannot get current dir"));
    let to_add = "/Users/ekkemunz/Documents/.noah/cda/files/res/bin-converters/wkhtmltopdf";
    if let Err(cause) = add_to_path(PathBuf::from(to_add)) {
        eprintln!("cannot modify environment variable {:#?}", cause);
    }
    dbg!(env::var_os("PATH"));

    let out = Command::new("wkhtmltopdf")
        .arg("-h")
        .output()
        .expect("ls -la")
        .stdout;

    dbg!(extract_std(out));
}
