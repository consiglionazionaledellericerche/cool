/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.bulkinfo;

import it.cnr.bulkinfo.exception.BulkInfoException;
import it.cnr.bulkinfo.exception.BulkInfoNotFoundException;
import it.cnr.bulkinfo.exception.BulkinfoKindException;
import it.cnr.bulkinfo.exception.BulkinfoNameException;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BulkInfoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoService.class);

  private static final String RESOURCEPATH = "/bulkInfo/";

  //TODO BulkInfoNotFoundException e' stato temporaneamente sostituito con return null per mantenere la compatibilita' con ApplicationModel
  /**
   * Questo metodo costruisce una mappa con alcuni parametri e un BulkInfo.
   * La struttura e' per mantenere la compatibilita' con il vecchio BulkInfo.
   * Si fanno anche i check sui parametri.
   * Se vuoi solo un BulkInfo usa l'altro metodo pubblico, find()
   *
   * @param cmisSession
   * @param type
   * @param kind
   * @param name
   * @param objectId
   * @return
   * @throws BulkInfoException
   * @throws BulkinfoKindException
   * @throws BulkinfoNameException
   * @throws BulkInfoNotFoundException
   */
  public Map<String, Object> getView(String type,	String kind, String name)
      throws BulkInfoException, BulkinfoKindException, BulkinfoNameException, BulkInfoNotFoundException {

    Map<String, Object> model = new HashMap<String, Object>();

    if (name == null || name.equals("")) {
      throw new BulkinfoNameException("Variabile nome non impostata");
    }

    if(type != null && !type.equals("")) {

      BulkInfo bi = find(type);
      if (bi != null) {

        model.put("bulkInfo", bi);

        if ("form".equals(kind)) {
          model.put("formName", name);
        } else if ("column".equals(kind)) {
          model.put("columnSetName", name);
        } else if ("find".equals(kind)) {
          model.put("freeSearchSetName", name);
        } else {
          throw new BulkinfoKindException("Variabilie kind non impostata");
        }

        model.put("url.context", "");

      }
    }
    return model;
  }


  /**
   * Questo metodo publico e' solo un livello di indirezione per costruire
   * eventuali cache.
   * La magia succede nel build()
   *
   * EXTENSION POINT
   *
   * @param bulkInfoName
   * @return
   * @throws BulkInfoNotFoundException
   */
  protected BulkInfo find(String bulkInfoName) throws BulkInfoNotFoundException {
    return build(bulkInfoName);
  }


  /**
   * Questo metodo si occupa di recuperare la definizione xml di un bulkInfo
   * Per prima cosa controlla se l'abbiamo nei resources java, senno' si cerca
   * su cmis. La ricerca si puo' fare per nome del file bulkInfo o per nome
   * del tipo cmis 1. prova a trovare il BulkInfo nel classpath, senno' ne
   * crea uno nuovo 2. inietta tutti gli aspect e parent (ricorsivamente) 3.
   * inserisci le proprietà
   *
   * Restituisce il Bulkinfo costruito o null se non è stato possibile
   * costruirlo
   *
   * @param bulkInfoName
   * @throws nothing
   * @return BulkInfoNew or null
   * @throws BulkInfoNotFoundException
   */
  protected BulkInfo build(String bulkInfoName) throws BulkInfoNotFoundException {
    LOGGER.debug("Building BulkInfo " + bulkInfoName + " for cache");

    BulkInfo bulkInfo = getOrCreate(bulkInfoName);
    injectParentAndAspects(bulkInfo);

    return bulkInfo;
  }


  protected BulkInfo getOrCreate(String bulkInfoName) throws BulkInfoNotFoundException {
    LOGGER.debug("Searching bulkInfo in Classpath : " + bulkInfoName);
    Document doc = getBulkInfoXMLFromResources(bulkInfoName);

    if(doc == null) {
      LOGGER.debug("Bulkinfo " + bulkInfoName + " not found");
      throw new BulkInfoNotFoundException(bulkInfoName);
    }

    BulkInfo bulkInfo = new BulkInfoImpl(bulkInfoName, doc);
    LOGGER.debug("Bulkinfo " + bulkInfoName + " successfully created");

    return bulkInfo;
  }

  /**
   * Cerca di recuperare un BulkInfo dal classpath restituisce null se non lo
   * trova
   *
   * @param bulkInfoName
   * @return BulkInfoNew or null
   */
  protected Document getBulkInfoXMLFromResources(String bulkInfoName) {
    Document doc = null;
    String bulkInfoNameCorrected = bulkInfoName.replaceAll(":", "_");
    try {
      InputStream is = this.getClass().getResourceAsStream(
          RESOURCEPATH + bulkInfoNameCorrected + ".xml");

      if (is != null) {
        String xml = IOUtils.toString(is);
        doc = DocumentHelper.parseText(xml);
      }
    } catch (DocumentException exp) { // log error, return null
      LOGGER.error("DocumentExcpetion with bulkInfo :" + bulkInfoNameCorrected,
          exp);
    } catch (IOException exp) { // log error, return null
      LOGGER.error("IOException with bulkInfo :" + bulkInfoNameCorrected, exp);
    }
    return doc;
  }

  // qui succede la ricorsione su find()
  protected void injectParentAndAspects(BulkInfo bi) throws BulkInfoNotFoundException {
    if (bi.getCmisExtendsName() != null) {
      BulkInfo parent = find(bi.getCmisExtendsName());
      if (parent != null) {
        bi.completeWithParent(parent, false);
      }
    }
    if (bi.getCmisImplementsName() != null
        && !bi.getCmisImplementsName().isEmpty()) {
      for (String name : bi.getCmisImplementsName().keySet()) {
        BulkInfo aspect = find(name);
        if (aspect != null) {
          bi.completeWithParent(aspect, true);
        }
      }
    }
  }

}
