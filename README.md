# Project info #


SVN repository URL:
<svn+ssh://scm.cedrc.cnr.it/data/svnroot/svn-alfrescoportal/trunk/cool>

Project Kanban Board
<https://trello.com/board/cool/50ac9200740893525001931a>

Jenkins

* <http://bandt.si.cnr.it:8180/jenkins/job/cool-jconon/>
* <http://bandt.si.cnr.it:8180/jenkins/job/cool-doccnr/>
* <http://bandt.si.cnr.it:8180/jenkins/job/cool-jconon-development/>
* <http://bandt.si.cnr.it:8180/jenkins/job/cool-doccnr-development/>

Configuration
**cool-doccnr/src/main/resources/META-INF/spring/default-repository.properties**

Quick Run
---

	cd cool-doccnr && mvn tomcat:run

per avviare tomcat in ascolto da qualunque indirizzo:

	tomcat:run -DbindAddress=0.0.0.0

per abilitare il debug remoto:

	set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,suspend=n,server=y

Release
---

ricordarsi di aggiornare l'RBAC di produzione e le risorse in remote

jconon
---
gli artefatti vengono generati automaticamente da Jenkins con il comando:

	clean deploy -Pjconon,produzione -Dcool.build.number=${SVN_REVISION}

e vengono depositati sul repository nexus <http://bandt.si.cnr.it:8280/nexus/content/groups/public/it/cnr/si/cool/cool-jconon/>

maven-replacer-plugin
---

	<build>
	  <plugins>
	    <plugin>
	      <groupId>com.google.code.maven-replacer-plugin</groupId>
	      <artifactId>replacer</artifactId>
	      <version>1.5.1</version>
	      <executions>
	        <execution>
	          <phase>prepare-package</phase>
	          <goals>
	            <goal>replace</goal>
	          </goals>
	        </execution>
	      </executions>
	      <configuration>
	        <filesToInclude>cool-common\src\main\resources\META-INF\js\cnr\cnr.js , cool-common\src\main\resources\META-INF\js\cnr\cnr.url.js</filesToInclude>
	        <replacement>
	          <token>(var debug =)(.*)</token>
	          <value>$1 false,</value>
	        </replacement>
	        <replacement>
	          <token>(timeDelayLogger =)(.*)</token>
	          <value>$1 6000,</value>
	        </replacement>
	      </replacements>
	      <regexFlags>
	        <regexFlag>MULTILINE</regexFlag>
	      </regexFlags>
	    </configuration>
	  </plugin>
	</plugins>
	</build>