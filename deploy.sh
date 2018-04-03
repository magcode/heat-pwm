#!/bin/bash

SSH=$1

# Check arguments
if [ "$#" -ne 1 -o ${#SSH} = 0 ]; then
    echo "Incorrect number of parameters."
    exit
fi

#mvn clean package
ssh $SSH rm /tmp/heat-pwm-*-jar-with-dependencies.jar
cd target
jarfile=$(find . -name *dependencies.jar)
scp $jarfile $SSH:/tmp
cd ..
scp service-install.sh $SSH:/tmp
ssh $SSH sudo /tmp/service-install.sh /tmp/$jarfile heat heat
