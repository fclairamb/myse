#!/bin/sh
ROOT=$(pwd)

cd $ROOT/myse
mvn clean
mvn assembly:assembly

cd $ROOT
mkdir -p dist/
cp myse/target/*dependencies.jar dist/myse_${BUILD_ID}.jar

