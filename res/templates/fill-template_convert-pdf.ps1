#TODO: SEE HOW TO RESOLVE PATH ONCE PACKAGED IN EXECUTABLE APP

$oldPath = $PWD
$scriptPath = Split-Path $MyInvocation.MyCommand.Path -Parent
# cd to where the script is so we actually know where the html template is relative to this script
# the script must always be in a subfolder of the 'res' directory where the html template and 'log' directory is

cd $scriptPath 
$logFile = "../log/pdf-convert.log"
echo "" > $logFile # clean previous log messages or create the file

$template = "desc-template.html"  # html template to be filled each time

$verbose = ($args -Contains "--verbose") 

$in = "$oldPath\" + $args[0] # Markdown file containing course data
$in_item = (Get-Item $in -ErrorAction Continue 2>> $logFile) 
$in = $in_item.FullName  # Full absolute path to markdown file (i.e. input for pandoc)
$inName = $in_item.Name  # Name of that file, should end in ".md"

$outProvidedCondition = 1 + $verbose

$outHtmlName = $inName -replace ".md", ".html"
$outHtml = "$oldPath/$outHtmlName"
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


pandoc $in -t html --template=$template -o $outHtml 2>> $logFile


if ($verbose) { echo "`nConverting html to pdf" }


wkhtmltopdf --enable-local-file-access -T 2 -B 0 -L 3 -R 0  $outHtml $outPdf 2>> $logFile


if ($verbose) { echo "`nDone.`nPDF is present at `"$outPdf`"" }

if ($outHtml -and (Test-Path $outHtml)) {
    rm "$outHtml" -ErrorAction SilentlyContinue  2>> $logFile
}


cd $oldPath
