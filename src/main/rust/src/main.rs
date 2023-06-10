#![allow(non_snake_case)]

pub mod net;
pub mod bootstrapper;
pub mod launcher;
pub mod message_dialog;
pub mod para_convert;
pub mod utils;

use utils::*;
// use utils::{abs_path_clean, init_log4rs, pop_n_push_s};

fn _real_main() {
    init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS
    launcher::main();
}

fn _test_main() {
    dbg!(bootstrapper::main()).expect("test main should not error");
}

pub fn main() {
    // real_main();
    _test_main();
}
