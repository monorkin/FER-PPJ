#!/bin/bash
if [ "$#" -lt 3 ]; then
	echo "usage: $0 <langdef.san> <lin> <out>"
	exit 1
fi
echo $1 $2 $3
set -x
cd bin
java -Xss1m GSA 2>&1 >/dev/null < ../"$1"
cd analizator
java -Xss1m SA < ../"$2" 2>/dev/null | diff -su -- - ../"$3"
