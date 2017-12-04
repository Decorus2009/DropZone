#!/bin/bash

apt-get update -y
apt-get upgrade -y

# for add-apt-repository
apt install software-properties-common -y
add-apt-repository ppa:webupd8team/java
apt-get update -y

apt-get install oracle-java8-installer -y
apt-get install postgresql postgresql-contrib -y
apt-get install maven -y
apt-get install git -y

git clone https://github.com/Decorus2009/DropZone
