use crate::{
    fr,
    message_dialog::{quick_message_dialog, quick_yesno_dialog},
    utils::{abs_path_clean, current_exe_path, e_to_s, pop_n_push_s, wrap_etos},
};
use native_dialog::MessageType;

use std::{
    io,
    path::{Path, PathBuf},
    process::{Command, Output},
    thread,
};

use log::{error, warn};

fn symlink(link: &Path, target: &Path) -> Result<(), String> {
    #[cfg(unix)]
    use std::os::unix::fs::symlink as fs_symlink;
    #[cfg(windows)]
    use std::os::windows::fs::symlink_file as fs_symlink;

    wrap_etos(
        fs_symlink(link, target),
        &format!(
            "Cannot symlink {} to {} (target)",
            link.display(),
            target.display()
        ),
    )
}

fn get_java_paths() -> io::Result<(String, String, String)> {
    let pathbuf = current_exe_path();

    let java_bin_path = if cfg!(target_os = "windows") {
        &["jre-17-win", "bin", "java.exe"]
    } else if cfg!(target_os = "macos") {
        &["jre-17-mac", "bin", "java"]
    } else {
        &["jre-17-linux", "bin", "java"]
    };
    let javadir = pop_n_push_s(&pathbuf, 1, &["files", "res", "java"]);

    let (java_exe_path, jar_path) = (
        pop_n_push_s(&javadir, 0, java_bin_path),
        pop_n_push_s(&javadir, 0, &["fancyform.jar"]),
    );
    if !java_exe_path.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("java_exe_path {:#?} not found", (java_exe_path).display()),
        ));
    }
    if !jar_path.exists() {
        return Err(io::Error::new(
            io::ErrorKind::NotFound,
            format!("jar_path {:#?} not found", (jar_path).display()),
        ));
    }
    let (java_exe_path, jar_path, scala_jar_path) = (
        abs_path_clean(java_exe_path),
        abs_path_clean(jar_path),
        abs_path_clean(pop_n_push_s(
            &javadir,
            0,
            &["Course-Description-Automation.jar"],
        )),
    );

    warn!(
        "java_exe_path: {}\n gui_jar: {}\n scala_jar: {}\n",
        &java_exe_path, &jar_path, &scala_jar_path
    );
    Ok((java_exe_path, jar_path, scala_jar_path))
}

/// Computes the path of the file that should be at '/files/res/abbrev.tsv'
fn get_abbrev_file_path() -> String {
    let ab_fp = pop_n_push_s(&current_exe_path(), 1, &["files", "res", "abbrev.tsv"]);
    abs_path_clean(ab_fp)
}

/// # Description
/// Clean markdown forlder before, so that the generated pdfs are only
/// those the user asked for
///
/// # Errors
/// Will return an error if
/// - path doesn't exists
/// - path is not a dir
/// - user doesn't have permissions
fn clean_md_before(md_path: &PathBuf) -> io::Result<()> {
    if md_path.is_dir() {
        std::fs::remove_dir_all(md_path).ok();
    }
    std::fs::create_dir_all(md_path)
}

/// # Descriptionn
/// Takes in a byte vector and return the underlying represented utf8 string.
/// `Vec<u8>` is the type returned by `outuput.stdout` or `output.stderr`
/// # Param

/// - out: byte vector to convert
/// # Returns
/// Underlying string
/// # Panics
/// If the given byte vector is not a valid utf8 strings
pub fn extract_std(out: Vec<u8>) -> String {
    String::from_utf8(out).expect("output should return a valid utf8 string")
}

/// # Description
/// Entry point of the program (i.e. get user input to get what to do)
/// # Params
/// - `java_exe_path`: path to java executable for current platform
/// - `gui_jar_path`: path to the jar containing the gui
fn launch_gui(java_exe_path: &str, gui_jar_path: &str) -> io::Result<Output> {
    // let (java_exe_path, jar_path, _scala_jar_path) = get_java_paths()?;
    let abbrevfile_path = get_abbrev_file_path();
    Command::new(java_exe_path)
        .args(&["-jar", &gui_jar_path, &abbrevfile_path])
        .output()
}

/// # Desc
/// Launch main scala application, that will query the unige database
/// and generate the markdown
/// # Params
/// - `java_exe_path`: path to java executable for current platform
/// - `scala_jar_path`: path to the jar containing the main scala app
fn launch_main_scalapp(
    java_exe_path: &str,
    scala_jar_path: &str,
    args: &str,
) -> io::Result<Output> {
    Command::new(java_exe_path)
        .args(["-jar", scala_jar_path, args])
        .output()
}

use crate::{log_err, para_convert};

/// # Description
/// Wrapper function around the program to able to simply propagate any amount
/// of errors up to `main()` (with `?`), to be dealt with only once.
/// (the first if there is one will just stop the execution and go back to `main()` with the error message)
/// # Returns
/// Nothing or Error message
fn sub_main() -> Result<(), String> {
    fn err_fmter<T: std::fmt::Display>(add_msg: &str, cause: &T) -> String {
        format!("{} : {}", err_fmt(add_msg), cause)
    }
    fn err_fmt(add_msg: &str) -> String {
        format!("The following error happened:\nLauncher: {add_msg}")
    }

    let (java_exe_path, gui_jar_path, scala_jar_path) = get_java_paths().unwrap();
    let gui_out = launch_gui(&java_exe_path, &gui_jar_path);

    // logs and return (when necessary) the error formatted into a message that will be
    // displayed in a popup to the user.
    let gui_out = match gui_out {
        Ok(output) => {
            let status = output.status;
            if !status.success() {
                let out = output.clone();
                warn!(
                    "-------------\n Gui part ------------\nNon success status: {:#?}",
                    status
                );
                warn!(
                    "\n stdout: {}\n-------\n stderr: {}\n",
                    extract_std(out.stdout),
                    extract_std(out.stderr)
                );
            }
            Ok(output)
        }
        Err(error) => {
            error!(
                "-------------\n Gui part Ouput (Error) ------------\n{}\n",
                err_fmt(&e_to_s("Gui returned an error")(&error))
            );
            Err(err_fmter("Cannot launch Gui", &error))
        }
    }?;
    let gui_out = extract_std(gui_out.stdout); // extract std_out from `Output` object

    #[cfg(not(macos))]
    thread::spawn(|| quick_message_dialog("Generating", "Generating pdfs please wait...", None));
    // NOTE: using another thread to display a graphical popup crashes on macOs

    let md_path = pop_n_push_s(&current_exe_path(), 1, &["files", "res", "md"]);
    if let Err(cause) = clean_md_before(&md_path) {
        log_err!(
            cause,
            format!("Cannot clean/create md directory: {}", md_path.display())
        );
    };

    use std::time::Instant;
    let now = Instant::now();
    // generate markdown
    let main_out = launch_main_scalapp(&java_exe_path, &scala_jar_path, &gui_out);
    match main_out {
        Ok(output) => {
            let status = output.status;
            if !status.success() {
                let out = output; //.clone();
                warn!(
                    "-------------\n Scala part Ouput ------------\nNon success status: {:#?}",
                    status
                );
                let std_err = extract_std(out.stderr);
                warn!(
                    "\n stdout: {}\n-------\n stderr: {}\n",
                    extract_std(out.stdout),
                    &std_err
                );
                return Err(std_err); // scala part already formats message to display to user.
            }
        }
        Err(error) => {
            error!(
                "-------------\n Scala part Ouput (Error) ------------\n{}\n",
                err_fmt(&e_to_s("Scala app returned an error")(error))
            );
        }
    };
    para_convert::main().map_err(|_| err_fmt("Not all pdf could be generated. (para_convert)"))?;
    let elapsed = now.elapsed();
    warn!("Generated {gui_out} in: {:.2?}", elapsed);

    Ok(())
}

/// # Desc
/// Launcher for the whole project. There are several steps.
///
/// 1. Launch gui to ask for user input
/// 2) If user input is correct launch the "main" part (scala app that
/// will generate the markdown documents)
///     2. if its not, asks user if he wants to retry
/// 3. Launch conversion of markdown files to pdf
/// 4) Display message to inform of success / error of conversion to pdf
/// and asks to user whether he wants to retry
///
///
/// # Returns
/// `Ok(())` i.e. Nothing if success. The error of the function that failed otherwise.
pub fn main() {
    fn ok_popup() -> bool {
        let err_msg = "main could not display popup";
        quick_yesno_dialog(
            "Success",
            "PDF generation successful.\n Do you want to generate anything else?",
            None,
        )
        .expect(err_msg)
    }

    fn error_popup(message: String) -> bool {
        let err_msg = "main could not display popup";
        quick_yesno_dialog(
            "Error",
            &format!("{message}\n Do you want to retry?"),
            Some(MessageType::Error),
        )
        .expect(err_msg)
    }

    // On windows creating a symlink requires admin rights. Demanding them each time is not
    // something we want to.
    #[cfg(not(windows))]
    {
        let symlink_path = utils::rl_crt_exe("pdfs");
        if !symlink_path.exists() {
            let res = symlink(&symlink_path, &utils::rl_crt_exe("files/res/pdf"));
            if res.is_err() {
                error!("{}", res.unwrap_err());
            }
        }
    }

    let prog_result = sub_main();
    let retry = match prog_result {
        Ok(()) => ok_popup(), // Ok popup
        Err(message) => error_popup(message),
    };

    if retry {
        main()
    }
}
