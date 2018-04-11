#!/bin/bash -uxe
make clean build
java -cp dist mapmaker.MapMaker
