#!/bin/bash

UPLOAD_TMP=upload_tmp

if [ ! -d "$UPLOAD_TMP" ]
then
    mkdir "$UPLOAD_TMP"
fi

mvn package && java -jar target/Dropzone-1.0-SNAPSHOT.jar
