RRDService disattivato
- Cool Classpath Store

CoolConsoleUpdate
- cache java (guava)
- cache it.cnr.cool.cmis.service.impl.CacheServiceImpl

verificare il versionService

pageservice da verificare riga per riga

avanzamento versione chemistry 0.12.0
-TypeDefinitionCache - gia stata implementata!!

opencmis extensions ???

cambiare url webscript
uniformare URL webscript remote

TOGLIERE OGNI RIFERIMENTO JBOSS...
togliere profili jboss ?

MEMO ZipperService:
  creare artefatto variazioni (jar)
  spostare la dipendenza di quartz in variazioni da cool-webapp
  test (e.g. ZipperServiceTest) da spostare nel nuovo artefatto

alleggerire WEB-INF lib - librerie sospette doccnr:
  X spring aop ???
  X aspectj
  X javase core - com.google.zxing
  X jfreechart
  X quartz ?
  org.springframework.*

creare nuovo artefatto cnr-flows "pure", senza artefatti superflui (e.g. contabili, accounting...)

rivedere tutti gli header di caching
- <bean id="jcononResourceController" class="it.cnr.cool.extensions.surf.mvc.CMISResourceController">


---- JCONON

nota x jconon: cool-common-web-context e' stato fortemente smembrato

verificare aggiornamento aspect in NodeMetadataService.updateObjectProperties()


--- fatto

puntamento a cmis 1.1

--- later
a che serve spring web ???? lo usiamo solo per gli allegati!