package it.cnr.cool.service.util;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import org.apache.chemistry.opencmis.client.api.QueryResult;


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
