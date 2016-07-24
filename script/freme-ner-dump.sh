#!/bin/bash

readonly URL="http://rv2622.1blu.de/solrlive"
readonly QUERY="/elinker/select?q=%21dataset:dbpedia"
readonly START=0
readonly STEP=1000000

TOTAL=$(curl -s "${URL}${QUERY}&wt=json&indent=true" | grep "numFound" | awk '{split($0,a,":"); print a[3]}' |awk '{split($0,a,","); print a[1]}'| bc )

echo "Starting processing ${TOTAL} documents ..."

counter=0

mkdir -p dump_original

while [ $counter -le $TOTAL ]
do
   echo "Processing ${counter} / ${TOTAL}  ..."
   wget --no-verbose "${URL}${QUERY}&start=$counter&rows=$(($counter + ${STEP}))&wt=csv&indent=true" -O dump_original/freme-ner-${counter}.csv
   counter=$(( $counter + ${STEP} ))
done

echo "Done..."