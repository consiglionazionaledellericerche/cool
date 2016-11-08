package it.cnr.cool;

import it.cnr.cool.cmis.service.CMISService;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by francesco on 13/02/15.
 */

@Repository
public class BulkInfoRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoRepository.class);

    @Autowired
    private CMISService cmisService;

    private static final String RESOURCE_PATH = "/bulkInfo/";


    @Cacheable("bulkinfo-object-type")
    public ObjectType getObjectType(String bulkTypeName) {
        LOGGER.info("loading from CMIS server type: " + bulkTypeName);
        try {
            return cmisService.createAdminSession().getTypeDefinition(bulkTypeName);        	
        } catch (CmisObjectNotFoundException _ex) {
        	 LOGGER.info("Type: {} not found!",bulkTypeName);
        	return null;
        }
    }


    @Cacheable("bulkinfo-xml-document")
    public Document getXmlDocument(String bulkInfoName) {

        LOGGER.info("loading from classpath XML Document: " + bulkInfoName);

        Document doc = null;

        try {
            bulkInfoName = bulkInfoName.replaceAll(":", "_");
            InputStream is = this.getClass().getResourceAsStream(
                    RESOURCE_PATH + bulkInfoName + ".xml"); // dara' NullPointer
            // se non esiste, lo gestiamo

            if (is != null) {
                String xml = IOUtils.toString(is);
                doc = DocumentHelper.parseText(xml);

            }
        } catch (DocumentException exp) { // log error, return null
            LOGGER.error("DocumentExcpetion with bulkInfo :" + bulkInfoName,
                    exp);
        } catch (IOException exp) { // log error, return null
            LOGGER.error("IOException with bulkInfo :" + bulkInfoName, exp);
        } catch (NullPointerException exp) { // log error, return null
            LOGGER.error("NullPointerException with bulkInfo :" + bulkInfoName,
                    exp);
        }

        return doc;

    }


}
