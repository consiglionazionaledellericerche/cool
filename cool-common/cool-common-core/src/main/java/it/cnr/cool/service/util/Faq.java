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
import java.util.GregorianCalendar;


public class Faq extends AlfrescoDocument {
	private String type;
	private String dataPubblicazione;
	private BigInteger number;
	private String faqBando;
	private String question;
	private String answer;
	private Boolean show;

        
	public Faq(QueryResult doc) {
		super(doc);
		type = doc.getPropertyValueById(CoolPropertyIds.FAQ_TYPE.value());
		dataPubblicazione = ALFRESCO_DATE_FORMAT.format(( (GregorianCalendar) doc.getPropertyValueById(CoolPropertyIds.FAQ_DATA.value())).getTime());
		number = doc.getPropertyValueById(CoolPropertyIds.FAQ_NUMBER.value());
		answer = doc.getPropertyValueById(CoolPropertyIds.FAQ_ANSWER.value());
		faqBando = doc.getPropertyValueById(CoolPropertyIds.FAQ_TYPE.value());
		question = doc.getPropertyValueById(CoolPropertyIds.FAQ_QUESTION.value());
		show = doc.getPropertyValueById(CoolPropertyIds.FAQ_SHOW.value());
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

	public String getFaqBando() {
		return faqBando;
	}


	public String getQuestion() {
		return question;
	}


	public String getAnswer() {
		return answer;
	}


	public Boolean getShow() {
		return show;
	}
}
