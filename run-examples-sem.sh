#!/bin/bash
if [ "$#" -lt 2 ]; then
	echo "usage: $0 <lin> <out>"
	exit 1
fi
echo $1 $2
set -x
java -Xss1m -cp bin SemantickiAnalizator < ./"$1" | diff -su -- - ./"$2"
