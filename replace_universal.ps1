$mainDir = (Get-Item "C:/Users/noahm/DocumentsNb/BA4/Course-Description-Automation/src/main") 
$universal = (Get-Item "C:/Users/noahm/DocumentsNb/BA4/Course-Description-Automation/src/universal").FullName
rm $universal -Recurse -Force

mkdir $universal
cp $mainDir "$universal/" -Recurse
