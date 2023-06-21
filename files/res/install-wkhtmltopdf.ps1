#at top of script
<# if (!
    #current role
    (New-Object Security.Principal.WindowsPrincipal(
        [Security.Principal.WindowsIdentity]::GetCurrent()
        #is admin?
    )).IsInRole(
        [Security.Principal.WindowsBuiltInRole]::Administrator
    )
) {
    #elevate script and exit current non-elevated runtime
    Start-Process `
        -FilePath 'powershell' `
        -ArgumentList (
        #flatten to single array
        '-File', $MyInvocation.MyCommand.Source, $args `
        | % { $_ }
    ) `
        -Verb RunAs
    exit
} #>
$path =  $MyInvocation.MyCommand.Source
$path = Resolve-Path "$path/../../.."

function Run-Elevated ($scriptblock) {
    $sh = New-Object -com 'Shell.Application'
    $sh.ShellExecute('powershell',"-NoExit -Command cd $path; $scriptblock",'','runas')
    exit
}
$block = {
    # Create symlink to pdf folder
    if (Test-Path "pdfs") { rm pdfs -Recurse -ErrorAction SilentlyContinue }
    cmd /C mklink /D "pdfs" .\files\res\pdf\

    # Download dependencies through winget
    winget install --Id wkhtmltopdf.wkhtmltox
    refreshenv
    #pause; 
    exit
}

Run-Elevated ($block)
refreshenv
exit
