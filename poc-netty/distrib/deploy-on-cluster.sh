#!/bin/bash

cat cluster-conf/server-list | while read line
do
    scp -r ../quizz/* "user@$line:/home/user/quizz/"
done