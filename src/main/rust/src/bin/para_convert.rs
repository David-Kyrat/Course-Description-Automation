use CDA::{para_convert, utils::init_log4rs};

fn main() -> std::io::Result<()> {
    init_log4rs(None);
    para_convert::main()
}
