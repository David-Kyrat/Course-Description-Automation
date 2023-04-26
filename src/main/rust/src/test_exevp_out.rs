
use crate::win_exec::execvp_out;
use crate::{abs_path_clean, init_log4rs, pop_n_push_s, execvp, get_resources_path, RETRY_AMOUNT};

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{env, fs, io};

use winsafe::co::CREATE;
use winsafe::guard::CloseHandlePiGuard;
use winsafe::{prelude::*, SysResult, HPIPE, HPROCESS, STARTUPINFO};


use log::{error, warn};

pub fn test_main() -> io::Result<()> {

    let executable = "C:\\Users\\noahm\\bin\\wkhtmltopdf.exe";
    let command_line = "-h";
    let out = execvp_out(executable, command_line, Some(u32::MAX));
    dbg!(out);
    
    Ok(())
}

