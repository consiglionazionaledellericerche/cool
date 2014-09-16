package it.cnr.cool.cmis.service;

import it.cnr.cool.cmis.model.DocumentType;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;

public class CMISBaseCriteria {

	public CMISBaseCriteria() {
		super();
	}

	public Criteria getCriteria() {
		Criteria criteria = CriteriaFactory.createCriteria(DocumentType.CMIS_DOCUMENT.queryName());
		return criteria;
	}
}
