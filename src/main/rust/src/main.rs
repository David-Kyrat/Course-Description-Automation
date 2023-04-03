use std::{env, io};
use std::path::{PathBuf, Path};
use path_clean::PathClean;

use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{SysResult, HPROCESS, STARTUPINFO, prelude::kernel_Hprocess};


pub fn execvp(app_name: &str, command_line: &str) {
    let mut si: STARTUPINFO = STARTUPINFO::default();
    let app_name_opt = Some(app_name);
    let command_line: &str = &format!("{app_name} {command_line}"); //append name of program
    let cmd_line_opt = Some(command_line);
    // first word before space in command line should be app_name
    // (I think its ignored either way if app_name is not None because its argv[0])
    let close_handle_res: SysResult<CloseHandlePiGuard> = HPROCESS::CreateProcess(
        app_name_opt,
        cmd_line_opt,
        None,
        None,
        true,
        CREATE::NO_WINDOW | CREATE::INHERIT_PARENT_AFFINITY,
        None, //inherits
        None, // inherits
        &mut si,
    );
    //println!("before wait");
    let close_handle = close_handle_res.expect(&format!(
        "could not start process {app_name} {command_line}"
    ));

    let _res = HPROCESS::WaitForSingleObject(&close_handle.hProcess, Some(u32::MAX))
        .expect("could not wait on child");
    //println!("after wait");
}

pub fn test() {
    let args: Vec<String> = env::args().collect();
    //let app_name = &args[1];
    let mut exe_path: PathBuf = env::current_exe().expect("Could not get executable path");
    exe_path.pop();
    exe_path.pop();
    let pandoc_path: &_ = &exe_path;

    let app_name: &str = pandoc_path.to_str().expect("cannot parse path");
    let inp_md = &args[1];
    let out_html: &str = "";
    let template = "../../desc-template.html";
    // let cmd_line = &args[1..].join(" ");
    // let cmd_line = &format!("cc {cmd_line}");
    let cmd_line: &str = &format!("{inp_md} -t html --template={template} -o {out_html}");
    dbg!(cmd_line);

    execvp(app_name, cmd_line);
}

// use path_clean::clean;

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

fn abs_path_clean(path: impl AsRef<Path>) -> String {
    let path = absolute_path(path);
    path.expect(&format!("in abs_path_clean"))
        .to_str()
        .unwrap()
        .replace(WEIRD_PATTERN, "")
}

/// # Description
///
/// Return a 4-tuple containing the paths to the executables
/// of pandoc and wkhtmltopdf, and the paths to the markdown 
/// and templates resource directory ('res/md' and 'res/templates')
/// 
/// # Returns
///
/// (pandoc_path, wkhtmltopdf_path, md_path, templates_path)
fn get_resources_path() -> (String, String, String, String) {
    let mut rustdir_path: PathBuf = env::current_exe().unwrap();
    for _ in 0..3 { rustdir_path.pop(); }

    let mut res_path: PathBuf = rustdir_path.clone();
    for _ in 0..3 { res_path.pop(); }
    res_path.push("res");

    let res_path_borrowed: &str = &abs_path_clean(res_path);
    let exe_paths_borrowed: &str = &abs_path_clean(rustdir_path);
    (
        exe_paths_borrowed.to_owned() + "\\pandoc.exe",
        exe_paths_borrowed.to_owned() + "\\wkhtmltopdf.exe",
        res_path_borrowed.to_owned() + "\\md" ,
        res_path_borrowed.to_owned() + "\\templates"
    )
}

pub fn main() {
    println!("--------------------\n\n");
    let (pandoc_path, wk_path, md_path, templates_path) = get_resources_path();
    println!("pandoc_path:\n{:#?}, exists? {}", pandoc_path, Path::new(&pandoc_path).exists());

    println!("--------------------\n");
    println!("wk_path:\n{:#?}, exists? {}", wk_path, Path::new(&wk_path).exists());

    println!("--------------------\n");
    println!("md_path:\n{:#?}, exists? {}", md_path, Path::new(&md_path).exists());

    println!("--------------------\n");
    println!("templates_path:\n{:#?}, exists? {}", templates_path, Path::new(&templates_path).exists());


    let _msg = "could not resolve path";
    println!("\n\n--------------------\nDONE")
}
