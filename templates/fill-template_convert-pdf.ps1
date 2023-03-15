echo "" > ../res/log/pdf-convert.log  # clean previous log messages or create the file
$template = "desc-template.html"  # html template to be filled each time

$verbose = ($args -Contains "--verbose") 

$in = $args[0] # Markdown file containing course data
$in = (Resolve-Path $in -ErrorAction Continue).Path 2>> ../res/log/pdf-convert.log

$outProvidedCondition = 1 + $verbose #(if ($verbose) { 1 } else { 0 })
echo $outProvidedCondition

$outHtml = "$in" -replace ".md", ".html"
if ($args.Length -gt $outProvidedCondition) { $outPdf = $args[1] }
else { $outPdf = "$in" -replace ".md", ".pdf" }

if ($verbose) { echo "Generating course description (html)" }


pandoc $in -t html --template=$template -o $outHtml 2>> ../res/log/pdf-convert.log  


if ($verbose) { echo "`nConverting html to pdf" }


wkhtmltopdf --enable-local-file-access -T 2 -B 0 -L 3 -R 0  $outHtml $outPdf 2>> ../res/log/pdf-convert.log  
#TODO: maybe do different log files


$outPdf = (Resolve-Path $outPdf).Path
if ($verbose) { echo "`nDone.`nPDF is present at `"$outPdf`"" }
rm $outHtml -ErrorAction SilentlyContinue
