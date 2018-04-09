#!/bin/bash -uxe
make build
java -cp dist mapmaker.MapMaker
