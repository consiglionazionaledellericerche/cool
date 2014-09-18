RRDService disattivato
- Cool Classpath Store
CmisBindingListener

CoolConsoleUpdate

ResourceBundleBootstrapComponent ???

<bean id="jcononResourceController" class="it.cnr.cool.extensions.surf.mvc.CMISResourceController">

verificare il versionService

nota x jconon: cool-common-web-context e' stato fortemente smembrato

---

pageservice da verificare riga per riga

togliere getModel da common e cache...

avanzamento versione chemistry, opencmis extensions ???
puntamento a cmis 1.1

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

creare artefatto doccnr "pure", senza artefatti superflui (e.g. contabili, accounting...) [o usare profili...]

usare solo plugin tomcat7