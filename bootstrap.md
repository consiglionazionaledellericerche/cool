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
var newGroup, model, parentGroup, ElencoGruppi, GruppoDSFTM, nomeGruppoDSFTM, i;

GruppoDSFTM = 'DSFTM';
nomeGruppoDSFTM = 'Dipartimento di Scienze Fisiche';
ElencoGruppi = [];
ElencoGruppi.push({});
ElencoGruppi[0].nome = 'REDATTORI_DSFTM';
ElencoGruppi[0].titolo = 'REDATTORI DSFTM';
ElencoGruppi.push({});
ElencoGruppi[1].nome = 'REDATTORI_IPR_DSFTM';
ElencoGruppi[1].titolo = 'REDATTORI IPR DSFTM';
ElencoGruppi.push({});
ElencoGruppi[2].nome = 'VALIDATORI_DSFTM';
ElencoGruppi[2].titolo = 'VALIDATORI DSFTM';
ElencoGruppi.push({});
ElencoGruppi[3].nome = 'DIRETTORE_DSFTM';
ElencoGruppi[3].titolo = 'DIRETTORE DSFTM';
ElencoGruppi.push({});
ElencoGruppi[4].nome = 'RESPONSABILI_FLUSSO_DSFTM';
ElencoGruppi[4].titolo = 'RESPONSABILI FLUSSO DSFTM';
ElencoGruppi.push({});
ElencoGruppi[5].nome = 'PROTOCOLLO_DSFTM';
ElencoGruppi[5].titolo = 'PROTOCOLLO DSFTM';

for (i = 0; i < ElencoGruppi.length; i++) {
  logger.info("parentGroup: " + ElencoGruppi[i].nome);
  logger.info("parentGroup: " + ElencoGruppi[i].titolo);
}

parentGroup = people.getGroup("GROUP_" + GruppoDSFTM);
if (!parentGroup) {
  parentGroup = people.createGroup(GruppoDSFTM);
  parentGroup.properties["cm:authorityDisplayName"] = nomeGruppoDSFTM;
  parentGroup.save();
  logger.info("parentGroup: " + parentGroup.authorityDisplayName);
} else {
  logger.info("gruppo esistente: " + parentGroup.properties["cm:authorityDisplayName"]);
}

for (i = 0; i < ElencoGruppi.length; i++) {
  newGroup = people.getGroup("GROUP_" + ElencoGruppi[i].nome);
  if (!newGroup) {
    newGroup = people.createGroup(parentGroup, ElencoGruppi[i].nome);
    newGroup.properties["cm:authorityDisplayName"] = ElencoGruppi[i].titolo;
    model.newGroup = newGroup;
    newGroup.save();
  } else {
    logger.info("gruppo esistente: " + newGroup.properties["cm:authorityDisplayName"]);
  }
}

