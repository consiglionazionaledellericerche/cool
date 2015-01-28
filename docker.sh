#! /bin/bash

docker run \
   --rm  \
   -v $(pwd)/cool-webapp/cool-doccnr/target/:/opt/apache-tomcat-7.0.55/webapps \
    ubuntu-tomcat7 
