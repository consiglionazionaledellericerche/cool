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
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.QueryResult;

import java.math.BigInteger;
import java.util.GregorianCalendar;


public class Notice extends AlfrescoDocument {
	private String type;
	private String dataPubblicazione;
	private BigInteger number;
	private String title;
	private String noticeStyle;
	private String text;
	private String dataScadenza;
	private String visibility;
	
	public Notice(CmisObject doc, String visibility) {
		super(doc);
		type = doc.getPropertyValue(CoolPropertyIds.NOTICE_TYPE.value());
		dataPubblicazione = ALFRESCO_DATE_FORMAT.format(( (GregorianCalendar) doc.getPropertyValue(CoolPropertyIds.NOTICE_DATA.value())).getTime());
		number = doc.getPropertyValue(CoolPropertyIds.NOTICE_NUMBER.value());
		title = doc.getPropertyValue(CoolPropertyIds.NOTICE_TITLE.value());
		noticeStyle = doc.getPropertyValue(CoolPropertyIds.NOTICE_STYLE.value());
		text = doc.getPropertyValue(CoolPropertyIds.NOTICE_TEXT.value());
		dataScadenza = ALFRESCO_DATE_FORMAT.format(( (GregorianCalendar)doc.getPropertyValue(CoolPropertyIds.NOTICE_SCADENZA.value())).getTime());
		this.visibility = visibility;		
	}
	
	public Notice(QueryResult doc, String visibility) {
		super(doc);
		type = doc.getPropertyValueById(CoolPropertyIds.NOTICE_TYPE.value());
		dataPubblicazione = ALFRESCO_DATE_FORMAT.format(( (GregorianCalendar) doc.getPropertyValueById(CoolPropertyIds.NOTICE_DATA.value())).getTime());
		number = doc.getPropertyValueById(CoolPropertyIds.NOTICE_NUMBER.value());
		title = doc.getPropertyValueById(CoolPropertyIds.NOTICE_TITLE.value());
		noticeStyle = doc.getPropertyValueById(CoolPropertyIds.NOTICE_STYLE.value());
		text = doc.getPropertyValueById(CoolPropertyIds.NOTICE_TEXT.value());
		dataScadenza = ALFRESCO_DATE_FORMAT.format(( (GregorianCalendar)doc.getPropertyValueById(CoolPropertyIds.NOTICE_SCADENZA.value())).getTime());
		this.visibility = visibility;		
	}


	public String getType() {
		return type;
	}


	public String getDataPubblicazione() {
		return dataPubblicazione;
	}


	public BigInteger getNumber() {
		return number;
	}


	public String getTitle() {
		return title;
	}


	public String getNoticeStyle() {
		return noticeStyle;
	}


	public String getText() {
		return text;
	}


	public String getDataScadenza() {
		return dataScadenza;
	}


	public String getVisibility() {
		return visibility;
	}

}
