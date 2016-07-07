package it.cnr.cool.service;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.exception.CoolClientException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

public class NodeService {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeService.class);

	@Autowired
	private NodeMetadataService nodeMetadataService;

	@Autowired
	private CMISService cmisService;

	private static final String CRUD_STATUS = "crudStatus";
	private static final String STATUS_TO_BE_INSERT = "INSERT";
	private static final String STATUS_TO_BE_UPDATE = "UPDATE";

	@Autowired
	private CommonsMultipartResolver resolver;
	@Autowired
	private CommonsMultipartResolver multipartResolverMax;

	public List<CmisObject> manageRequest(HttpServletRequest req,
			boolean isPOST, boolean isDELETE) {
		Session cmisSession = cmisService.getCurrentCMISSession(req);
		OperationContext oc = new OperationContextImpl(cmisSession.getDefaultContext());
		oc.setFilterString(PropertyIds.OBJECT_ID);
		String objectId = req.getParameter(PropertyIds.OBJECT_ID);
		MultipartHttpServletRequest mRequest;
		if (req.getParameter("maxUploadSize") != null && Boolean.valueOf(req.getParameter("maxUploadSize")))
			mRequest = multipartResolverMax.resolveMultipart(req);			
		else
			mRequest = resolver.resolveMultipart(req);
		if (objectId == null) {
			objectId = mRequest.getParameter(PropertyIds.OBJECT_ID);
		}
		List<CmisObject> attachments = new ArrayList<CmisObject>();

		if (isPOST) {

			String crudStatus = mRequest.getParameter(CRUD_STATUS);
			boolean isStatusToBeUpdate = STATUS_TO_BE_UPDATE
					.equalsIgnoreCase(crudStatus);
			boolean isStatusToBeInsert = crudStatus == null
					|| STATUS_TO_BE_INSERT.equalsIgnoreCase(crudStatus);

			LOGGER.info("POST, processing MultipartHttpServletRequest");

			objectId = mRequest.getParameter(PropertyIds.OBJECT_ID);

			boolean forbidArchives = Boolean.valueOf(mRequest.getParameter("forbidArchives"));

			if (isStatusToBeInsert) {

				if (mRequest.getFileMap().size() > 1) {
					throw new CoolClientException(
							"unable to manage request: multiple file attachments");
				}

				for (MultipartFile file : mRequest.getFileMap().values()) {
					if (objectId == null){
						throw new ClientMessageException("message.source.folder.empty");
					}else{
						String cmisType = mRequest.getParameter("cmis:objectTypeDocument");
						if (cmisType == null || cmisType.length() == 0 ){
							throw new ClientMessageException("message.select.type");
						} else {
							String originalFilename = file.getOriginalFilename();
							if (forbidArchives
									&& isArchive(originalFilename)) {
								throw new ClientMessageException("message.archive");
							} else{
								try{
									cmisSession.getTypeDefinition(cmisType);
									attachments.add(uploadDocument(mRequest, file, cmisSession,
											(Folder)cmisSession.getObject(objectId, oc),
											originalFilename, cmisType));
								}catch (CmisInvalidArgumentException e) {
									throw new ClientMessageException(e.getMessage());
								}catch (CmisBaseException e) {
									throw new ClientMessageException(e.getMessage());
								}
							}
						}
					}
				}


			} else if (isStatusToBeUpdate) {

				MultipartFile file = mRequest.getFile("file-0");

				if (file.getSize() == 0)
					throw new ClientMessageException(
							"Il file allegato non è leggibile!");
				if (forbidArchives && isArchive(file.getOriginalFilename())) {
					throw new ClientMessageException("message.archive");
				} else {
					try {
                        attachments.add(upgradeDocument(file,
                                (Document)cmisSession.getLatestDocumentVersion(objectId)));
					} catch (CmisObjectNotFoundException e) {
						throw new ClientMessageException("message.document.not.found");
					} catch (CmisBaseException e) {
						throw new ClientMessageException(e.getMessage());
					}
				}


			}
		} else if (isDELETE) {
			try{
				OperationContext operationContext = cmisSession.getDefaultContext();
				operationContext.setIncludeRelationships(IncludeRelationships.SOURCE);
				CmisObject cmisObject = cmisSession.getObject(objectId);
				if (cmisObject.getRelationships() != null
						&& !cmisObject.getRelationships().isEmpty()) {
					for (Relationship relationship : cmisObject.getRelationships()) {
						if (!relationship.getType().getId().equals("R:cm:original"))
							relationship.getTarget().delete(true);
					}
				}
				if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)){
					cmisObject.delete(true);
				}else if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER)) {
					((Folder)cmisObject).deleteTree(true, UnfileObject.DELETE, false);
				}
			}catch (CmisObjectNotFoundException e) {
				LOGGER.error("cancellaAllegato", e);
				throw new ClientMessageException("message.document.not.found");
			}catch (CmisPermissionDeniedException e) {
				LOGGER.error("cancellaAllegato", e);
				throw new ClientMessageException("message.access.denieded");
			}
		}
		return attachments;
	}

	private boolean isArchive(String originalFilename) {
		List<String> extensions = Arrays.asList("zip", "gz", "tar", "7z", "rar", "iso");
		for(String extension : extensions) {
			if (originalFilename.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	private CmisObject upgradeDocument(MultipartFile mFileDocumento, Document doc){
		if (!mFileDocumento.isEmpty()){
			try {
				ContentStream contentStream = new ContentStreamImpl(
						doc.getName(),
						BigInteger.ZERO,
						mFileDocumento.getContentType(),
						mFileDocumento.getInputStream());
				doc.setContentStream(contentStream, true);
				return doc.getObjectOfLatestVersion(false);
			}catch (CmisContentAlreadyExistsException e) {
				LOGGER.error("caricaAllegato", e);
				throw new ClientMessageException("message.file.alredy.exists");
			}catch (CmisPermissionDeniedException e) {
				LOGGER.error("caricaAllegato", e);
				throw new ClientMessageException("message.access.denieded");
			} catch (IOException e) {
				LOGGER.error("caricaAllegato", e);
				throw new ClientMessageException(e.getMessage());
			}
		}
		return null;
	}

	private CmisObject uploadDocument(HttpServletRequest request,
			MultipartFile mFileDocumento, Session cmisSession, Folder source,
			String name, String cmisType) {
		if (!mFileDocumento.isEmpty()){
			try {
				ContentStream contentStream = new ContentStreamImpl(
						mFileDocumento.getOriginalFilename(),
						BigInteger.valueOf(mFileDocumento.getSize()),
						mFileDocumento.getContentType(),
						mFileDocumento.getInputStream());
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.putAll(nodeMetadataService.populateMetadataFromRequest(
						cmisSession, cmisType, request));
				properties.put(PropertyIds.OBJECT_TYPE_ID, cmisType);
				properties.put(PropertyIds.NAME, name);
				Document doc = source.createDocument(properties, contentStream,
						VersioningState.MAJOR);
				doc.updateProperties(properties);
				if (request.getParameter("cmis:sourceId") != null) {
					Map<String, Object> propertiesRel = new HashMap<String, Object>();
					propertiesRel.put(PropertyIds.SOURCE_ID,
							request.getParameter("cmis:sourceId"));
					propertiesRel.put(PropertyIds.TARGET_ID, doc.getId());
					propertiesRel.put(PropertyIds.OBJECT_TYPE_ID,
							request.getParameter("cmis:relObjectTypeId"));
					cmisSession.createRelationship(propertiesRel);
				}
				return doc;
			} catch (CmisContentAlreadyExistsException e) {
				LOGGER.error("caricaAllegato", e);
				throw new ClientMessageException("message.file.alredy.exists");
			} catch (IOException e) {
				LOGGER.error("caricaAllegato", e);
				throw new ClientMessageException(e.getMessage());
			} catch (ParseException e) {
				LOGGER.error("caricaAllegato", e);
				throw new ClientMessageException(e.getMessage());
			}
		}
		return null;
	}
}