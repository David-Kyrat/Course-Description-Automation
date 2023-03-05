
#INFO: A3 renders really well but may not be conveniant:
function a3_convert {
    Param($in, $out)
    
    wkhtmltopdf.exe --enable-local-file-access  -T 2 -B 0 -L 3 -R 0 -s A3 $in $out
}

function a4_convert {
    Param($in, $out)
    
    wkhtmltopdf.exe --enable-local-file-access -T 2 -B 0 -L 3 -R 0 $in $out
}

$in = $args[0]
$in = (Resolve-Path $in).Path
$out = ""
if ($args.Length -le 1) { $out = $in -replace ".html", ".pdf" }
else { $out = $args[1] }

a4_convert $in $out
