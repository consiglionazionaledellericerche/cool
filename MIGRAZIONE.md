spostare cmisbindinglistener

fixare test

check messaggi warning nelle log durante avvio applicazioni

require.js anche per doccnr

ripulire i rbac.get.json.ftl e spostarli in /Data Dictionary/Web Applications
spostare rbac di doccnr in cool-doccnr/


---- JCONON
nota x jconon: dal context cool-common-web-context sono stati eliminate molte definizioni di bean per Spring Surf/Spring MVC

verificare aggiornamento aspect in NodeMetadataService.updateObjectProperties()


---- cirone
verificare loop for (SecondaryType st : documentToCopy.getSecondaryTypes())

ZipperService: => VariazioniService
  creare artefatto variazioni (jar)
  pagina etc.
  spostare la dipendenza di quartz in variazioni da cool-webapp
  test (e.g. ZipperServiceTest) da spostare nel nuovo artefatto

togliere dipendenza Quartz

--- fatto

puntamento a cmis 1.1
avanzamento versione chemistry 0.12.0
rimozione alfresco-opencmis-extension

TypeDefinitionCache - gia stata implementata!!

RRDService disattivato
- Cool Classpath Store
- versionservice preoduction
- hardening classpath://remote

cambiare url webscript, uniformare URL webscript remote

refactoring PageService

buttare via risorse obsolete Surf etc.

--- later
a che serve spring web ???? lo usiamo solo per gli allegati!
verificare con la configurazione di produzione apache che il caching funziona (cache pubbliche/private...)
rivedere tutti gli header di caching
- i18n, cache, common
- <bean id="jcononResourceController" class="it.cnr.cool.extensions.surf.mvc.CMISResourceController">

usare PathMatchingResourcePatternResolver per i18n

CoolConsoleUpdate
- cache java (guava) - isolare dipendenza Guava (si puo' evitare ???)
- cache it.cnr.cool.cmis.service.impl.CacheServiceImpl

verificare il versionService

mod-cache e gzip su doccnr

bloccare con 403 le url
  /service/api/solr/*
  /s/api/solr/*
  /wcservice/api/solr/*
  /wcs/api/solr/*