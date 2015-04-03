#!/bin/sh
ROOT=$(pwd)

cd $ROOT/myse
mvn clean
mvn package

cd $ROOT
mkdir -p dist/
cp myse/target/*dependencies.jar dist/

