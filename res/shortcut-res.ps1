$oldpath = $pwd
$scriptDir = Split-Path ($MyInvocation.mycommand.path) -Parent
cd $scriptDir

#now we're in res directory
$shortcutLink = (Get-Item "../../").FullName # create shortcut outside of "wrapping" 'files' folder
#.. is "files/", ../.. is executable path
$shortcutLink = "$shortcutLink\pdfs.lnk"
$ShortcutTarget = "$pwd\pdf"

$s = (New-Object -ComObject WScript.Shell).CreateShortcut.invoke($shortcutLink)
$s.TargetPath = $ShortcutTarget
$s.Description = "PDFs res directory shortcut"
$s.Save()

cd $oldpath
