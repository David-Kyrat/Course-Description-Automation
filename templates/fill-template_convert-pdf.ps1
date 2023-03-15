$template = "desc-template.html"  # html template to be filled each time

$in = $args[0] # Markdown file containing course data
$in = (Resolve-Path $in).Path

$outHtml = "$in" -replace ".md", ".html"
if ($args.Length -gt 1) { $outPdf = (Resolve-Path $args[1]).Path }
else { $outPdf = "$in" -replace ".md", ".pdf" }

echo "Generating course description (html)"
pandoc $in -t html --template=$template -o $outHtml

echo "`nConverting html to pdf"
wkhtmltopdf --enable-local-file-access -T 2 -B 0 -L 3 -R 0  $outHtml $outPdf 2>> ../res/log/pdf-convert.log  
#TODO: maybe do different log files

echo "`nDone.`nPDF is present at `"$outPdf`""
