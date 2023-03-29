$oldPath = $PWD
$scriptPath = Split-Path $MyInvocation.MyCommand.Path -Parent

cd $scriptPath 
# now we should be at <root-dir>/res/templates
# $mdDir = (Resolve-Path ("$pwd/../md")).Path


ls "../md/*.md" | % -Parallel {
    $outPdfPath = "../pdf/" + $_.Name.replace(".md", ".pdf")
    ./fill-template_convert-pdf.ps1 $_.FullName $outPdfPath -ErrorAction Continue 2>&1> $null
}

cd $oldPath
