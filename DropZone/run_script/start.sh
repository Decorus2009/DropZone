#!/bin/bash
if [ ! -d upload_tmp ]
then
    mkdir upload_tmp
fi
mvn package && java -jar target/Dropzone-1.0-SNAPSHOT.jar
