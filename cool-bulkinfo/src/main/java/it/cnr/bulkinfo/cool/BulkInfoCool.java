package it.cnr.bulkinfo.cool;

import it.cnr.bulkinfo.BulkInfo;
import it.cnr.bulkinfo.BulkInfoImpl.FieldProperty;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;


public interface BulkInfoCool extends BulkInfo {
	void addFieldProperty(PropertyDefinition<?> propertyDefinition);

	PropertyDefinition<?> getPropertyDefinition(Session session,
			CmisObject cmisObject, FieldProperty fieldProperty);
}
