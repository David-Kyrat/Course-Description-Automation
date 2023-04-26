#![allow(non_snake_case)]

pub mod utils;
pub mod win_exec;

use utils::{abs_path_clean, init_log4rs, pop_n_push_s};






use std::{io};








pub mod test_exevp_out;

pub fn main() -> io::Result<()> {
    // FIX:: ADD BACK BELOW WHEN DONE TESTING
    //init_log4rs(None);
    // HK: DONT DELETE ABOVE THIS

    log4rs::init_file("logging_config.yaml", Default::default());


    // _main()
    
    test_exevp_out::test_main()
}
