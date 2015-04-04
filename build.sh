#!/bin/sh
ROOT=$(pwd)

BUILD_DATE=$(date +%Y-%m-%d_%H-%M-%S)
GIT_COMMIT=$(git rev-parse HEAD)
GIT_COMMIT_COUNT=$(git rev-list HEAD --count)

cd $ROOT/myse
mvn clean
mvn assembly:assembly

cd $ROOT
mkdir -p dist/
rm dist/* -Rf
cp myse/target/*dependencies.jar dist/myse_${BUILD_ID}.jar

