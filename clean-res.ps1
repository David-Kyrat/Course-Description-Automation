$dirs = "files/res/pdf", "files/res/md"

$dirs | % {
    $folder_name = $_
    rm "$folder_name/desc-20??-*"
}

$dirs | % {
    $fd = $_
    if (-not (Test-Path("$fd/.gitkeep"))) {
        echo " " >> "$fd/.gitkeep"
    }
}
