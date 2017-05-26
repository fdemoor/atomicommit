#! /bin/sh

JAR=lib/plantuml-8059.jar
DL=http://central.maven.org/maven2/net/sourceforge/plantuml/plantuml/8059/plantuml-8059.jar

if [ ! -f $JAR ]; then
  echo "Downloading plantuml jar"
  wget $DL -P lib/
fi

FILES=$(find atomicommit -name "*.puml")

for i in $FILES; do
    java -jar $JAR $i -tsvg
done
