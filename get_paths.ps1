$op = $PWD
cd C:/Users/noahm/DocumentsNb/BA4/CDA-MASTER
$redundant = "C:/Users/noahm/DocumentsNb/BA4/CDA-MASTER/"

$ignore = "legal", "readme", ".zip", ".svg", ".gitkeep", "ignore", ".log", "release"
$regex = "$ignore".replace(" ", "|")


echo "" > "C:/Users/noahm/DocumentsNb/BA4/CDA-MASTER/rust/to_dl.txt" 

ls ./files/res/* -Recurse -Force | Where-Object {$_.FullName -notmatch "$regex"} | % { 
    $crt = fixPath -Path $_.FullName
    if (Test-Path -Path "$crt" -PathType Leaf) {
        $crt.Replace($redundant, '').Trim() >> "C:/Users/noahm/DocumentsNb/BA4/CDA-MASTER/rust/to_dl.txt" 
    }
    
}

cd $op
