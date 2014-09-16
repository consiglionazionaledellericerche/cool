set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,suspend=n,server=y 

REM CONFIGURAZIONE PROXY (e.g. Fiddler)
REM -DproxySet=true -DproxyHost=127.0.0.1 -DproxyPort=8888
mvn clean tomcat:run -P jconon