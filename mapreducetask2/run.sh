#! /usr/bin/env bash

mvn package > /dev/null
hdfs dfs -rm -r -skipTrash out* > /dev/null
yarn jar target/Transitions-1.0.jar edu.phystech.konuspaevdn.Transitions /data/wiki/en_articles out > /dev/null

hdfs dfs -cat out/part-r-00000 | head

