/*global execution, companyhome, logger, utils, cnrutils, use, search, task, actions, bpm_workflowDescription, wfcnr_wfCounterId, bpm_package, bpm_comment, bpm_assignee, bpm_groupAssignee, bpm_workflowDueDate, bpm_workflowPriority, initiator, people, wfCommon,wfvarNomeFlusso, arubaSign */
var wfFlussoAttestati = (function () {
  "use strict";
  //Variabili Globali
  //var nomeFlusso = "AUTORIZZAZIONI DSFTM";

  function setNomeFlusso() {
    execution.setVariable('wfvarNomeFlusso', 'FLUSSO ATTESTATI');
    execution.setVariable('wfvarTitoloFlusso', 'FLUSSO_ATTESTATI');
    logger.error("wfFlussoAttestati.js -- wfvarNomeFlusso: " + execution.getVariable('wfvarNomeFlusso'));
  }

  function setProcessVarIntoTask() {
    logger.error("wfFlussoAttestati.js -- setProcessVarIntoTask");
    if (bpm_workflowDueDate !== undefined && bpm_workflowDueDate !== null) {
      task.dueDate = bpm_workflowDueDate;
    }
    if (bpm_workflowPriority !== undefined && bpm_workflowPriority !== null) {
      task.priority = bpm_workflowPriority;
    }
    if (bpm_comment !== undefined && bpm_comment !== null) {
      task.setVariable('bpm_comment', bpm_comment);
    }
    logger.error("wfFlussoAttestati.js -- set bpm_workflowDueDate " +  bpm_workflowDueDate + " bpm_workflowPriority: " + bpm_workflowPriority + " bpm_comment: " + bpm_comment);
  }

  function settaGruppi() {
    logger.error("wfFlussoAttestati.js -- settaGruppi");
    execution.setVariable('wfvarGruppoATTESTATI', 'GROUP_ATTESTATI');
  }

  function settaStartProperties() {
    var workflowPriority, utenteRichiedente;
    logger.error("wfFlussoAttestati.js -- settaStartProperties");
    workflowPriority = execution.getVariable('bpm_workflowPriority');
    if (bpm_workflowPriority === 'undefined') {
      execution.setVariable('bpm_workflowPriority', 3);
    }
    if ((execution.getVariable('bpm_dueDate') !== null) && (execution.getVariable('bpm_dueDate') !== undefined)) {
      execution.setVariable('bpm_dueDate', execution.getVariable('bpm_workflowDueDate'));
    }
    logger.error("wfFlussoDSFTM.js -- get bpm_dueDate: " + execution.getVariable('bpm_dueDate'));
    if ((execution.getVariable('cnrattestati_userNameRichiedente') !== null) && (execution.getVariable('cnrattestati_userNameRichiedente') !== undefined)) {
      utenteRichiedente = people.getPerson(execution.getVariable('cnrattestati_userNameRichiedente'));
      logger.error("wfFlussoDSFTM.js -- utenteRichiedente: " + utenteRichiedente.properties.userName);
      execution.setVariable('wfvarUtenteRichiedente', utenteRichiedente.properties.userName);
    }
  }


  function flussoAttestatiSartSettings() {
    logger.error("wfFlussoAttestati.js -- flussoAttestatiSartSettings");
    //SET GRUPPI
    settaGruppi();
    settaStartProperties();
    wfCommon.settaDocPrincipale(bpm_package.children[0]);
  }

  function notificaMailGruppo(gruppoDestinatariMail, tipologiaNotifica) {
    var members, testo, isWorkflowPooled, destinatario, i;
    logger.error("wfFlussoAttestati.js -- notificaMail");
    members = people.getMembers(gruppoDestinatariMail);
    testo = "Notifica di scadenza di un flusso documentale";
    isWorkflowPooled = true;
    logger.error("FLUSSO ATTESTATI - invia notifica ai membri del gruppo: " + gruppoDestinatariMail.properties.authorityName);
    for (i = 0; i < members.length; i++) {
      destinatario = members[i];
      logger.error("FLUSSO ATTESTATI - invia notifica a : " + destinatario.properties.userName + " del gruppo: " + gruppoDestinatariMail.properties.authorityName);
      wfCommon.inviaNotifica(destinatario, testo, isWorkflowPooled, gruppoDestinatariMail, execution.getVariable('wfvarNomeFlusso'), tipologiaNotifica);
    }
  }

  function notificaMailSingolo(destinatario, tipologiaNotifica) {
    var testo, isWorkflowPooled, gruppoDestinatariMail;
    logger.error("wfFlussoAttestati.js -- notificaMail");
    isWorkflowPooled = false;
    gruppoDestinatariMail = "GENERICO";
    testo = "Notifica di scadenza di un flusso documentale";
    logger.error("FLUSSO ATTESTATI - invia notifica a : " + destinatario.properties.userName);
    wfCommon.inviaNotifica(destinatario, testo, isWorkflowPooled, gruppoDestinatariMail, execution.getVariable('wfvarNomeFlusso'), tipologiaNotifica);
  }

  function eliminaPermessi(nodoDocumento) {
    // elimina tutti i permessi preesistenti
    var permessi,  i;
    permessi = nodoDocumento.getPermissions();
    nodoDocumento.setInheritsPermissions(false);
    for (i = 0; i < permessi.length; i++) {
      nodoDocumento.removePermission(permessi[i].split(";")[2], permessi[i].split(";")[1]);
      logger.error(i + ") rimuovo permesso: " + permessi[i].split(";")[2] + " a " + permessi[i].split(";")[1]);
    }
    nodoDocumento.setOwner('spaclient');
    logger.error("wfFlussoAttestati.js -- setPermessi assegno l'ownership del documento: a " + nodoDocumento.getOwner());
  }

  function setPermessiValidazione(nodoDocumento) {
    eliminaPermessi(nodoDocumento);
    if (people.getGroup(execution.getVariable('wfvarGruppoATTESTATI'))) {
      nodoDocumento.setPermission("Consumer", execution.getVariable('wfvarGruppoATTESTATI'));
      logger.error("wfFlussoAttestati.js -- setPermessiValidazione con wfvarGruppoATTESTATI: " + execution.getVariable('wfvarGruppoATTESTATI'));
    }
    nodoDocumento.setPermission("Consumer", execution.getVariable('wfvarUtenteFirmatario').properties.userName);
    logger.error("wfFlussoAttestati.js -- setPermessiValidazione con wfvarUtenteFirmatario: " + execution.getVariable('wfvarUtenteFirmatario').properties.userName);
  }

  function setPermessiEndflussoAttestati(nodoDocumento) {
    eliminaPermessi(nodoDocumento);
    if (people.getGroup(execution.getVariable('wfvarGruppoATTESTATI'))) {
      nodoDocumento.setPermission("Consumer", execution.getVariable('wfvarGruppoATTESTATI'));
      logger.error("wfFlussoAttestati.js -- setPermessiEndflussoAttestati con wfvarGruppoATTESTATI: " + execution.getVariable('wfvarGruppoATTESTATI'));
    }
    nodoDocumento.setPermission("Consumer", execution.getVariable('wfvarUtenteFirmatario').properties.userName);
    logger.error("wfFlussoAttestati.js -- setPermessiEndflussoAttestati con wfvarUtenteFirmatario: " + execution.getVariable('wfvarUtenteFirmatario').properties.userName);
  }



  function validazione() {
    var nodoDoc, tipologiaNotifica;
    // --------------
    logger.error("wfFlussoAttestati.js -- get bpm_workflowDueDate: " + execution.getVariable('bpm_workflowDueDate'));
    logger.error("wfFlussoAttestati.js -- wfvarGruppoATTESTATI: " + execution.getVariable('wfvarGruppoATTESTATI'));
    logger.error("wfFlussoAttestati.js -- bpm_dueDate: " + task.getVariable('bpm_dueDate'));
    logger.error("wfFlussoAttestati.js -- bpm_priority: " + task.getVariable('bpm_priority'));
    logger.error("wfFlussoAttestati.js -- bpm_comment: " + task.getVariable('bpm_comment'));
    logger.error("wfFlussoAttestati.js -- bpm_assignee: " + task.getVariable('bpm_assignee'));
    execution.setVariable('wfvarUtenteFirmatario', bpm_assignee);
    setProcessVarIntoTask();
    task.setVariable('bpm_percentComplete', 30);
    if ((bpm_package.children[0] !== null) && (bpm_package.children[0] !== undefined)) {
      nodoDoc = bpm_package.children[0];
      wfCommon.taskStepMajorVersion(nodoDoc);
      setPermessiValidazione(nodoDoc);
    }
    // INVIO NOTIFICA
    tipologiaNotifica = 'compitoAssegnato';
    if (people.getPerson(execution.getVariable('wfvarUtenteFirmatario').properties.userName)) {
      notificaMailSingolo(people.getPerson(execution.getVariable('wfvarUtenteFirmatario').properties.userName), tipologiaNotifica);
    }
  }

  function validazioneEnd() {
    logger.error("wfFlussoAttestati.js -- bpm_assignee: " + task.getVariable('bpm_assignee').properties.userName);
    logger.error("wfFlussoAttestati.js -- wfcnr_reviewOutcome: " + task.getVariable('wfcnr_reviewOutcome'));
    logger.error("wfFlussoAttestati.js -- wfcnr_reviewOutcome: " + task.getVariable('bpm_comment'));
    execution.setVariable('wfcnr_reviewOutcome', task.getVariable('wfcnr_reviewOutcome'));
    execution.setVariable('wfvarCommento', task.getVariable('bpm_comment'));
  }

  function Approva() {
    var nodoDoc, statoFinale, formatoFirma, dataFirma, username, ufficioFirmatario, codiceDoc, commentoFirma;
    logger.error("wfFlussoAttestati.js -- Approva ");
    if ((bpm_package.children[0] !== null) && (bpm_package.children[0] !== undefined)) {
      nodoDoc = bpm_package.children[0];
      //nodoDoc.setPermission("Coordinator", execution.getVariable('wfvarUtenteFirmatario').properties["cm:userName"]);
      statoFinale = "APPROVATO";
      formatoFirma = "leggera";
      dataFirma = new Date();
      username = execution.getVariable('wfvarUtenteFirmatario').properties.userName;
      commentoFirma = execution.getVariable('wfvarCommento');
      ufficioFirmatario = 'GENERICO';
      codiceDoc = execution.getVariable('wfcnr_codiceDocumentoUfficio');
      wfCommon.setMetadatiFirma(nodoDoc, formatoFirma, username, ufficioFirmatario, dataFirma, codiceDoc, commentoFirma);
      logger.error("wfFlussoAttestati.js -- approva: firma leggera ");
      wfCommon.taskEndMajorVersion(nodoDoc, statoFinale);
      setPermessiEndflussoAttestati(nodoDoc);
    }
  }

  function Respingi() {
    var nodoDoc, statoFinale, formatoFirma, dataFirma, username, ufficioFirmatario, codiceDoc, commentoFirma;
    logger.error("wfFlussoAttestati.js -- Respingi ");
    if ((bpm_package.children[0] !== null) && (bpm_package.children[0] !== undefined)) {
      nodoDoc = bpm_package.children[0];
      statoFinale = "RESPINTO";
      formatoFirma = "non eseguita";
      dataFirma = new Date();
      username = execution.getVariable('wfvarUtenteFirmatario').properties.userName;
      commentoFirma = execution.getVariable('wfvarCommento');
      ufficioFirmatario = 'GENERICO';
      codiceDoc = execution.getVariable('wfcnr_codiceDocumentoUfficio');
      wfCommon.setMetadatiFirma(nodoDoc, formatoFirma, username, ufficioFirmatario, dataFirma, codiceDoc, commentoFirma);
      wfCommon.taskEndMajorVersion(nodoDoc, statoFinale);
      setPermessiEndflussoAttestati(nodoDoc);
    }
  }

  function flussoAttestatiEndSettings() {
    var tipologiaNotifica, destinatari;
    logger.error("wfFlussoAttestati.js -- flussoAttestatiEndSettings ");
    // INVIO NOTIFICA
    tipologiaNotifica = 'flussoCompletato';
    destinatari = execution.getVariable('wfvarGruppoATTESTATI');
    if (people.getGroup(destinatari)) {
      notificaMailGruppo(people.getGroup(destinatari), tipologiaNotifica);
    }
    if (people.getPerson(execution.getVariable('wfvarUtenteRichiedente'))) {
      notificaMailSingolo(people.getPerson(execution.getVariable('wfvarUtenteRichiedente')), tipologiaNotifica);
    }
    // --------------
  }
  return {
    setNomeFlusso : setNomeFlusso,
    flussoAttestatiSartSettings : flussoAttestatiSartSettings,
    validazione : validazione,
    validazioneEnd : validazioneEnd,
    Approva : Approva,
    Respingi : Respingi,
    flussoAttestatiEndSettings : flussoAttestatiEndSettings
  };
}

  ());
