#![windows_subsystem = "windows"] // don't display terminal window. Valid values are 'Windows|Terminal'
#![allow(non_snake_case)]

pub mod launcher;
pub mod message_dialog;
pub mod para_convert;
pub mod utils;



// Defining an OS-dependant module 
// I.e. on windows have exec module point to /src/exec/winexec.rs
// and on unix have it point to /src/exec/nixexec.rs
/* #[path = "exec/winexec.rs"]
mod exec; */

#[cfg(windows)]
#[path = "exec/winexec.rs"]
pub mod exec;

#[cfg(unix)]
#[path = "exec/nixexec.rs"]
pub mod exec;

