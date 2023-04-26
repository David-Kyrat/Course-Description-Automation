#![allow(unused)]

use crate::{abs_path_clean, pop_n_push_s, win_exec::execvp, fr, unwrap_retry_or_log};
use crate::utils::RETRY_AMOUNT;

use io::ErrorKind::Other;
use rayon::iter::*;
use std::fs::{DirEntry, ReadDir};
use std::path::{Path, PathBuf};
use std::{env, fs, io};

use log::error;

fn launch_gui() -> io::Result<()> {
    

    Ok(())
}



pub fn main() -> io::Result<()>  {

    Ok(())
}
