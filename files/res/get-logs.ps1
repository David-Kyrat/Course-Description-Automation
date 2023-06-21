$old_path = $pwd

$cda_dir =  $MyInvocation.MyCommand.Source
$cda_dir = Resolve-Path "$cda_dir/.." 

$user_docs = [Environment]::GetFolderPath("MyDocuments")
$log_dir = @("$cda_dir/log")
if (Test-Path "$user_docs/err.log") { $log_dir += "$user_docs/err.log" } 

echo $old_path

$log_dir | % {
    Compress-Archive -Path $_ -DestinationPath "$old_path/logs.zip"
}
