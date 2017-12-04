#!/bin/bash
mvn clean

kill -9 $(cat ./run_script/app.pid)

UPLOAD_TMP=upload_tmp

if [ -d "$UPLOAD_TMP" ]
then
    rm -r "$UPLOAD_TMP"
fi

rm ./run_script/app.pid
