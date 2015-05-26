#!/bin/sh

set -x

if [ "$release" = "" ]; then
	echo "No release !"
	exit 1
fi

ROOT=$(pwd)

export BUILD_DATE=$(date +%Y-%m-%d_%H-%M-%S)
export PACKAGE_VERSION=$(LANG=C dpkg-parsechangelog | grep Version: | cut -d' ' -f2-)
export GIT_COMMIT=$(git rev-parse HEAD)
export GIT_COMMIT_SHORT=$(git rev-parse HEAD|head -c5)
export GIT_COMMIT_COUNT=$(git rev-list HEAD --count)
export VERSION=${PACKAGE_VERSION}.${GIT_COMMIT_COUNT}

cd $ROOT/myse
mvn clean
mvn package -DskipTests

cd $ROOT
mkdir -p dist/
rm dist/* -Rf

echo ${VERSION} >dist/version_last
echo ${VERSION} >version_$release
cp version_$release dist/version_$release

ln myse/target/*dependencies.jar dist/myse_${VERSION}.jar
sha1sum dist/myse_${VERSION}.jar >dist/myse_${VERSION}.jar.sha1sum

if [ "$release" = "stable" ]; then
	# We add this for people who just want/need to download the jar directly
	if [ -f dist/myse.jar ]; then
		rm dist/myse.jar; 
	fi
	ln dist/myse_${VERSION}.jar dist/myse.jar

	echo "
<html>
  <head>
    <title>MYSE ${VERSION}</title>
    <meta http-equiv=\"refresh\" content=\"0; url=http://update.myse.io/myse.jar\" />
  </head>
  <body>
    You are about to <a href=\"/myse.jar\">Get MYSE ${VERSION}</a>.
  </body>
</html>" >dist/index.html
  
	# As this is a version that will be distributed, we want to tag it
	git tag ${VERSION}
	git push --tags
fi

# We create the debian package
if [ ! -f Makefile ]; then
	ln debian/Makefile Makefile
fi

if [ "$DEB_KEY" != "" -a "$release" = "stable" ]; then
	dpkg-buildpackage -b -k${DEB_KEY}
	mkdir -p dist/package
	mv ../myse_* dist/package
else
	dpkg-buildpackage -b -us -uc
	mkdir -p dist/unsigned_package
	mv ../myse_* dist/unsigned_package
fi

# TODO: less dirty
HOST=$(hostname)
if [ "$HOST" = "ovh3" ]; then
	echo "Syncing updates website."
	rsync -av dist/ myse.io@localhost:update/
fi

