$files = "app-info-logo.svg", "abbrev.tsv", "readme-example2.png", "cda-icon-mac.icns", "logging_config.yaml"

$files | % { rm $_ -ErrorAction SilentlyContinue }
rm .\files -Recurse -ErrorAction SilentlyContinue
