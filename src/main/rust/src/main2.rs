use native_dialog::MessageType;
use crate::message_dialog::{quick_message_dialog, quick_yesno_dialog};

pub fn main() {
    let err_msg = "main could not display popup";
    let yes = quick_yesno_dialog("Question ?", "<Question description>", None).expect(err_msg);

    if yes {
        quick_message_dialog("You clicked yes", "Some Text", None).expect(err_msg)
    } else {
        quick_message_dialog("You clicked No", "Some Text", Some(MessageType::Error))
            .expect(err_msg)
    }
}

/* fn err_fmt(title: &str, text: &str, popup_type: Option<DialogControl>) -> String { format!("main: could not display pop-up for title: {title}, text: {text}, popup_type:{:#?}", popup_type) }
let (title, desc, popup_type) = ("Question ?", "<Question description>", Some(YesNo)); */
