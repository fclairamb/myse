# MySE

MySE stands for _my search engine_.

## Goals

MySE allows to index the content of multiple sources of data into a simple search engine.

## Install

* Windows / Mac Os X / Linux manual : [the JAR](http://update.myse.io)
* Linux Debian with APT: [The APT repository](http://apt.webingenia.com)

## Current status

It's not production-ready, use it at your own risk. That being said, there's very little
risk in letting in run over your files as it only reads them.

## Supported system

It is made in java so it should work everywhere. It requires at least java 1.7. It has been tested on:

* Linux Debian
* Windows 7+
* Mac Os X

## Supported sources

* Local disk
* Apache Commons VFS
* Samba (for Windows shared drive)
* Dropbox
* Google drive
* FTPS / FTPeS (Apache Commons didn't work so well on these)
* Web exploration (basic web crawler, buggy at this stage)

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
* [JCIFS](https://jcifs.samba.org/)
* [Google Drive API](https://developers.google.com/drive/web/quickstart/quickstart-java)
* [Dropbox API](https://www.dropbox.com/developers/core/start/java)

## Known issues

* Samba: Files starting with a space are constantly marked for indexation and deletion but never actually indexed. It's a jcifs issue, you can only remove the trailing space at this stage.
* Indexation is slow: It's actually intended to avoid scanning too much at once.
