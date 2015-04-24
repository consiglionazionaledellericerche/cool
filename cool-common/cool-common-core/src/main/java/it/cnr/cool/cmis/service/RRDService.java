package it.cnr.cool.cmis.service;

import it.cnr.cool.mail.MailService;
import it.cnr.cool.util.StringUtil;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mail.MailException;

/**
 * Remote Resource Deploy Service
 * @author mspasiano
 *
 */
public class RRDService implements InitializingBean {
    private static final Logger LOGGER= LoggerFactory.getLogger(RRDService.class);

    @Autowired
    private CMISService cmisService;

	@Autowired
	private ACLService aclService;

	@Autowired
	private MailService mailService;

	@Autowired
	private VersionService versionService;

	private String dictionaryTypeId;

	public void setDictionaryTypeId(String dictionaryTypeId) {
		this.dictionaryTypeId = dictionaryTypeId;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		if (!versionService.isProduction()) {
			LOGGER.warn("development mode, avoid scan document paths");
			return;
		} else {
			LOGGER.info("production mode");
		}
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath*:remote/**");
		Session cmisSession = cmisService.createAdminSession();
		Boolean webscriptCreated = Boolean.FALSE;
		List<Document> documentsToBeActive = new ArrayList<Document>();
		List<String> differentFiles = new ArrayList<String>();
		for (Resource resource : resources) {
			if (!resource.isReadable())
				continue;
			String urlPath = resource.getURL().toString();
			String cmisPath = URIUtil.decode(urlPath.substring(urlPath.indexOf("remote/") + 6));
			LOGGER.debug(urlPath);
			try{
				CmisObject doc = cmisSession.getObjectByPath(cmisPath);

				if (doc instanceof Document) {
					InputStream remote = ((Document) doc).getContentStream().getStream();
					InputStream local = resource.getInputStream();
					if (!StringUtil.getMd5(remote).equals(StringUtil.getMd5(local))) {
						LOGGER.error("different md5 for element " + cmisPath);
						differentFiles.add(cmisPath);
					}
				}

			}catch(CmisObjectNotFoundException _ex){
				String fileName = cmisPath.substring(cmisPath.lastIndexOf("/") + 1);
				boolean createFolder = !fileName.equalsIgnoreCase("bulkInfoMapping.js");
				CmisObject source = createPath(cmisSession, cmisPath.substring(0, cmisPath.lastIndexOf("/")), createFolder);
				if (source == null)
					continue;
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put(PropertyIds.NAME, fileName);
				properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
				String contentType = "text/plain";
				if (fileName.endsWith(".xml") && cmisPath.contains("Models")){
					properties.put(PropertyIds.OBJECT_TYPE_ID, dictionaryTypeId);
					contentType = "text/xml";
				} else if (fileName.endsWith(".jbpm.xml")) {
					properties.put(PropertyIds.OBJECT_TYPE_ID, "D:bpm:workflowDefinition");
					properties.put("bpm:engineId", "jbpm");
					contentType = "text/xml";
				} else if (fileName.endsWith(".activiti.xml")) {
					properties.put(PropertyIds.OBJECT_TYPE_ID, "D:bpm:workflowDefinition");
					properties.put("bpm:engineId", "activiti");
					contentType = "text/xml";
				} else {
					webscriptCreated = Boolean.TRUE;
				}
				InputStream is = resource.getInputStream();
				ContentStream contentStream = new ContentStreamImpl(
						fileName,BigInteger.valueOf(is.available()),
						contentType, is);
				Document doc = (Document)cmisSession.getObject(cmisSession.createDocument(properties, source,
					contentStream, VersioningState.MAJOR));
				if (fileName.endsWith(".jbpm.xml")) {
					properties.put("bpm:definitionDeployed", Boolean.TRUE);
					doc.updateProperties(properties);
				}
				if (fileName.endsWith(".xml") && cmisPath.contains("Models")){
					try {
						properties.put("cm:modelActive", Boolean.TRUE);
						doc.updateProperties(properties);
					} catch (Exception ex) {
						LOGGER.error("Cannot activate Model:"+fileName);
						documentsToBeActive.add(doc);
					}
				}
				if (fileName.equalsIgnoreCase("bulkInfoMapping.js")){
					aclService.setInheritedPermission(
							cmisService.getAdminSession(),
							doc.getVersionSeriesId(), false);
				}
			}
		}
		while (!documentsToBeActive.isEmpty()) {
			for (Iterator<Document> iterator = documentsToBeActive.iterator(); iterator.hasNext();) {
				Document document = iterator.next();
				try {
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put("cm:modelActive", Boolean.TRUE);
					document.updateProperties(properties);
					iterator.remove();
				} catch (Exception ex) {
					LOGGER.warn("Cannot activate Model:" + document.getName());
				}
			}
		}
		/*Reset dei webscript sul repository CMIS*/
		if (webscriptCreated){
			String link = cmisService.getBaseURL().concat(
					"service/index?reset=on");
			Response resp = CmisBindingsHelper.getHttpInvoker(
					cmisService.getAdminSession()).invokePOST(
					new UrlBuilder(link), "text/html", null,
					cmisService.getAdminSession());
			LOGGER.debug("Refresh Web Scripts has responded: "+ resp.getResponseMessage());
		}

		if (!differentFiles.isEmpty()) {
			String text = cmisSession.getRepositoryInfo().getProductName()
					+ " " + cmisSession.getRepositoryInfo().getProductVersion();

            for (String s : differentFiles) {
                text += "<br>" + s;
            }
			String address = InetAddress.getLocalHost().getHostAddress();
			try {
				mailService.send("md5 " + RRDService.class.getSimpleName()
						+ " " + cmisService.getBaseURL() + " " + address, text);
			} catch (MailException e) {
				LOGGER.warn("unable to send mail " + text, e);
			}
		}

	}

	private CmisObject createPath(Session cmisSession, String cmisPath, boolean createFolder) {
		StringTokenizer tokens = new StringTokenizer(cmisPath, "/");
		StringBuffer relativePath = new StringBuffer();
		CmisObject cmisObject = null;
		while(tokens.hasMoreTokens()){
			String folderName = tokens.nextToken();
			relativePath.append("/").append(folderName);
			try{
				cmisObject = cmisSession.getObjectByPath(relativePath.toString());
			}catch(CmisObjectNotFoundException _ex){
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
}
