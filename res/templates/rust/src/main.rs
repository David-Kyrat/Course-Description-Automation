use std::env;
use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;

use winsafe::prelude::kernel_Hprocess;
use winsafe::STARTUPINFO;
use winsafe::{SysResult, HPROCESS};

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

pub fn create_fake_gcc() {
    let args: Vec<String> = env::args().collect();
    let app_name = "C:\\zig-win-0.11.0-dev\\zig.exe";
    let cmd_line = &args[1..].join(" ");
    let cmd_line = &format!("cc {cmd_line}");
    execvp(app_name, cmd_line);
}



pub fn main() {
    let args: Vec<String> = env::args().collect();
    //let app_name = &args[1];
    let app_name = "C:\\zig-win-0.11.0-dev\\zig.exe";
    let cmd_line = &args[1..].join(" ");
    let cmd_line = &format!("cc {cmd_line}");
    //let app_name = Some("C:/texlive/2022/bin/win32/pdftohtml.exe");
    //let app_name = "C:\\zig-win-0.11.0-dev\\zig.exe";
    //let cmd_line = "cc ";
    dbg!(cmd_line);
    execvp(app_name, cmd_line);
    //println!("\nDONE")
}
