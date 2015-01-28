package it.cnr.cool.repository;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by francesco on 27/01/15.
 */

@Repository
public class PermissionRepository {

    public static final String RBAC = "rbac";
    @Autowired
    private CMISService cmisService;

    private String rbacPath;

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionRepository.class);

    @Cacheable(RBAC)
    public String getRbac() {

        LOGGER.debug("loading RBAC from Alfresco");

        try {
            Session session = cmisService.createAdminSession();
            InputStream is = cmisService.getDocumentInputStream(session,
                    rbacPath);
            return IOUtils.toString(is);
        } catch (IOException e) {
            LOGGER.error("error retrieving permissions", e);
        } catch (JsonParseException e) {
            LOGGER.error("error retrieving permissions", e);
        }

        return null;

    }

    @CacheEvict(value= RBAC, allEntries=true)
    public boolean update(String json) {

        LOGGER.debug(RBAC + " cache eviction due to update");

        LOGGER.debug(json);
        try {
            Session session = cmisService.createAdminSession();
            cmisService.updateDocument(session, rbacPath, json);
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    public void setRbacPath(String rbacPath) {
        LOGGER.info("using RBAC at path: " + rbacPath);
        this.rbacPath = rbacPath;
    }

}
