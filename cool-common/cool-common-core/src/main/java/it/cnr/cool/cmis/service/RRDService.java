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

package it.cnr.cool.cmis.service;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.util.StringUtil;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Remote Resource Deploy Service
 *
 * @author mspasiano
 */
@Service
public class RRDService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RRDService.class);
    public static final String D_BPM_WORKFLOW_DEFINITION = "D:bpm:workflowDefinition";

    @Autowired
    private CMISService cmisService;

    @Autowired
    private CMISConfig cmisConfig;

    @Autowired
    private ACLService aclService;

    @Autowired
    private MailService mailService;

    @Autowired
    private VersionService versionService;

    @Value("${rrd.path}")
    private String rrdPath;

    @Value("${dictionary.model:D:cm:dictionaryModel}")
    private String dictionaryTypeId;

    @Value("${rrd.skipmd5:false}")
    private boolean skipMD5;

    @Value("${rrd.excludefiles}")
    private List<String> excludeFiles;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!versionService.isProduction()) {
            LOGGER.warn("development mode, avoid scan document paths");
            return;
        } else {
            LOGGER.info("production mode");
        }
        List<Resource> resources = getResources();
        LOGGER.info("Try to connect CMIS server {}",
                Optional.ofNullable(cmisConfig.getServerParameters())
                        .map(serverParam ->
                                Optional.ofNullable(serverParam.get(CMISConfig.CMISSessionParameter.ATOMPUB_URL.value()))
                                        .orElseGet(() -> serverParam.get(CMISConfig.CMISSessionParameter.BROWSER_URL.value()))
                        )
                        .orElse("Cannot find CMIS server URL"));
        Session cmisSession = cmisService.createAdminSession();
        Boolean webscriptCreated = Boolean.FALSE;
        List<Document> documentsToBeActive = new ArrayList<Document>();
        List<String> differentFiles = new ArrayList<String>();
        List<String> cmisPaths = new ArrayList<String>();
        for (Resource resource : resources) {
            if (!resource.isReadable())
                continue;
            String urlPath = resource.getURL().toString();

            String[] split = urlPath.split("!");
            String folderName = split[split.length - 1].split("/")[1];
            int beginIndex = urlPath.indexOf("/" + folderName + "/") + folderName.length() + 1;
            String substring = urlPath.substring(beginIndex);

            String cmisPath = URIUtil.decode(substring);
            LOGGER.debug(urlPath);
            if (cmisPaths.contains(cmisPath)) {
                continue;
            }
            try {
                CmisObject doc = cmisSession.getObjectByPath(cmisPath);
                cmisPaths.add(cmisPath);
                if (doc instanceof Document) {
                    InputStream remote = ((Document) doc).getContentStream().getStream();
                    InputStream local = resource.getInputStream();
                    if (!skipMD5 && !StringUtil.getMd5(remote).equals(StringUtil.getMd5(local))) {
                        LOGGER.error("different md5 for element " + cmisPath);
                        differentFiles.add(cmisPath);
                    }
                }
            } catch (CmisObjectNotFoundException _ex) {
                LOGGER.debug("object not found: {}", resource, _ex);
                String fileName = cmisPath.substring(cmisPath.lastIndexOf("/") + 1);
                if (fileName.length() == 0)
                    continue;
                boolean createFolder = !fileName.equalsIgnoreCase("bulkInfoMapping.js");
                CmisObject source = createPath(cmisSession, cmisPath.substring(0, cmisPath.lastIndexOf("/")), createFolder);
                if (source == null)
                    continue;
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, fileName);
                properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                String contentType = "text/plain";
                if (fileName.endsWith(".xml") && cmisPath.contains("Models")) {
                    properties.put(PropertyIds.OBJECT_TYPE_ID, dictionaryTypeId);
                    contentType = "text/xml";
                } else if (fileName.endsWith(".jbpm.xml")) {
                    properties.put(PropertyIds.OBJECT_TYPE_ID, D_BPM_WORKFLOW_DEFINITION);
                    properties.put("bpm:engineId", "jbpm");
                    contentType = "text/xml";
                } else if (fileName.endsWith(".activiti.xml")) {
                    properties.put(PropertyIds.OBJECT_TYPE_ID, D_BPM_WORKFLOW_DEFINITION);
                    properties.put("bpm:engineId", "activiti");
                    contentType = "text/xml";
                } else {
                    webscriptCreated = Boolean.TRUE;
                }
                InputStream is = resource.getInputStream();
                ContentStream contentStream = new ContentStreamImpl(
                        fileName, BigInteger.valueOf(is.available()),
                        contentType, is);
                Document doc = (Document) cmisSession.getObject(cmisSession.createDocument(properties, source,
                        contentStream, VersioningState.MAJOR));
                if (fileName.endsWith(".jbpm.xml")) {
                    properties.put("bpm:definitionDeployed", Boolean.TRUE);
                    doc.updateProperties(properties);
                }
                if (fileName.endsWith(".xml") && cmisPath.contains("Models")) {
                    try {
                        properties.put("cm:modelActive", Boolean.TRUE);
                        doc.updateProperties(properties);
                    } catch (Exception ex) {
                        LOGGER.debug("Cannot activate Model:" + fileName, ex);
                        documentsToBeActive.add(doc);
                    }
                }
                if (fileName.equalsIgnoreCase("bulkInfoMapping.js")) {
                    aclService.setInheritedPermission(
                            cmisService.getAdminSession(),
                            doc.getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), false);
                }
            }
        }
        while (!documentsToBeActive.isEmpty()) {
            for (Iterator<Document> iterator = documentsToBeActive.iterator(); iterator.hasNext(); ) {
                Document document = iterator.next();
                try {
                    Map<String, Object> properties = new HashMap<String, Object>();
                    properties.put("cm:modelActive", Boolean.TRUE);
                    document.updateProperties(properties);
                    iterator.remove();
                } catch (Exception ex) {
                    LOGGER.warn("Cannot activate Model:" + document.getName(), ex);
                }
            }
        }
        /*Reset dei webscript sul repository CMIS*/
        if (webscriptCreated) {
            String link = cmisService.getBaseURL().concat(
                    "service/index?reset=on");
            Response resp = CmisBindingsHelper.getHttpInvoker(
                    cmisService.getAdminSession()).invokePOST(
                    new UrlBuilder(link), "text/html", null,
                    cmisService.getAdminSession());
            LOGGER.debug("Refresh Web Scripts has responded: " + resp.getResponseMessage());
        }
        final String text = cmisSession.getRepositoryInfo().getProductName()
                + " " + cmisSession.getRepositoryInfo().getProductVersion();
        String address = InetAddress.getLocalHost().getHostAddress();
        Optional.ofNullable(differentFiles.stream()
                .filter(s -> !excludeFiles.stream().anyMatch(s1 -> s.contains(s1)))
                .collect(Collectors.joining("<br>")))
                .filter(s -> !s.isEmpty())
                .ifPresent(s -> {
                    try {
                        mailService.send("md5 " + RRDService.class.getSimpleName()
                                + " " + cmisService.getBaseURL() + " " + address, text + "<br>"  + s);
                    } catch (MailException e) {
                        LOGGER.warn("unable to send mail " + s, e);
                    }
                });
    }

    List<Resource> getResources() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        String[] paths = rrdPath.split(",");

        return Arrays
                .stream(paths)
                .map(path -> "classpath*:" + path + "/**")
                .peek(path -> LOGGER.info("looking for resources in {}", path))
                .flatMap(locationPattern -> {
                    try {
                        Resource[] resources = resolver.getResources(locationPattern);
                        return Arrays.stream(resources);
                    } catch (IOException e) {
                        throw new RuntimeException("unable to add resource from " + locationPattern, e);
                    }
                })
                .peek(resource -> LOGGER.info("resource: {}", resource))
                .collect(Collectors.toList());


    }

    private CmisObject createPath(Session cmisSession, String cmisPath, boolean createFolder) {
        StringTokenizer tokens = new StringTokenizer(cmisPath, "/");
        StringBuffer relativePath = new StringBuffer();
        CmisObject cmisObject = null;
        while (tokens.hasMoreTokens()) {
            String folderName = tokens.nextToken();
            relativePath.append("/").append(folderName);
            try {
                cmisObject = cmisSession.getObjectByPath(relativePath.toString());
            } catch (CmisObjectNotFoundException _ex) {
                if (!createFolder)
                    return null;
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, folderName);
                properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                if (cmisObject == null)
                    cmisObject = cmisSession.getRootFolder();
                cmisObject = cmisSession.getObject(cmisSession.createFolder(properties, cmisObject));
            }
        }
        return cmisObject;
    }

    public void setRrdPath(String rrdPath) {
        this.rrdPath = rrdPath;
    }

    public void setDictionaryTypeId(String dictionaryTypeId) {
        this.dictionaryTypeId = dictionaryTypeId;
    }

    public void setSkipMD5(Boolean skipMD5) {
        this.skipMD5 = skipMD5;
    }
}
