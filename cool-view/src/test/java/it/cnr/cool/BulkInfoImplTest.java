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

package it.cnr.cool;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import it.cnr.bulkinfo.BulkInfo;
import it.cnr.bulkinfo.BulkInfoImpl;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class BulkInfoImplTest {

  private static String DEFAULT_RESOURCE = "cmis_folder_test.xml";

  /**
   * Verifico che compila e che sono in grado di creare un oggetto vuote
   */
  //@Test
  public void testCreateEmptyBulkInfoNew() {

    BulkInfo bi = new BulkInfoImpl();
    assertTrue(bi != null);
  }

  /**
   * Creo un BulkInfoNew predefinito e verifico che alcune proprieta' siano valorizzate
   * 
   * @throws IOException
   * @throws DocumentException
   */
  //@Test
  public void testCreateNonEmptyBulkInfoNew() throws IOException, DocumentException {
    BulkInfo bi = getExampleBulkInfoNew();
    assertTrue(bi != null);
    // test some random fields
    assertTrue(bi.getClass() != null);
    assertTrue(bi.getFieldProperties() != null);
    assertEquals("cmis:folder", bi.getCmisTypeName());
  }

  /**
   * Creo un bulkinfo particolare e verifico che siano presenti TUTTI i 
   * form, column e freesearch
   * 
   * @throws IOException
   * @throws DocumentException
   */
  //@Test
  public void testCreateNonEmptyBulkInfoNewDetailed() throws IOException, DocumentException {
    BulkInfo bi = getExampleBulkInfoNew("F_jconon_call_folder_test.xml");
    assertTrue(bi != null);

    assertTrue(bi.getClass() != null);
    assertTrue(bi.getFieldProperties() != null);
    assertEquals("F:jconon_call:folder", bi.getCmisTypeName());

    //get all column sets
    assertTrue(bi.getColumnSets().containsKey("default"));
    assertTrue(bi.getColumnSets().containsKey("home"));
    //count columns for a random column set
    assertTrue(bi.getColumnSet("home").size() == 19);

    //get all forms
    assertTrue(bi.getForms().size() == 10);
    assertTrue(bi.getForms().containsKey("default"));
    assertTrue(bi.getForms().containsKey("affix_sezione_1"));
    assertTrue(bi.getForms().containsKey("affix_sezione_2"));
    assertTrue(bi.getForms().containsKey("affix_sezione_3"));
    assertTrue(bi.getForms().containsKey("affix_sezione_permessi"));
    assertTrue(bi.getForms().containsKey("affix_sezione_commissione"));
    assertTrue(bi.getForms().containsKey("affix_sezione_allegati"));
    assertTrue(bi.getForms().containsKey("create_child_call"));
    assertTrue(bi.getForms().containsKey("filters"));
    assertTrue(bi.getForms().containsKey("all-filters"));

    //count properties for a random form
    assertTrue(bi.getForm("filters").size() == 5);
    //non posso testare i singoli formFieldProperty

    //get all print forms
    assertTrue(bi.getPrintForms().containsKey("default"));

    //get all free search sets 
    assertTrue(bi.getFreeSearchSets().containsKey("default"));
  }

  /**
   * Creo un Bulkinfo predefinito
   * @return
   * @throws IOException
   * @throws DocumentException
   */
  private BulkInfo getExampleBulkInfoNew() throws IOException,
  DocumentException {
    return getExampleBulkInfoNew(null);
  }

  /**
   * Creo un BulkInfoNew, null per quello predefinito
   * @param resource
   * @return
   * @throws IOException
   * @throws DocumentException
   */
  private BulkInfo getExampleBulkInfoNew(String resource) throws IOException,
  DocumentException {
    String path ="/bulkInfo/";
    if(resource == null) resource = DEFAULT_RESOURCE;
    String xml = IOUtils.toString(this.getClass().getResourceAsStream(path + resource));

    Document doc = org.dom4j.DocumentHelper.parseText(xml);
    BulkInfo bi = new BulkInfoImpl("testBulkinfo", doc);
    return bi;
  }

  private Configuration getConfig() {
    // construct template config
    Configuration config = new Configuration();
    //        config.setCacheStorage(new MruCacheStorage(cacheSize, cacheSize << 1));
    config.setTemplateUpdateDelay(0);
    config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    config.setLocalizedLookup(true);
    config.setOutputEncoding("UTF-8");
    //        if (getDefaultEncoding() != null)
    //        {
    //            config.setDefaultEncoding(getDefaultEncoding());
    //        }
    //        
    //        if (getTemplateLoader() != null)
    //        {
    //            config.setTemplateLoader(getTemplateLoader());
    //        }
    return config;
  }

  @Test
  public void testSomething() throws IOException,
  DocumentException {

    BulkInfo bi = getExampleBulkInfoNew("accountBulkInfo_test.xml");

    assertNotNull(bi);
  }

}
