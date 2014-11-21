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

	  
# javascript per la creazione dei gruppi per I FLUSSI DOCUMENTALI
/*global execution, people, logger */

// ********* PARAMETRI ********
var newGroup, model, parentGroup, ElencoGruppi, GruppoPADRE, nomeGruppoPADRE, i, j, authorityService, cnrutils, nomeGruppo, zonaLdap, zonaFlussi, zonaAlfresco, zonaDefault, authorityService, gruppo, ElencoGruppoDSFTM, ElencoGruppoMISSIONI, ElencoGruppoATTESTATI;
// CREAZIONE GRUPPI

// CREAZIONE GRUPPO DSFTM
ElencoGruppoDSFTM = [];
ElencoGruppoDSFTM.push({});
ElencoGruppoDSFTM[0].nome = 'REDATTORI_DSFTM';
ElencoGruppoDSFTM[0].titolo = 'REDATTORI DSFTM';
ElencoGruppoDSFTM.push({});
ElencoGruppoDSFTM[1].nome = 'REDATTORI_IPR_DSFTM';
ElencoGruppoDSFTM[1].titolo = 'REDATTORI IPR DSFTM';
ElencoGruppoDSFTM.push({});
ElencoGruppoDSFTM[2].nome = 'VALIDATORI_DSFTM';
ElencoGruppoDSFTM[2].titolo = 'VALIDATORI DSFTM';
ElencoGruppoDSFTM.push({});
ElencoGruppoDSFTM[3].nome = 'DIRETTORE_DSFTM';
ElencoGruppoDSFTM[3].titolo = 'DIRETTORE DSFTM';
ElencoGruppoDSFTM.push({});
ElencoGruppoDSFTM[4].nome = 'PROTOCOLLO_DSFTM';
ElencoGruppoDSFTM[4].titolo = 'PROTOCOLLO DSFTM';
ElencoGruppoDSFTM.push({});
ElencoGruppoDSFTM[5].nome = 'RESPONSABILI_FLUSSO_DSFTM';
ElencoGruppoDSFTM[5].titolo = 'RESPONSABILI FLUSSO DSFTM';

// CREAZIONE GRUPPO MISSIONI
ElencoGruppoMISSIONI = [];
ElencoGruppoMISSIONI.push({});
ElencoGruppoMISSIONI[0].nome = 'RESPONSABILI_MISSIONI';
ElencoGruppoMISSIONI[0].titolo = 'RESPONSABILI MISSIONI';
ElencoGruppoMISSIONI.push({});
ElencoGruppoMISSIONI[1].nome = 'RESPONSABILI_MODULO';
ElencoGruppoMISSIONI[1].titolo = 'RESPONSABILI MODULO';
ElencoGruppoMISSIONI.push({});
ElencoGruppoMISSIONI[2].nome = 'DIRETTORI_ISTITUTO';
ElencoGruppoMISSIONI[2].titolo = 'DIRETTORI ISTITUTO';
ElencoGruppoMISSIONI.push({});
ElencoGruppoMISSIONI[3].nome = 'DIRETTORI_SPESA';
ElencoGruppoMISSIONI[3].titolo = 'DIRETTORI SPESA';

// CREAZIONE GRUPPO ATTESTATI
ElencoGruppoATTESTATI = [];
ElencoGruppoATTESTATI.push({});
ElencoGruppoATTESTATI[0].nome = 'RESPONSABILI_ATTESTATI';
ElencoGruppoATTESTATI[0].titolo = 'RESPONSABILI ATTESTATI';

// CREAZIONE GRUPPO TOTALE
var ElencoCompleto = [];
ElencoCompleto.push({});
ElencoCompleto[0].padre = 'DSFTM';
ElencoCompleto[0].titolopadre = 'Dipartimento Scienze Fisiche e Tecnologie della Materia';
ElencoCompleto[0].figli = ElencoGruppoDSFTM;
ElencoCompleto.push({});
ElencoCompleto[1].padre = 'MISSIONI';
ElencoCompleto[1].titolopadre = 'Gruppo per il flusso MISSIONI';
ElencoCompleto[1].figli = ElencoGruppoMISSIONI;
ElencoCompleto.push({});
ElencoCompleto[2].padre = 'ATTESTATI';
ElencoCompleto[2].titolopadre = 'Gruppo per il flusso ATTESTATI';
ElencoCompleto[2].figli = ElencoGruppoATTESTATI;

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

function creaAlberoGruppi(GruppoPADRE, nomeGruppoPADRE, ElencoGruppo) {
  'use strict';
  for (i = 0; i < ElencoGruppo.length; i++) {
    logger.error("parentGroup: " + ElencoGruppo[i].nome);
    logger.error("parentGroup: " + ElencoGruppo[i].titolo);
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

  for (i = 0; i < ElencoGruppo.length; i++) {
    newGroup = people.getGroup("GROUP_" + ElencoGruppo[i].nome);
    if (!newGroup) {
      newGroup = people.createGroup(parentGroup, ElencoGruppo[i].nome);
      newGroup.properties["cm:authorityDisplayName"] = ElencoGruppo[i].titolo;
      model.newGroup = newGroup;
      newGroup.save();
    } else {
      logger.error("gruppo esistente: " + newGroup.properties["cm:authorityDisplayName"]);
    }
    settaGruppoInZoneFlussi("GROUP_" +  ElencoGruppo[i].nome);
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
for (j = 0; j < ElencoCompleto.length; j++) {
  creaAlberoGruppi(ElencoCompleto[j].padre, ElencoCompleto[j].titolopadre, ElencoCompleto[j].figli);
}
