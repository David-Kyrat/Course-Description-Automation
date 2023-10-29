#!/usr/bin/env bash
# ================================================================
# WARNING: THIS SCRIPTS LOCATE AND SYMLINK THE CORRECT DEPENDENCY
# WHERE THEY ARE NEED IF THEY ARE ALREADY INSTALLED. 
# THIS WAS MADE FOR INTEGRATION AND DEPLOYEMENT TESTING ONLY
# THE PROGRAM WONT WORK IF THE CORRECT DEPENCY ARE NOT 
# INSTALLED IN THE files/res FOLDER.
# ================================================================

[ -d "./files/res" ] || (echo "Please move at the root of the project. files/res folder must be accessible"; exit 1)

[[ "$(command -v java)" ]] || (echo "Java is not installed"; exit 1)

exit 0
