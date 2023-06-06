#![allow(non_snake_case)]

pub mod launcher;
pub mod main2;
pub mod message_dialog;
pub mod para_convert;
pub mod utils;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s};

//C:\\Users\\noahm\\DocumentsNb\\BA4\\CDA-MASTER\\files\\res\\java\\jdk-17\\bin\\java.exe -jar C:\\Users\\noahm\\DocumentsNb\\BA4\\CDA-MASTER\\files\\res\\java\\Course-Description-Automation.jar 12X001#
pub fn main() {
    init_log4rs(None);
    // NB: DONT DELETE ABOVE THIS

    launcher::main();
    // para_convert::main().expect("could not convert");
}
