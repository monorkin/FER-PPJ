#!/bin/bash

function print_usage() {
	echo "usage: $0 <target.zip>"
}

if [ -z "$1" ]; then 
	print_usage
	exit 1
fi

if [ -e "$1" ]; then
	echo "$1 exists"
	exit 2
fi
set -x
name="$(basename $1 .zip)"
dir="$(mktemp -d /tmp/ppj-sprut-XXXX)"
cp -r src "$dir/$name"
cp -r "$dir/$name/hr" "$dir/$name/analizator/"

cd "$dir/$name"
zip -r "$1" "."
cd -
rm -fr "$dir"
