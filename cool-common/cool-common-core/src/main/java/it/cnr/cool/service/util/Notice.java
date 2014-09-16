package it.cnr.cool.service.util;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import org.apache.chemistry.opencmis.client.api.QueryResult;


public class Notice extends AlfrescoDocument {
	private String type;
	private String dataPubblicazione;
	private BigInteger number;
	private String title;
	private String noticeStyle;
	private String text;
	private String dataScadenza;
	private String visibility;
	
	
	public Notice(QueryResult doc, String visibility) {
		super(doc);
		type = doc.getPropertyValueById(CoolPropertyIds.NOTICE_TYPE.value());
		dataPubblicazione = DATEFORMAT.format(( (GregorianCalendar) doc.getPropertyValueById(CoolPropertyIds.NOTICE_DATA.value())).getTime());
		number = doc.getPropertyValueById(CoolPropertyIds.NOTICE_NUMBER.value());
		title = doc.getPropertyValueById(CoolPropertyIds.NOTICE_TITLE.value());
		noticeStyle = doc.getPropertyValueById(CoolPropertyIds.NOTICE_STYLE.value());
		text = doc.getPropertyValueById(CoolPropertyIds.NOTICE_TEXT.value());
		dataScadenza = DATEFORMAT.format(( (GregorianCalendar)doc.getPropertyValueById(CoolPropertyIds.NOTICE_SCADENZA.value())).getTime());
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
