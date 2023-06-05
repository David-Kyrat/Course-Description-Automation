use native_dialog::{MessageDialog, MessageType, Result};

// #[derive(Debug)]
pub enum DialogControl {
    YesNo,
    Ok,
}

impl Default for DialogControl {
    fn default() -> Self {
        Self::Ok
    }
}

use DialogControl::*;

///# Params
///* `title`: window-title
///* `text`: text to be displayed inside popup
///* `msg_type`: `Option` of a `native_dialog::MessageType` (i.e. `info`, `warn`...)
///* `popup_type`: `Option` of a `DialogControl` (i.e. `YesNo`, `Ok`...)
///
///# Returns
/// * if `popup_type` is `DialogControl::YesNo` then the `Option` wrapped in the returned result
/// is guaranteed to be `Some` (if it is not an error, it contains the user-response to the YesNo
/// dialog popup)
/// * if `popup_type` is `None` or `Ok` then the `Option` is guaranteed to be `None`
///
/// The `Result` represents whether the popup was correctly displayed.
fn message_dialog(
    title: &str,
    text: &str,
    msg_type: Option<MessageType>,
    popup_type: Option<DialogControl>,
) -> Result<Option<bool>> {
    let msg_dialog = MessageDialog::new()
        .set_type(match msg_type {
            Some(mtype) => mtype,
            None => MessageType::Info,
        })
        .set_title(title)
        .set_text(text);

    match popup_type {
        Some(YesNo) => msg_dialog.show_confirm().map(|answer| Some(answer)),
        Some(Ok) | None => msg_dialog.show_alert().map(|()| None),
    }
}

///# Desc
///Display an informative pop-up window with the given title, description and type
///# Params
///* `title`: window-title
///* `text`: text to be displayed inside popup
///* `msg_type`: `Option` of a `native_dialog::MessageType` (i.e. `info`, `warn`...)
///
/// The `Result` represents whether the popup was correctly displayed.
pub fn quick_message_dialog(title: &str, text: &str, msg_type: Option<MessageType>) -> Result<()> {
    message_dialog(title, text, msg_type, None).map(|_| ())
}

///# Desc
///Display a Yes/No query pop-up window with the given title, description and type
///# Params
///* `title`: window-title
///* `text`: text to be displayed inside popup
///* `msg_type`: `Option` of a `native_dialog::MessageType` (i.e. `info`, `warn`...)
///
///# Returns
/// * the user-response to the YesNo
///
/// The `Result` represents whether the popup was correctly displayed.
pub fn quick_yesno_dialog(title: &str, text: &str, msg_type: Option<MessageType>) -> Result<bool> {
    // Option is guaranteed to hold the user-response to the YesNo popup
    message_dialog(title, text, msg_type, Some(YesNo)).map(|opt| opt.unwrap())
}
