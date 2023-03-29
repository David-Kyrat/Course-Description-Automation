
$oldPath = $PWD
$scriptPath = Split-Path $MyInvocation.MyCommand.Path -Parent
# cd to where the script is so we actually know where the html template is relative to this script
# the script must always be in a subfolder of the 'res' directory where the html template and 'log' directory is

cd $scriptPath 

$template = "desc-template.html"  # html template to be filled each time

$verbose = ($args -Contains "--verbose") 

# Markdown file containing course data
if (Split-Path $args[0] -IsAbsolute) {
    $in = $args[0]
} else {
    $in = "$oldPath\" + $args[0] 
}

$in_item = (Get-Item $in -ErrorAction Continue ) 
$in = $in_item.FullName  # Full absolute path to markdown file (i.e. input for pandoc)
$inName = $in_item.Name  # Name of that file, should end in ".md"

$outProvidedCondition = 1 + $verbose

#HK: Place html file in $scriptPath i.e. in template file, or it won't find the css/js resources 
$outHtmlName = $inName -replace ".md", ".html"  
$outHtml = "$scriptPath/$outHtmlName"
$outPdf = ""

if ($args.Length -gt $outProvidedCondition) { 
    $outPdf = $args[1] 
    if (-not (Split-Path $outPdf -IsAbsolute)) {
        $outPdf = "$oldPath/$outPdf"  # resolves relative path to output file against the path where the script was called
    }
} else {
    $outPdfName = $inName -replace ".md", ".pdf" 
    $outPdf = "$oldPath/$outPdfName"  # by defaults saves pdf where script was called
}

if ($verbose) { echo "Generating course description (html)" }


pandoc $in -t html --template=$template -o $outHtml # 2>> $logFile


if ($verbose) { echo "`nConverting html to pdf" }


wkhtmltopdf --enable-local-file-access -T 2 -B 0 -L 3 -R 0  $outHtml $outPdf # 2>> $logFile


if ($verbose) { echo "`nDone.`nPDF is present at `"$outPdf`"" }

if ($outHtml -and (Test-Path $outHtml)) {
   # rm "$outHtml" -ErrorAction SilentlyContinue  # 2>> $logFile
}


cd $oldPath
