extern crate native_windows_derive as nwd;
extern crate native_windows_gui as nwg;

// fn clear() { print!("\x1Bc"); }

pub mod native_win;

fn main() {
    let app = native_win::main(false, Some("course not found".to_owned()));
    dbg!(app);
}
