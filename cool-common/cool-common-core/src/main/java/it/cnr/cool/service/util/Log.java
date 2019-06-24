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

package it.cnr.cool.service.util;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import org.apache.chemistry.opencmis.client.api.QueryResult;

import java.math.BigInteger;


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
