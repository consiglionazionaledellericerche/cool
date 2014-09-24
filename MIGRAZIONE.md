cambiare url webscript, uniformare URL webscript remote

CoolConsoleUpdate
- cache java (guava)
- cache it.cnr.cool.cmis.service.impl.CacheServiceImpl

verificare il versionService

pageservice da verificare riga per riga

rivedere tutti gli header di caching
- i18n, cache, common
- <bean id="jcononResourceController" class="it.cnr.cool.extensions.surf.mvc.CMISResourceController">

buttare via risorse obsolete Surf etc.

alleggerire WEB-INF lib - librerie sospette doccnr:
  X spring aop ???
  X aspectj
  X javase core - com.google.zxing
  X jfreechart
  X quartz ?
  org.springframework.*
  isolare dipendenza Guava (si puo' evitare ???)

fixare test


---- Marco
TypeDefinitionCache - gia stata implementata!!

RRDService disattivato
- Cool Classpath Store

---- JCONON

nota x jconon: dal context cool-common-web-context sono stati eliminate molte definizioni di bean per Spring Surf/Spring MVC

verificare aggiornamento aspect in NodeMetadataService.updateObjectProperties()


---- cirone

verificare loop for (SecondaryType st : documentToCopy.getSecondaryTypes())

ZipperService:
  creare artefatto variazioni (jar)
  spostare la dipendenza di quartz in variazioni da cool-webapp
  test (e.g. ZipperServiceTest) da spostare nel nuovo artefatto

--- fatto

puntamento a cmis 1.1
avanzamento versione chemistry 0.12.0
rimozione alfresco-opencmis-extension

--- later
a che serve spring web ???? lo usiamo solo per gli allegati!
verificare con la configurazione di produzione apache che il caching funziona (cache pubbliche/private...)