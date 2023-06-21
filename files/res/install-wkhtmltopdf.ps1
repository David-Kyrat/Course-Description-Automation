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
    #if (Test-Path "pdfs") { rm pdfs -Recurse -ErrorAction SilentlyContinue }
    #cmd /C mklink /D "pdfs" .\files\res\pdf\

    where.exe wkhtmltopdf 2>$null | Out-Null
    $wk_exists = $?
    if ($wk_exists) { Exit }

    # Download dependencies through winget
    where.exe winget 2>$null | Out-Null
    # $winget_exists  = $?
    if ( $winget_exists ) {
        winget install --Id wkhtmltopdf.wkhtmltox
        $succ = $?
        if (-not $succ) { $winget_exists = false }
    } 
    if (-not $winget_exists) {
        $bin_path = "${env:USERPROFILE}\bin"
        
        #cd files/res

        if (-not (Test-Path $bin_path)) { mkdir "$bin_path" -ErrorAction Continue }
        if (-not (Test-Path "$bin_path\wkhtmltopdf.exe")) { 
            cp "bin-converters/wkhtmltopdf.exe" "${env:USERPROFILE}\bin"            
        }
        cmd /C 'SET PATH=%PATH%;%USERPROFILE%\bin && SETX PATH "%PATH%"'
        #./add_to_path.bat
        #cd ../..
        #. .\files\res\bin-converters\add_to_path.ps1 # source script copy wkhtmltopdf exec to $env::APPDATA and add it to path
    }

    where.exe wkhtmltopdf | Out-File "wkhtmltopdf.path.txt"
    #refreshenv
    #exit
}

Run-Elevated ($block)
refreshenv

