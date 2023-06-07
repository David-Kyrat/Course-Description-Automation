#![allow(non_snake_case)]

pub mod utils;
pub mod para_convert; 
pub mod launcher;
pub mod message_dialog;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s};

pub fn main() {
    init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS
    // init_log4rs_debug();
    let paths = utils::current_exe_path();
    dbg!(paths);
    launcher::main();
}
