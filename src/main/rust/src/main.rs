#![allow(non_snake_case)]

pub mod utils;
pub mod win_exec;
pub mod para_convert; 
pub mod launcher;
pub mod win_popup;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s, init_log4rs_debug};


pub fn main() {
    // FIX:: ADD BACK BELOW WHEN DONE TESTING
    // init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS

    init_log4rs_debug();

    // para_convert::main()
    launcher::main().unwrap();
}
