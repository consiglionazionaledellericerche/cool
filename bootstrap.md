# Creazione utenze Jconon #

	USER=admin
	PASS=admin
	HOST=http://win:8080/alfresco
	
	curl -u$USER:$PASS $HOST/service/api/people \
	  -d '{"userName":"spaclient", "password":"sp@si@n0", "firstName":"spaclient", "lastName":"spaclient","email":"spaclient"}' \
	  -H "Content-type: application/json"
	
	curl -u$USER:$PASS $HOST/service/api/people \
	  -d'{"userName":"jconon", "password":"jcononpw", "firstName":"jconon", "lastName":"jconon","email":"jconon"}' \
	  -H "Content-type: application/json"
	
	curl -u$USER:$PASS $HOST/service/api/groups/ALFRESCO_ADMINISTRATORS/children/spaclient \
	  -X POST \
	  -H "Content-type: application/json"

	  
# javascript per la creazione dei gruppi DSFTM per I FLUSSI DOCUMENTALI
/*global execution, people, logger */

// ********* PARAMETRI ********
var newGroup, model, parentGroup, ElencoGruppi, GruppoPADRE, nomeGruppoPADRE, i, authorityService, cnrutils, nomeGruppo, zonaLdap, zonaFlussi, zonaAlfresco, zonaDefault, authorityService, gruppo;
// CREAZIONI GRUPPI
GruppoPADRE = 'MISSIONI';
nomeGruppoPADRE = 'Gruppo per il flusso MISSIONI';
ElencoGruppi = [];
ElencoGruppi.push({});
ElencoGruppi[0].nome = 'RESPONSABILI_MISSIONI';
ElencoGruppi[0].titolo = 'RESPONSABILI MISSIONI';
ElencoGruppi.push({});
ElencoGruppi[1].nome = 'RESPONSABILI_MODULO';
ElencoGruppi[1].titolo = 'RESPONSABILI MODULO';
ElencoGruppi.push({});
ElencoGruppi[2].nome = 'DIRETTORI_ISTITUTO';
ElencoGruppi[2].titolo = 'DIRETTORI ISTITUTO';
ElencoGruppi.push({});
ElencoGruppi[3].nome = 'DIRETTORI_SPESA';
ElencoGruppi[3].titolo = 'DIRETTORI SPESA';

// VERIFICA/CREAZIONI ZONA FLUSSI
zonaLdap = 'AUTH.EXT.ldap1';
zonaFlussi = 'AUTH.EXT.flussi';
zonaAlfresco = 'AUTH.ALF';
zonaDefault = 'APP.DEFAULT';
authorityService = cnrutils.getBean("AuthorityService");

// ********* FUNCTIONS ********
//SETTA IL GRUPPO NELLA ZONA FLUSSI E LA RIMUOVE DAL GRUPPO ALFRESCO
function settaGruppoInZoneFlussi(nomeGruppo) {
  'use strict';
  var ListaAppo, listaZone;
  listaZone = authorityService.getAuthorityZones(nomeGruppo);
  logger.error("listaZone: " + listaZone);
  if (listaZone.contains(zonaDefault)) {
    logger.error("listaZone: contiene zona: " + zonaDefault + "? " + listaZone.contains(zonaDefault));
  }
  if (listaZone.contains(zonaAlfresco)) {

    logger.error("listaZone: contiene zona: " + zonaAlfresco + "? " + listaZone.contains(zonaAlfresco));
    ListaAppo = cnrutils.getClass('java.util.HashSet').newInstance();
    ListaAppo.add(zonaAlfresco);
    authorityService.removeAuthorityFromZones(nomeGruppo, ListaAppo);
    logger.error("Rimosso zona: " + zonaAlfresco);
  }
  if (!listaZone.contains(zonaFlussi)) {
    logger.error("listaZone: contiene zona: " + zonaFlussi + "? " + listaZone.contains(zonaFlussi));
    ListaAppo = cnrutils.getClass('java.util.HashSet').newInstance();
    ListaAppo.add(zonaFlussi);
    authorityService.addAuthorityToZones(nomeGruppo, ListaAppo);
    logger.error("Aggiunto zona: " + zonaFlussi);
  }
}

function creaAlberoGruppi() {
  'use strict';
  for (i = 0; i < ElencoGruppi.length; i++) {
    logger.error("parentGroup: " + ElencoGruppi[i].nome);
    logger.error("parentGroup: " + ElencoGruppi[i].titolo);
  }

  parentGroup = people.getGroup("GROUP_" + GruppoPADRE);
  if (!parentGroup) {
    parentGroup = people.createGroup(GruppoPADRE);
    parentGroup.properties["cm:authorityDisplayName"] = nomeGruppoPADRE;
    parentGroup.save();
    logger.error("parentGroup: " + parentGroup.authorityDisplayName);
  } else {
    logger.error("gruppo esistente: " + parentGroup.properties["cm:authorityDisplayName"]);
  }

  settaGruppoInZoneFlussi("GROUP_" + GruppoPADRE);

  for (i = 0; i < ElencoGruppi.length; i++) {
    newGroup = people.getGroup("GROUP_" + ElencoGruppi[i].nome);
    if (!newGroup) {
      newGroup = people.createGroup(parentGroup, ElencoGruppi[i].nome);
      newGroup.properties["cm:authorityDisplayName"] = ElencoGruppi[i].titolo;
      model.newGroup = newGroup;
      newGroup.save();
    } else {
      logger.error("gruppo esistente: " + newGroup.properties["cm:authorityDisplayName"]);
    }
    settaGruppoInZoneFlussi("GROUP_" +  ElencoGruppi[i].nome);
  }
}

//VERIFICO ESISTENZA ZONA FLUSSI ALTRIMENTI LA CREO
function verificaZonaFlussi() {
  'use strict';
  if (authorityService.getZone(zonaFlussi)) {
    logger.error("zona: " + zonaFlussi + " ESISTENTE: " + authorityService.getZone(zonaFlussi));
  } else {
    var nodeRefZonaFlussi =  authorityService.getOrCreateZone(zonaFlussi);
    logger.error("zona: " + zonaFlussi + " CREATA: " + nodeRefZonaFlussi);
  }
}

// ********* MAIN ********

//logger.error("authorityName: " + gruppo.properties.authorityName + " -- authorityDisplayName: " + gruppo.properties.authorityDisplayName + " -noderef: " + gruppo.nodeRef);
//logger.error("zona LDAP: " + authorityService.getZone(zonaLdap));

verificaZonaFlussi();
creaAlberoGruppi();
