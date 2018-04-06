#!/bin/bash -e

ofile=$1
if [ -z "$ofile" ]; then
    echo "Usage $0 <option-file>"
    exit 1
fi

java editor.Main "$ofile" | python list-players.py
