$oldPath = $PWD
$scriptPath = Split-Path $MyInvocation.MyCommand.Path -Parent

cd $scriptPath 
# now we should be at <root-dir>/res/templates


# For Each markdown file in res/md/ directory, do in Parallel =>
ls "../md/*.md" | % -Parallel {
    $outPdfPath = "../pdf/" + $_.Name.replace(".md", ".pdf")
    ./fill-template_convert-pdf.ps1 $_.FullName $outPdfPath -ErrorAction Continue 2>&1> $null
}

cd $oldPath
