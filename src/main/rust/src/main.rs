#![allow(non_snake_case)]

pub mod utils;
pub mod para_convert; 
pub mod launcher;
pub mod message_dialog;
pub mod main2;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s, init_log4rs_debug};

//C:\\Users\\noahm\\DocumentsNb\\BA4\\CDA-MASTER\\files\\res\\java\\jdk-17\\bin\\java.exe -jar C:\\Users\\noahm\\DocumentsNb\\BA4\\CDA-MASTER\\files\\res\\java\\Course-Description-Automation.jar 12X001#
pub fn main() {
    init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS

    // init_log4rs_debug();
    launcher::main();
}
