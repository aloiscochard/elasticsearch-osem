#!/bin/sh
mvn package -Dmaven.test.skip

#mvn deploy:deploy-file  -Durl=file://./ \
#                        -DrepositoryId=elasticsearch-osem \
#                        -Dfile=target/elasticsearch-osem-0.1-SNAPSHOT.jar \
#                        -DpomFile=pom.xml

cp target/elasticsearch-osem-0.1-SNAPSHOT.jar  \
  snapshots/org/elasticsearch/elasticsearch-osem/0.1-SNAPSHOT/elasticsearch-osem-0.1-SNAPSHOT.jar

cp pom.xml \
  snapshots/org/elasticsearch/elasticsearch-osem/0.1-SNAPSHOT/elasticsearch-osem-0.1-SNAPSHOT.pom

