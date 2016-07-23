#!/bin/bash

readonly START=0
readonly TOTAL=88000000

counter=0

while [ $counter -le $TOTAL ]
do
   curl "http://rv2622.1blu.de/solrlive/elinker/select?q=*%3A*&start=$counter&rows=$(($counter + 1000))&wt=csv&indent=true" >> freme-ner.csv
   counter=$(( $counter + 1000 ))
done
