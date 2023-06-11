#![allow(non_snake_case)]

mod launcher;
mod message_dialog;
mod para_convert;
mod utils;

use utils::*;
// use utils::{abs_path_clean, init_log4rs, pop_n_push_s};

fn _real_main() {
    init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS
    launcher::main();
}

fn _test_main() {
}

pub fn main() {
    // real_main();
    _test_main();
}
