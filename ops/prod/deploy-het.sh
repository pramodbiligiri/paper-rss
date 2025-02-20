#!/bin/bash

LOGIN_USER=rssapp

newdir=`ssh -i ~/.ssh/id_het rssapp@new.papertime.app 'bin/new-build-dir'`;

echo "New build dir: $newdir";

scp -i ~/.ssh/id_het target/tts-rss-1.0-SNAPSHOT.jar $LOGIN_USER@new.papertime.app:$newdir

remote_cmd="bin/update-build $newdir";

ssh -i ~/.ssh/id_het $LOGIN_USER@new.papertime.app "$remote_cmd"
