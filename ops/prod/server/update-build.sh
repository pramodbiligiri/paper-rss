#!/bin/bash

newdir=$1;

echo "Setting up build in $newdir";

oldpid=`ps -ef | grep java | grep "app.identifier=rssapp" | grep -v grep | awk '{print $2}'`;

kill -9 $oldpid;

cd $newdir;

rm -f /home/rssapp/builds/latest;
ln -sf $newdir /home/rssapp/builds/latest;

echo "Launching rssapp from build in $newdir";

PORT=8080 nohup java -Xms512m -Xmx1024m -Dspring.profiles.active=het -Dapp.identifier=rssapp -jar paper-rss-1.0-SNAPSHOT.jar > app.log 2>&1&
