#!/bin/sh
# Change these for each different app
DIR=imagej
KEEP="public class ij.ImageJ{ public static void main(java.lang.String[]); }"

# Obfuscation Options
LIBRARY="/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar"

# Common for all apps
INJAR=$DIR/annotated.jar
OUTJAR=$DIR/obfuscated.jar
MAPPING=$DIR/mapping.txt
java -jar proguard.jar -injars $INJAR -outjars $OUTJAR -libraryjars $LIBRARY -printmapping $MAPPING -keep $KEEP
