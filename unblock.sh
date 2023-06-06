#!/bin/sh

# Attempt at tricking os into thinking file has not be downloaded from the internet
# to be able to just run it...
file="$0"
cat "$file" > temp
cat temp > "$file"
chmod u+x "$file"
rm temp
