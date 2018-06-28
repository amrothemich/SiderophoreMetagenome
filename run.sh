#!/bin/sh
javac -d "bin" -classpath "bin" "src/acquisition/FileAcquisition.java"
java -classpath "bin" acquisition.FileAcquisition
wait
python src/acquisition/run.py