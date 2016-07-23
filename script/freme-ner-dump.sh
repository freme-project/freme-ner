#!/bin/bash

readonly START=0
readonly TOTAL=88000000
readonly URL=http://rv2622.1blu.de/solrlive

counter=0

mkdir dump

while [ $counter -le $TOTAL ]
do
   curl "${URL}/elinker/select?q=*%3A*&start=$counter&rows=$(($counter + 1000000))&wt=csv&indent=true" > dump/freme-ner-${counter}.csv
   counter=$(( $counter + 1000000 ))
done
