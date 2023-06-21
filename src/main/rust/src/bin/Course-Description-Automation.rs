#![allow(non_snake_case)]

use CDA::{
    launcher,
    utils::{init_log4rs, now},
};


pub fn main() {
    init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS
    log::info!(
        "\n\n--------------- Run Started [{}] -------------------",
        now()
    );
    launcher::main();
}
