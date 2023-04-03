use std::{env, io};
use std::path::{PathBuf, Path};
use path_clean::PathClean;

use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{SysResult, HPROCESS, STARTUPINFO, prelude::kernel_Hprocess};

/// # Description
/// Creates a process using winsafe api. (safe wrapper around windows sdk api).
/// On Windows, Creating a new process always comes with the execution of some executable in a new
/// thread / in that child process (there is no "just" fork.)
/// That's why this function take an absolute_path to an executable
/// and the the arguments to pass to it (argv)
///
/// # Params
/// - `app_name`: Absolute path to an executable
/// - `command_line`: Argument to program (equivalent of `argv`)
///
/// # NB
/// This functions returns after having waited on the "child" process. 
/// (Although the wait is not mandatory to avoid zombies thanks to the winsafe api,
/// here we just want to wait for the completion of the job.)
///
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

fn test_get_resources_path() {
    let (pandoc_path, wk_path, md_path, templates_path) = get_resources_path();
    println!("pandoc_path:\n{:#?}, exists? {}", pandoc_path, Path::new(&pandoc_path).exists());

    println!("--------------------\n");
    println!("wk_path:\n{:#?}, exists? {}", wk_path, Path::new(&wk_path).exists());

    println!("--------------------\n");
    println!("md_path:\n{:#?}, exists? {}", md_path, Path::new(&md_path).exists());

    println!("--------------------\n");
    println!("templates_path:\n{:#?}, exists? {}", templates_path, Path::new(&templates_path).exists());

}


/// # Description
/// Calls pandoc cmd with `execvp` to convert the given markdown file according
/// to the predefined html template.
/// # Params
/// - `md_filename`: filename of a markdown document in `/res/md/` directory.
/// i.e. `desc-2022-11X001.md` for `/res/md/desc-2022-11X001.md`
/// - `pandoc_path`: Absolute path to the pandoc executable.
/// - `md_path`: Absolute path to the `/res/md` directory.
/// - `templates_path`: Absolute path to the `/res/templates` directory.
///
/// # NB
/// The output file is saved in `/res/templates/<md_filename>.html` (without the '.md' extension)
///
fn pandoc_fill_template(md_filename: &String, pandoc_path: &str, md_path: &str, templates_path: &str) {
    let template: String = templates_path.to_owned() + "\\desc-template.html";
    
    let md_name: &str = md_filename;
    let md_filepath: &String = &format!("{md_path}\\{md_name}");
    let out_html = templates_path.to_owned() + "\\" + &md_name.replace(".md", ".html");

    let cmd_line: &str = &format!("{md_filepath} -t html --template={template} -o {out_html}");
    dbg!(cmd_line);
    dbg!(md_name);
    execvp(pandoc_path, cmd_line);
}

pub fn main() {
    let args: Vec<String> = env::args().collect();
    // FIX: md input should be only a filename (of a file in res/md/)

    println!("--------------------\n\n");
    //test_get_resources_path();
    let (pandoc_path, wk_path, md_path, templates_path) = get_resources_path();
    //let (pandoc_path, _wk_path, md_path, templates_path) = (pandoc_path.as_str(), wk_path.as_str(), md_path.as_str(), templates_path); // constant strings
    pandoc_fill_template(&args[1], &pandoc_path, &md_path, &templates_path);

    println!("\n\n--------------------\nDONE")
}
