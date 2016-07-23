#!/bin/bash

shopt -s extglob
readonly DESTINATION="datasets"

mkdir -p datasets

for f in dump/*.csv;
do
 echo "Processing $f file..";
 cat ${f} |  tr -d '"' |  sed -e "s/'/'\\'/g" | sed -e "s/)/\\\)/g" | sed -e "s/(/\\\(/g"  | eval "$(awk '{split($0,a,","); print "echo \\\<"a[1]"\\\>\t\\\<http://www.w3.org/2004/02/skos/core#prefLabel\\\>\t\\\""a[5]"\\\"" ">>datasets/"  a[7]"_"a[4]".nt"}')"
done

echo "Done..."