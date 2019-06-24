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

package it.cnr.cool.repository;

import com.google.gson.JsonParseException;
import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by francesco on 27/01/15.
 */

@Repository("permissionRepository")
public class PermissionRepositoryImpl implements PermissionRepository {

    public static final String RBAC = "rbac";
    @Autowired
    private CMISService cmisService;

    @Value("${rbac.path}")
    private String rbacPath;

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionRepositoryImpl.class);

    @Cacheable(RBAC)
    public String getRbac() {

        LOGGER.debug("loading RBAC from Alfresco");

        try {
            Session session = cmisService.createAdminSession();
            InputStream is = getDocumentInputStream(session,
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
            updateDocument(session, rbacPath, json);
        } catch (Exception e) {
            LOGGER.error("update issue {}", json, e);
            return false;
        }

        return true;
    }


    public void setRbacPath(String rbacPath) {
        LOGGER.info("using RBAC at path: " + rbacPath);
        this.rbacPath = rbacPath;
    }



    public InputStream getDocumentInputStream(Session session, String path) {
        Document document = (Document) session.getObjectByPath(path);
        return document.getContentStream().getStream();
    }



    protected void updateDocument(Session session, String path, String content) {

        Document document = (Document) session.getObjectByPath(path);
        String name = document.getName();
        String mimeType = document.getContentStreamMimeType();

        ContentStreamImpl cs = new ContentStreamImpl(name, mimeType, content);

        document.setContentStream(cs, true, true);
    }



}
