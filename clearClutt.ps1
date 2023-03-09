# Clear clutter from sbt output when piped to it

[String] $str = $input 
$patt = "running ch.Main"
$idx = $str.IndexOf($patt)
$tmp = $str.Substring($idx)
$len = $patt.Length+1
$newStrStart = $tmp.Substring(0, $len) + "`r`n`r`n" 
$newStrEnd =  $tmp.Substring($len)
$idxEnd = $newStrEnd.IndexOf("[")
$newStrEnd = $newStrEnd.Substring(0, $idxEnd).trim() + "`r`n" + $newStrEnd.Substring($idxEnd)
$newStrStart + $newStrEnd
