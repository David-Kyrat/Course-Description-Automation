PROJECT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PDF_SYMLINK_PATH="$HOME/Documents/Course-Description-Automation-pdfs"
PDF_SYMLINK_TARGET_PATH="$PROJECT_DIR/files/res/pdf"
FILE_NAME="Course-Description-Automation"

if [ ! -d "$PROJECT_DIR" ]; then
    echo "Cannot find $PROJECT_DIR make sure it is located at \"$HOME/Documents/$PROJECT_DIR\" "
    exit 1
fi

[[ ! -L "$PDF_SYMLINK_PATH" ||  ! -e "$PDF_SYMLINK_PATH" ]] && ln -sf "$PDF_SYMLINK_TARGET_PATH" "$PDF_SYMLINK_PATH"

cd "$PROJECT_DIR" || exit

[ ! "$(find "./$FILE_NAME"  -type  f -perm -a+x)" ] && chmod a+x "./$FILE_NAME"

"./$FILE_NAME" &
disown
