package it.cnr.cool.service.util;

import java.math.BigInteger;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import org.apache.chemistry.opencmis.client.api.QueryResult;


public class Log extends AlfrescoDocument {
	private String type;
	private String user;
	private String application;
	private int codice;
	

	public Log(QueryResult doc) {
		super(doc);
		type = doc.getPropertyValueById(CoolPropertyIds.LOGGER_TYPE.value());
		user = doc.getPropertyValueById(CoolPropertyIds.LOGGER_USER.value());
		application = doc.getPropertyValueById(CoolPropertyIds.LOGGER_APPLICATION.value());
		codice = ((BigInteger) doc.getPropertyValueById(CoolPropertyIds.LOGGER_CODE.value())).intValue();
	}

	public String getType() {
		return type;
	}

	public String getUser() {
		return user;
	}

	public String getApplication() {
		return application;
	}

	public int getCodice() {
		return codice;
	}
}
