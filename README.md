# MySE

MySE stands for _my search engine_.

## Goals

MySE allows to index the content of multiple sources of files into a simple search engine.

## Current status

It can only be used for tests. It doesn't even support de-indexing of deleted files at this stage.

That being said, there is very little risk in letting it run in production.

For any release marked as stable, I will try to make sure the data can be upgraded.

## Supported system

It is made in java so it should work everywhere. It requires at least java 1.7. It has been tested on:

* Linux Ubuntu XFCE
* Windows 7 / 8 / 8.1
* Mac Os X

## Supported sources

* Local disk
* Apache Commons VFS
* Samba (for Windows shared drive)
* Dropbox
* Google drive
* FTPS / FTPeS (Apache Commons didn't work so well on these)

## How it works

All data is stored in ~/.myse/

The search engine list all the files and tries to fetch text data from them.

The frontend uses [angular](https://angularjs.org/).

Core libraries are:

* [Jetty 9](http://eclipse.org/jetty/) (embedded)
* [Elasticsearch](https://www.elastic.co/products/elasticsearch) (embedded)
* [H2](http://www.h2database.com/html/main.html) (embedded) with JPA
* [Apache Tika](https://tika.apache.org/)
* [Apache Commons VFS](http://commons.apache.org/proper/commons-vfs/)
* [Google Drive API](https://developers.google.com/drive/web/quickstart/quickstart-java)
* [Dropbox API](https://www.dropbox.com/developers/core/start/java)

## Known issues

* Deleted files can still be found: Big issue indeed.
* Indexation is slow: It's actually intended to avoid scanning too much at once. But it's not configurable at this stage.
* Google Drive & Dropbox files detection isn't optimal: It will be addressed
