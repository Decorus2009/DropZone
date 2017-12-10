#!/bin/bash
mvn clean

kill -9 $(cat app.pid)

UPLOAD_TMP=upload_tmp

if [ -d "$UPLOAD_TMP" ]
then
    rm -r "$UPLOAD_TMP"
fi

rm app.pid
