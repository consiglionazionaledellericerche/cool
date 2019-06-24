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

package it.cnr.cool.frontOfficeHandler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.model.ACLType;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolClientException;
import it.cnr.cool.security.PermissionEnum;
import it.cnr.cool.service.frontOffice.TypeDocument;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.util.StringUtil;
import it.cnr.si.opencmis.criteria.Criteria;
import it.cnr.si.opencmis.criteria.CriteriaFactory;
import it.cnr.si.opencmis.criteria.Order;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Principal;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
@Service
public class AlfrescoHandler implements ILoggerHandler, InitializingBean {

	@Autowired
	private CMISService cmisService;

	@Autowired
	private OperationContext cmisCountOperationContext;
	private OperationContext maxOperationContext;
	private OperationContext aclOperationContext;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AlfrescoHandler.class);

	@Value("${dataDictionary.path}")
	private String dataDictionaryPath;

    public void afterPropertiesSet() {
		maxOperationContext = new OperationContextImpl(
				cmisCountOperationContext);
		aclOperationContext = new OperationContextImpl(
				cmisCountOperationContext);
		aclOperationContext.setIncludeAcls(true);
	}

	@Override
	public String write(String json, TypeDocument typeDocument) {
		Session adminSession = cmisService.createAdminSession();
		Folder parent = null;
		Document doc = null;
		String path = "";
		switch (typeDocument) {
		case Notice:
			path = dataDictionaryPath + "/" + TypeDocument.Notice.getFolder();
			break;
		case Log:
			path = dataDictionaryPath + "/" + TypeDocument.Log.getFolder();
			break;
		case Faq:
			path = dataDictionaryPath + "/" + TypeDocument.Faq.getFolder();
			break;
		}
		try {
			parent = (Folder) adminSession.getObjectByPath(path);
		} catch (CmisObjectNotFoundException ex) {
			// se non esiste la cartella la creo
			LOGGER.debug("folder {} does not exist", path, ex);
			String folderName = path.substring(path.lastIndexOf("/") + 1);
			String rootPath = path.substring(0, path.lastIndexOf("/"));
			CmisObject source = adminSession.getObjectByPath(rootPath);
			try {
				Map<String, Object> propertiesFolder = new HashMap<String, Object>();
				propertiesFolder.put(PropertyIds.NAME, folderName);
				propertiesFolder.put(PropertyIds.OBJECT_TYPE_ID,
						BaseTypeId.CMIS_FOLDER.value());
				ObjectId foderId = adminSession.createFolder(propertiesFolder,
						source);
				parent = (Folder) adminSession.getObject(foderId);
			} catch (CmisObjectNotFoundException ex1) {
				LOGGER.error("Errore nella creazione della cartella "
						+ folderName, ex1);
			}
		}
		String name = null;
		JsonObject root;
		List<Ace> newAces = new ArrayList<Ace>();

		try {
			byte[] content = null;
			root = new JsonParser().parse(json).getAsJsonObject();
			Map<String, Object> properties = new HashMap<String, Object>();

			switch (typeDocument) {
			case Log:
				properties = propertiesLog(root);
				content = root.toString().getBytes();
				break;
			case Notice:
				properties = propertiesNotice(root, newAces);
				String text = properties.get(
						CoolPropertyIds.NOTICE_TEXT.value()).toString();
				content = text.getBytes();
				break;
			case Faq:
				properties = propertiesFaq(root);
				content = root.toString().getBytes();
				break;
			}
			InputStream stream = new ByteArrayInputStream(content);
			ContentStream contentStream = null;

			if (typeDocument == TypeDocument.Log)
				contentStream = new ContentStreamImpl(name,
						BigInteger.valueOf(content.length),
						MimeTypes.JSON.mimetype(), stream);
			else
				contentStream = new ContentStreamImpl(name,
						BigInteger.valueOf(content.length),
						MimeTypes.TEXT.mimetype(), stream);
			// codice per editare le notice e le faq
			if (root.has("nodeRefToEdit")) {
				String nodeRefToEdit = root.get("nodeRefToEdit").getAsString();
				doc = adminSession.getLatestDocumentVersion(
						adminSession.createObjectId(nodeRefToEdit),
						aclOperationContext);
				doc = (Document) doc.updateProperties(properties);

				// se la notice è stata modificata ha gli acl
				List<Ace> removeAces = new ArrayList<Ace>();
				for (Ace ace : doc.getAcl().getAces()) {
					List<String> permissionsConsumer = new ArrayList<String>();
					permissionsConsumer.add(ACLType.Consumer.name());
					Ace aceContributor = new AccessControlEntryImpl(
							ace.getPrincipal(), permissionsConsumer);
					removeAces.add(aceContributor);
				}
				doc.applyAcl(newAces, removeAces, null);
				doc.setContentStream(contentStream, true);
			} else {
				doc = parent.createDocument(properties, contentStream, VersioningState.MAJOR);
				// se la notice è stata creata, ovviamente non avrà gli acl
				// vecchi
                doc.applyAcl(newAces, null, AclPropagation.OBJECTONLY);
			}
		} catch (Exception e) {
			throw new CoolClientException(
					"errore nella scrittura del documento", e);
		}
		return doc.getId();
	}

	private Map<String, Object> propertiesLog(JsonObject root) {
		Map<String, Object> properties = new HashMap<String, Object>();
		int codeTypeError;
		ErrorCode typeLog;
		String name;
		String user;
		String application;
		codeTypeError = root.get("codice").getAsInt();
		properties.put(CoolPropertyIds.LOGGER_CODE.value(), codeTypeError);

		typeLog = ErrorCode.fromValue(codeTypeError);
		if (typeLog != null) {
			name = "log_" + typeLog + "_" + new Date().getTime() + ".json";
			properties.put(CoolPropertyIds.LOGGER_TYPE.value(), typeLog.name());
		} else {
			name = "log_" + new Date().getTime() + ".json";
		}
		user = root.get("mappa").getAsJsonObject().get("user").getAsString();
		if (user != null)
			properties.put(CoolPropertyIds.LOGGER_USER.value(), user);

		application = root.get("mappa").getAsJsonObject().get("application")
				.getAsString();
		if (application != null)
			properties.put(CoolPropertyIds.LOGGER_APPLICATION.value(),
					application);
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				CoolPropertyIds.LOGGER_TYPE_NAME.value());
		properties.put(PropertyIds.NAME, name);
		return properties;
	}

	private Map<String, Object> propertiesFaq(JsonObject root)
			throws ParseException {
		Map<String, Object> properties = new HashMap<String, Object>();
		if (!root.get(CoolPropertyIds.FAQ_TYPE.value()).isJsonNull()) {
			String type = root.get(CoolPropertyIds.FAQ_TYPE.value())
					.getAsString();
			properties.put(CoolPropertyIds.FAQ_TYPE.value(), type);
		} else {
			properties.put(CoolPropertyIds.FAQ_TYPE.value(), null);
		}
		if (!root.get(CoolPropertyIds.FAQ_QUESTION.value()).isJsonNull()) {
			String question = root.get(CoolPropertyIds.FAQ_QUESTION.value())
					.getAsString();
			properties.put(CoolPropertyIds.FAQ_QUESTION.value(), question);
		}
		if (!root.get(CoolPropertyIds.FAQ_ANSWER.value()).isJsonNull()) {
			String answer = root.get(CoolPropertyIds.FAQ_ANSWER.value())
					.getAsString();
			properties.put(CoolPropertyIds.FAQ_ANSWER.value(), answer);
		}
		if (!root.get(CoolPropertyIds.FAQ_DATA.value()).isJsonNull()) {
			String dataFaq = root.get(CoolPropertyIds.FAQ_DATA.value())
					.getAsString();
			properties.put(CoolPropertyIds.FAQ_DATA.value(),
					StringUtil.CMIS_DATEFORMAT.parse(dataFaq));
		}
		if (!root.get(CoolPropertyIds.FAQ_NUMBER.value()).isJsonNull()) {
			int faqNumber = root.get(CoolPropertyIds.FAQ_NUMBER.value())
					.getAsInt();
			properties.put(CoolPropertyIds.FAQ_NUMBER.value(), faqNumber);
		} else {
			properties.put(
					CoolPropertyIds.FAQ_NUMBER.value(),
					getMax(CoolPropertyIds.FAQ_QUERY_NAME.value(),
							CoolPropertyIds.FAQ_NUMBER.value()));
		}
		if (!root.getAsJsonObject().get(CoolPropertyIds.FAQ_SHOW.value())
				.isJsonNull()) {
			boolean pubblica = root.get(CoolPropertyIds.FAQ_SHOW.value())
					.getAsBoolean();
			properties.put(CoolPropertyIds.FAQ_SHOW.value(), pubblica);
		}
		properties.put(PropertyIds.NAME, "faq_" + new Date().getTime());
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				CoolPropertyIds.FAQ_TYPE_NAME.value());
		return properties;
	}

	private Map<String, Object> propertiesNotice(JsonObject root,
			List<Ace> newAces) throws ParseException {
		Map<String, Object> properties = new HashMap<String, Object>();
		if (!root.get(CoolPropertyIds.NOTICE_TEXT.value()).isJsonNull()) {
			properties
					.put(CoolPropertyIds.NOTICE_TEXT.value(),
							root.get(CoolPropertyIds.NOTICE_TEXT.value())
									.getAsString());
		}
		if (!root.getAsJsonObject().get(CoolPropertyIds.NOTICE_DATA.value())
				.isJsonNull()) {
			String dataNotice = root.get(CoolPropertyIds.NOTICE_DATA.value())
					.getAsString();
			Calendar a = Calendar.getInstance();
			a.setTime(StringUtil.CMIS_DATEFORMAT.parse(dataNotice));
			properties.put(CoolPropertyIds.NOTICE_DATA.value(), a.getTime());
		}
		if (!root.get(CoolPropertyIds.NOTICE_TYPE.value()).isJsonNull()) {
			String type = root.get(CoolPropertyIds.NOTICE_TYPE.value())
					.getAsString();
			properties.put(CoolPropertyIds.NOTICE_TYPE.value(), type);
		} else {
			properties.put(CoolPropertyIds.NOTICE_TYPE.value(), null);
		}
		if (!root.getAsJsonObject().get(CoolPropertyIds.NOTICE_TITLE.value())
				.isJsonNull()
				|| ! root.get(CoolPropertyIds.NOTICE_TITLE.value()).getAsString().equals("")) {
			String title = root.get(CoolPropertyIds.NOTICE_TITLE.value())
					.getAsString();
			properties.put(CoolPropertyIds.NOTICE_TITLE.value(), title);
		}
		if (!root.getAsJsonObject().get(CoolPropertyIds.NOTICE_STYLE.value())
				.isJsonNull()) {
			String noticeStyle = root.get(CoolPropertyIds.NOTICE_STYLE.value())
					.getAsString();
			properties.put(CoolPropertyIds.NOTICE_STYLE.value(), noticeStyle);
		} else {
			// con lo style a null non viene sparato in video ma solo mostrato
			// tra gli avvisi
			properties.put(CoolPropertyIds.NOTICE_STYLE.value(), null);
		}
		if (!root.getAsJsonObject()
				.get(CoolPropertyIds.NOTICE_SCADENZA.value()).isJsonNull()) {
			String scadenzaNotice = root.get(
					CoolPropertyIds.NOTICE_SCADENZA.value()).getAsString();
			Calendar a = Calendar.getInstance();
			a.setTime(StringUtil.CMIS_DATEFORMAT.parse(scadenzaNotice));
			properties
					.put(CoolPropertyIds.NOTICE_SCADENZA.value(), a.getTime());
		}
		if (!root.get(CoolPropertyIds.NOTICE_NUMBER.value()).isJsonNull()) {
			int noticeNumber = root.get(CoolPropertyIds.NOTICE_NUMBER.value())
					.getAsInt();
			properties.put(CoolPropertyIds.NOTICE_NUMBER.value(), noticeNumber);
		} else {
			properties.put(
					CoolPropertyIds.NOTICE_NUMBER.value(),
					getMax(CoolPropertyIds.NOTICE_QUERY_NAME.value(),
							CoolPropertyIds.NOTICE_NUMBER.value()));
		}
		if (!root.getAsJsonObject()
				.get(CoolPropertyIds.NOTICE_AUTHORITY.value()).isJsonNull()) {
			String groupConsumer = root.get(
					CoolPropertyIds.NOTICE_AUTHORITY.value()).getAsString();
			properties.put(CoolPropertyIds.NOTICE_AUTHORITY.value(),
					groupConsumer);
			// aggiungo l'utente indicato nel widget come consumatore (oltre a
			// admin come coordinatore (cancellazione, modifica) ereditato
			// dalla folder)
			List<String> permissionsConsumer = new ArrayList<String>();
			permissionsConsumer.add(ACLType.Consumer.name());
            permissionsConsumer.add(PermissionEnum.CMIS_READ.value());
			Principal principal;
			principal = new AccessControlPrincipalDataImpl(groupConsumer);
			Ace aceContributor = new AccessControlEntryImpl(principal,
					permissionsConsumer);
            newAces.add(aceContributor);
		}
		properties.put(PropertyIds.NAME, "Notice_" + new Date().getTime());
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				CoolPropertyIds.NOTICE_TYPE_NAME.value());
		return properties;
	}

	// restituisce il max + 1 del notice:number/faq:number tra le notice/faq
	// create
	public int getMax(String queryName, String property) {
		Session session = cmisService.createAdminSession();
		Criteria criteria = CriteriaFactory.createCriteria(queryName);
		// recupero solo il campo notice:number ordinato in maniera decrescente
		criteria.addColumn(property);
		criteria.addOrder(Order.desc(property));
		ItemIterable<QueryResult> queryResult = criteria.executeQuery(session,
				false, maxOperationContext);
		QueryResult resultMax = queryResult.getPage(1).iterator().next();
		int max;
		try {
			max = ((BigInteger) resultMax.getPropertyValueById(property))
					.intValue();
		} catch (NullPointerException e) {
			LOGGER.error("error while performing get max {} {}", queryName, property, e);
			// se nn trovo nessun avviso:number / faq:number parto da 0 (quindi
			// restituisco 1)
			max = 0;
		}
		return max + 1;
	}

	public void setDataDictionaryPath(String dataDictionaryPath) {
		this.dataDictionaryPath = dataDictionaryPath;
	}
}