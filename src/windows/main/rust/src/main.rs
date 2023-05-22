#![allow(non_snake_case)]

pub mod utils;
pub mod win_exec;
pub mod para_convert; 
pub mod launcher;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s, init_log4rs_debug};
use std::io;


pub fn main() -> io::Result<()> {
    // FIX:: ADD BACK BELOW WHEN DONE TESTING
    // init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS

    init_log4rs_debug();

    // para_convert::main()
    launcher::main()
}