$dirs = "pdf", "templates"

$dirs | % {
    $folder_name = $_
    rm "res/$folder_name/desc-20??-*"
}
