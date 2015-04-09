#!/bin/sh

set -x

if [ "$release" = "" ]; then
	echo "No release !"
	exit 1
fi

ROOT=$(pwd)

export BUILD_DATE=$(date +%Y-%m-%d_%H-%M-%S)
export GIT_COMMIT=$(git rev-parse HEAD)
export GIT_COMMIT_SHORT=$(git rev-parse HEAD|head -c5)
export GIT_COMMIT_COUNT=$(git rev-list HEAD --count)

VERSION=${GIT_COMMIT_COUNT}-${GIT_COMMIT_SHORT}

echo ${VERSION} >dist/version_last
echo ${VERSION} >version_$release
cp version_$release dist/version_$release



cd $ROOT/myse
mvn clean
mvn assembly:assembly

cd $ROOT
mkdir -p dist/
rm dist/* -Rf

ln myse/target/*dependencies.jar dist/myse_${VERSION}.jar
sha1sum dist/myse_${VERSION}.jar >dist/myse_${VERSION}.jar.sha1sum

# We add this for people who just want/need to download the jar directly
if [ "$release" = "stable" ]; then
	if [ -f dist/myse.jar ]; then
		rm dist/myse.jar; 
	fi
	ln dist/myse_${VERSION}.jar dist/myse.jar
fi
