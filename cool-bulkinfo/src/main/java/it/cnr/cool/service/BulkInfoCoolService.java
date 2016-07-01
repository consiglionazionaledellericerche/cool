package it.cnr.cool.service;

import com.google.gson.JsonObject;
import it.cnr.bulkinfo.BulkInfoImpl.FieldProperty;
import it.cnr.bulkinfo.cool.BulkInfoCool;
import it.cnr.bulkinfo.cool.BulkInfoCoolImpl;
import it.cnr.bulkinfo.exception.BulkInfoException;
import it.cnr.bulkinfo.exception.BulkInfoNotFoundException;
import it.cnr.bulkinfo.exception.BulkinfoKindException;
import it.cnr.bulkinfo.exception.BulkinfoNameException;
import it.cnr.cool.BulkInfoRepository;
import it.cnr.cool.cmis.service.ACLService;
import it.cnr.cool.cmis.service.VersionService;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;

public class BulkInfoCoolService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkInfoCoolService.class);

	@Autowired
	private ApplicationContext context;
	
    @Autowired
    private VersionService versionService;

	@Autowired
	private ACLService aclService;

    @Autowired
    private BulkInfoRepository bulkInfoRepository;

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
	 */
	public Map<String, Object> getView(Session cmisSession, BindingSession bindingSession, String type,
                                       String kind, String name, String objectId) throws BulkInfoException, BulkinfoKindException, BulkinfoNameException {

		Map<String, Object> model = new HashMap<String, Object>();

		if (name == null || name.equals("")) {
			throw new BulkinfoNameException("Variabile nome non impostata");
		}

		BulkInfoCool bi = find(type);

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

//			model.put("jsonUtils", new Util());
			model.put("cmisSession", cmisSession);
			model.put("url.context", "");

			if (objectId != null && !objectId.equals("")) {
				model.put("cmisObject", cmisSession.getObject(objectId));
                if (bi.getFieldProperties().containsKey("inheritedPermission")) {
                    JsonObject permission = aclService.getPermission(bindingSession, objectId);
                    model.put("inheritedPermission", permission.get("isInherited").getAsBoolean());
                }
			}
		}
		return model;
	}

	/**
	 * Questo metodo publico e' solo un livello di indirezione per costruire la
	 * cache guava. La magia succede nel build()
	 *
	 * @param bulkInfoName
	 * @return
	 */
    //TODO: cacharlo a livello HTTP!!!
	public BulkInfoCool find(String bulkInfoName) {

        BulkInfoCool bi = null;

        LOGGER.debug("building bulkinfo " + bulkInfoName);

        try {
            bi = build(bulkInfoName);
        } catch (BulkInfoNotFoundException e) {
            LOGGER.error("Error finding Bulkinfo " + bulkInfoName, e);
        }

        return bi;



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
	 * @return BulkInfoNew or null
	 * @throws BulkInfoNotFoundException
	 */
	private BulkInfoCool build(String bulkInfoName) throws BulkInfoNotFoundException {
		LOGGER.debug("Building BulkInfo " + bulkInfoName + " for cache");

		List<PropertyDefinition<?>> properties = new ArrayList<PropertyDefinition<?>>();
		BulkInfoCool bulkInfo = getOrCreate(bulkInfoName, properties);

		String name = "BulkInfo" + bulkInfo.getId();
		if (context.containsBean(name)) {
			BulkInfoInjection bulkInfoInjection = context.getBean(name, BulkInfoInjection.class);
			if (bulkInfoInjection != null)
				bulkInfoInjection.complete(bulkInfo);
		}

		if (bulkInfo != null) {
			ObjectType bulkObjectType = getObjectType(bulkInfoName, bulkInfo);
			if (bulkObjectType != null && bulkObjectType.getBaseTypeId().equals(BaseTypeId.CMIS_SECONDARY) && bulkInfo.getForm(bulkObjectType.getId()).isEmpty()) {
				bulkInfo.addForm(bulkObjectType.getId());
				bulkInfo.addPrintForm(bulkObjectType.getId());				
			}			
			injectParentAndAspects(bulkInfo);
			insertProperties(bulkInfo, properties, bulkObjectType);
		}

		return bulkInfo;
	}

	private ObjectType getObjectType(String bulkInfoName, BulkInfoCool bulkInfo) {
		ObjectType bulkObjectType = null;
		try {
			if (bulkInfo.getCmisTypeName() != null) {
				bulkObjectType = getObjectType(bulkInfo.getCmisTypeName());
			}

		} catch (CmisObjectNotFoundException ex) {
			LOGGER.error("Object type " + bulkInfoName + " not found. Il l'xml "
					+ "del BulkInfo "+ bulkInfoName +" e' sbagliato e va corretto");
		}
		return bulkObjectType;
	}

	private BulkInfoCool getOrCreate(String bulkInfoName,
			List<PropertyDefinition<?>> properties) throws BulkInfoNotFoundException {
		LOGGER.debug("Searching bulkInfo in Classpath : " + bulkInfoName);
		BulkInfoCool bulkInfo = getBulkInfoFromResources(bulkInfoName);

		if (bulkInfo == null) {
			LOGGER.debug("Bulkinfo not found in classpath. Constructing from Cmis Type : "
					+ bulkInfoName);
			bulkInfo = createBulkInfoFromCmisType(bulkInfoName, properties);
		} else {
			LOGGER.debug("Bulkinfo " + bulkInfoName + " successfully created");
		}

		if(bulkInfo == null) {
			throw new BulkInfoNotFoundException(bulkInfoName);
		}

		return bulkInfo;
	}

	/**
	 * Cerca di recuperare un BulkInfo dal classpath restituisce null se non lo
	 * trova
	 *
	 * @param bulkInfoName
	 * @return BulkInfoNew or null
	 */
	private BulkInfoCool getBulkInfoFromResources(String bulkInfoName) {

        LOGGER.debug("loading bulkinfo: " + bulkInfoName);

        Document doc = bulkInfoRepository.getXmlDocument(bulkInfoName);

        if (doc != null) {
            return new BulkInfoCoolImpl(bulkInfoName, doc, versionService.isProduction());
        } else {
            return null;
        }

	}


	/**
	 * SIDE EFFECTS su properties.
	 * Prende un tipo CMIS per nome, e se esiste,
	 * anche un bulkinfo con quel nome (da cmis) e poi ricorsivamente trova
	 * tutti i parent di quel tipo SE uno di questi parent ha associato un
	 * bulkinfo, lo inserisce e salta fuori dal loop (questo perchè prima i
	 * BulkInfo venivano costruiti incrementalmente, e se veniva trovato in
	 * memoria significava che era sicuramente già impostato, per c'e' questo il
	 * break) ALTRIMENTI inserisce le proprieta' di quel tipo (withoutInherited)
	 *
	 * @param bulkTypeName
	 * @param properties
	 * @return
	 */
	private BulkInfoCool createBulkInfoFromCmisType(String bulkTypeName,
			List<PropertyDefinition<?>> properties) {
		BulkInfoCool bulkInfo = null;

		try {
			ObjectType bulkObjectType = getObjectType(bulkTypeName);

			LOGGER.debug("successfully retrieved type definition for type: "
					+ bulkObjectType.getId());

			if (bulkObjectType != null) {
				LOGGER.debug("Searching bulkInfo in Cmis : " + bulkTypeName);

				String xml = "<bulkInfo></bulkInfo>";
				Document doc = DocumentHelper.parseText(xml);
				bulkInfo = new BulkInfoCoolImpl(bulkTypeName.replaceAll(":", "_"), doc);

				bulkInfo.setCmisTypeName(bulkObjectType.getId());
				bulkInfo.setCmisQueryName(bulkObjectType.getQueryName());

				// parent
				bulkInfo.setCmisExtendsName(bulkObjectType.getParentTypeId());
				// aspects
				if (bulkObjectType.getExtensions() != null){
					for (CmisExtensionElement cmisExtensionElement : bulkObjectType.getExtensions()) {
						if (cmisExtensionElement.getName().equals("mandatoryAspects")){
							for (CmisExtensionElement child : cmisExtensionElement.getChildren()) {
								bulkInfo.addCmisExtensionElement(child.getValue(), true);
							}
						}
					}
				}
				// properties non ereditate
				for (PropertyDefinition<?> property : bulkObjectType
						.getPropertyDefinitions().values()) {
					boolean inherited = property.isInherited();
					if (!inherited) {
						properties.add(property);
					}
					LOGGER.debug("added property (inherited=" + inherited
							+ ") " + property.getId());
				}

			}
		} catch (CmisObjectNotFoundException exp) { // log error, return null
			LOGGER.error("CmisObjectNotFoundException with bulkInfo :" + bulkTypeName,
					exp);
		} catch (DocumentException e) {
			// Non succereda' mai perchè parsiamo una stringa fissa
			// E' qui per bellezza, e perche' altrimenti non compila
			LOGGER.error("errore", e);
		}
		return bulkInfo;
	}

	// qui succede la ricorsione su find()
	private void injectParentAndAspects(BulkInfoCool bi) throws BulkInfoNotFoundException {
		if (bi.getCmisExtendsName() != null) {
			BulkInfoCool parent = find(bi.getCmisExtendsName());
			if (parent != null) {
				bi.completeWithParent(parent, false);
			}
		}
		if (bi.getCmisImplementsName() != null
				&& !bi.getCmisImplementsName().isEmpty()) {
			for (String name : bi.getCmisImplementsName().keySet()) {
				BulkInfoCool aspect = find(name);
				if (aspect != null) {
					bi.completeWithParent(aspect, true);
				}
			}
		}
	}

	/**
	 * SIDE EFFECTS Inserisce le proprieta (withChoice) passate nel bulkinfo
	 *
	 * @param bulkInfo
	 * @param properties
	 * @param bulkObjectType
	 */
	private void insertProperties(BulkInfoCool bulkInfo,
			List<PropertyDefinition<?>> properties, ObjectType bulkObjectType) {

		for (String cmisImplementsName : bulkInfo.getCmisImplementsName().keySet()) {
			try {
				properties.addAll(
						getPropertyWithChoice(getObjectType(cmisImplementsName), bulkInfo));
			} catch (CmisObjectNotFoundException _ex) {
				LOGGER.warn("Type :" + cmisImplementsName + " not found!");
			}
		}

		if (bulkObjectType != null) {
			properties.addAll(getPropertyWithChoice(bulkObjectType, bulkInfo));
		}

		for (PropertyDefinition<?> propertyDefinition : properties) {
			LOGGER.debug("Add property to BulkInfo :"+ propertyDefinition.getId());
			bulkInfo.addFieldProperty(propertyDefinition);
			if (bulkObjectType != null && bulkObjectType.getBaseTypeId().equals(BaseTypeId.CMIS_SECONDARY)) {
				FieldProperty fieldProperty = bulkInfo.getFieldProperties().get(propertyDefinition.getLocalName());
				if (fieldProperty != null) {
					bulkInfo.getForms().get(bulkObjectType.getId()).addFormFieldProperty(fieldProperty);
					bulkInfo.getPrintForms().get(bulkObjectType.getId()).addPrintFieldProperty(fieldProperty);
				}
			}			
		}
	}

	/**
	 * Dato un Tipo e un BulkInfo restituisce la lista delle proprieta' che
	 * HANNO una lista di valori possibili e che SONO gia' parte del BulkInfo ma
	 * che NON sono stati ancora inseriti ("jsonlist" == null)
	 *
	 * @param objectType
	 * @param bulkInfo
	 * @return
	 */
	private Collection<? extends PropertyDefinition<?>> getPropertyWithChoice(
			ObjectType objectType, BulkInfoCool bulkInfo) {
		List<PropertyDefinition<?>> properties = new ArrayList<PropertyDefinition<?>>();
		for (PropertyDefinition<?> property : objectType.getPropertyDefinitions().values()) {
			if (property.isInherited())
				continue;
			if (property.getChoices() != null && !property.getChoices().isEmpty()) {
				List<FieldProperty> fields = bulkInfo.getFieldPropertyByProperty(property.getId());
				if (fields != null && !fields.isEmpty()) {
					for (FieldProperty fieldProperty : fields) {
						if (fieldProperty.getAttribute("jsonlist") == null) {
							properties.add(property);
						}
					}
				} else {
					properties.add(property);
				}
			}
		}
		return properties;
	}


    private ObjectType getObjectType(String bulkTypeName) {
        LOGGER.debug("retrieving type: " + bulkTypeName);
        return bulkInfoRepository.getObjectType(bulkTypeName);
    }

}
