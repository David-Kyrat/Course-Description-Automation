#!/usr/bin/env zsh
# ================================================================
# WARNING: THIS SCRIPTS LOCATE AND SYMLINK THE CORRECT DEPENDENCY
# WHERE THEY ARE NEED IF THEY ARE ALREADY INSTALLED. 
# THIS WAS MADE FOR INTEGRATION AND DEPLOYEMENT TESTING ONLY
# THE PROGRAM WONT WORK IF THE CORRECT DEPENCY ARE NOT 
# INSTALLED IN THE files/res FOLDER.
# ================================================================

[ -d "./files/res" ] || (echo "Please move at the root of the project. files/res folder must be accessible"; exit 1)

[[ "$(command -v java)" ]] || (echo "Java is not installed"; exit 1)

if [ -z ${JAVA_HOME+x} ]; then (echo "JAVA_HOME is unset"; exit 1) fi


java_ver="$(java -version 2>&1 | grep -i version | cut -d'"' -f2 | cut -d'.' -f1-2)"
[[ $java_ver -ge "17" ]] || (echo "Java >= 17 is required."; exit 1)

[[ "$(command -v pandoc)" ]] || (echo "pandoc is not installed"; exit 1)
[[ "$(command -v wkhtmltopdf)" ]] || (echo "wkhtmltopdf is not installed"; exit 1)

cd files/res
cd bin-converters
# ln -s "$(which pandoc)"
cd ..

cd java
# ln -s "$(wh)"
cd ..

cd ../..

exit 0
