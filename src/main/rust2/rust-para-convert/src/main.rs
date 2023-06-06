use log::error;
use log4rs::init_file;

pub mod utils;
pub mod message_dialog;
pub mod launcher;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s, init_log4rs_debug};



fn main() {
    //err!("a");
    println!("Hello, world!");
    init_log4rs(None);

    launcher::get_java_paths().unwrap();
}
