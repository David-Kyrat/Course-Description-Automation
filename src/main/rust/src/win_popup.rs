extern crate native_windows_derive as nwd;
extern crate native_windows_gui as nwg;

use nwd::NwgUi;
use nwg::MessageButtons as MB;
use nwg::MessageChoice as MC;
use nwg::MessageIcons as MI;
use nwg::{ControlHandle,  MessageChoice, NativeUi};


#[derive(Default, NwgUi)]
pub struct EmbedApp {
    #[nwg_control(size: (300, 145), position: (300, 300), flags: "DISABLED")]
    #[nwg_events( OnWindowClose: [EmbedApp::exit], OnInit: [EmbedApp::init] )]
    window: nwg::Window,
    #[nwg_resource]
    embed: nwg::EmbedResource,
    #[nwg_resource(source_embed: Some(&data.embed), source_embed_str: Some("WINDOWICON"))]
    window_icon: nwg::Icon,
}

fn set_window_icon(ea: &EmbedApp) {
    ea.window.set_icon(Some(&ea.window_icon));
}

fn modal(parent: ControlHandle, _title: &str, _content: &str, success: bool) -> nwg::MessageChoice {
    let p = nwg::MessageParams {
        title: _title,
        content: _content,
        buttons: if success { MB::YesNo } else { MB::RetryCancel },
        icons: if success { MI::Info } else { MI::Error },
    };

    nwg::modal_message(parent, &p)
}

fn handle_match(choice: MessageChoice) -> bool {
    match choice {
        MC::Retry | MC::Yes =>  true,
        MC::Cancel | MC::No =>  false,
        _ => { panic!("unexpected button clicked"); }
    }
}

impl EmbedApp {
    pub const TITLE_SUCC: &str = "PDF generation successful";
    pub const TITLE_ERR: &str = "PDF generation unsuccessful";
    pub const MESSAGE_SUCC: &str =
        "PDF generation successful.\n Would you like to generate anything more?";
    pub const FONT: &str = "Microsoft Sans Serif";

    pub fn message_err(error: String) -> String {
        format!("{}, Do you want to retry?", error)
    }

    fn main_win(&self, success: bool, err_msg: Option<String>) -> bool {
        let err_msg = match err_msg {
            Some(expr) => expr,
            None => { if success { "".to_owned() } else { "unexpected error happened".to_owned() } }
        };

        let (title, message) = if success {
            (EmbedApp::TITLE_SUCC.to_owned(), EmbedApp::MESSAGE_SUCC.to_owned())
        } else {
            (EmbedApp::TITLE_ERR.to_owned(), EmbedApp::message_err(err_msg.to_owned()))
        };

        let choice = modal(self.window.handle, title.as_str(), message.as_str(), success);
        let restart = handle_match(choice);
        self.exit();
        restart
    }

    ///
    /// # Returns
    /// Retry (restart app) or cancel (just quits)
    pub fn success_window(&self) -> bool {
        let choice = modal(
            self.window.handle,
            EmbedApp::TITLE_SUCC,
            EmbedApp::MESSAGE_SUCC,
            true,
        );
        let restart = handle_match(choice);
        self.exit();
        restart
    }

    ///
    /// # Returns
    /// Retry (restart app) or cancel (just quits)
    pub fn error_window(&self, error_msg: String) -> bool {
        let choice = modal(
            self.window.handle,
            EmbedApp::TITLE_ERR,
            &EmbedApp::message_err(error_msg),
            false,
        );
        let restart = handle_match(choice);
        self.exit();
        restart
    }

    pub fn init(&self) {
        self.window.set_text(EmbedApp::TITLE_SUCC);
        set_window_icon(self);
    }

    fn exit(&self) {
        // nwg::simple_message("Goodbye", &format!("Goodbye"));
        nwg::stop_thread_dispatch();
    }
}

pub fn main(success: bool, err_msg: Option<String>) -> bool {
    nwg::init().expect("Failed to init Native Windows GUI");
    let app = EmbedApp::build_ui(Default::default()).expect("Failed to build UI");
    let response = app.main_win(success, err_msg);
    nwg::dispatch_thread_events();
    response
}

/*
fn get_font(font_name: &str, size: u32) -> Font {
    let mut font = Default::default();
    let res = nwg::Font::builder()
        .family(font_name)
        .size(size)
        .build(&mut font);
    if res.is_err() {
        nwg::Font::builder()
            .family("Arial")
            .size(size)
            .build(&mut Default::default())
            .expect("Arial (Default font) not available on your system");
    } else {
        res.unwrap();
    }
    font
} */
