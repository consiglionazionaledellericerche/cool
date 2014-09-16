package it.cnr.cool.mail;

import java.util.ArrayList;

public class DatiPEC {
	// indirizzo email per il protocollo 
	private String emailProtocollo;
	private String emailProtocollo2;
	private String emailProtocollo3;
	
	// denuminazione ufficio del servizio
	private String denominazioneServizio;

	// indirizzi email informativi
	private String emailServizio;
	private String emailServizio2;
	private String emailServizio3;

	// indirizzi email istituti
	private String emailIstituto;
	private String emailIstituto2;
	private String emailIstituto3;

	// cds mittente
	private String cds;
	private String dsCds;
	private String siglaCds;
	
	// uo mittente
	private String uo;
	private String dsUo;
	private String siglaUo;

	// cds destinatario
	private String cdsDest;
	private String dsCdsDest;
	private String siglaCdsDest;
	
	// uo destinatario
	private String uoDest;
	private String dsUoDest;
	private String siglaUoDest;

	private String oggetto;
	
	private String numeroRegistrazione;

	public java.util.List<String> emailListProtocollo() {
		java.util.List<String> indirizzi = new ArrayList<String>();
		if (emailProtocollo!=null)
			indirizzi.add(emailProtocollo);
		if (emailProtocollo2!=null)
			indirizzi.add(emailProtocollo2);
		if (emailProtocollo3!=null)
			indirizzi.add(emailProtocollo3);
		return indirizzi;
	}
	public java.util.List<String> emailListServizio() {
		java.util.List<String> indirizzi = new ArrayList<String>();
		if (emailServizio!=null)
			indirizzi.add(emailServizio);
		if (emailServizio2!=null)
			indirizzi.add(emailServizio2);
		if (emailServizio3!=null)
			indirizzi.add(emailServizio3);
		return indirizzi;
	}
	public java.util.List<String> emailListIstituti() {
		java.util.List<String> indirizzi = new ArrayList<String>();
		if (emailIstituto!=null)
			indirizzi.add(emailIstituto);
		if (emailIstituto2!=null)
			indirizzi.add(emailIstituto2);
		if (emailIstituto3!=null)
			indirizzi.add(emailIstituto3);
		return indirizzi;
	}
	public java.util.List<String> emailListTotale() {
		java.util.List<String> indirizzi = new ArrayList<String>();
		indirizzi.addAll(emailListProtocollo());
		indirizzi.addAll(emailListServizio());
		indirizzi.addAll(emailListIstituti());
		return indirizzi;
	}
	public String getEmailProtocollo() {
		return emailProtocollo;
	}
	public void setEmailProtocollo(String emailProtocollo) {
		this.emailProtocollo = emailProtocollo;
	}
	public void setEmailProtocollo2(String email_protocollo2) {
		this.emailProtocollo2 = email_protocollo2;
	}
	public String getEmailProtocollo2() {
		return emailProtocollo2;
	}
	public void setEmailProtocollo3(String email_protocollo3) {
		this.emailProtocollo3 = email_protocollo3;
	}
	public String getEmailProtocollo3() {
		return emailProtocollo3;
	}
	public String getEmailServizio() {
		return emailServizio;
	}
	public void setEmailServizio(String emailServizio) {
		this.emailServizio = emailServizio;
	}
	public String getEmailServizio2() {
		return emailServizio2;
	}
	public void setEmailServizio2(String emailServizio2) {
		this.emailServizio2 = emailServizio2;
	}
	public String getEmailServizio3() {
		return emailServizio3;
	}
	public void setEmailServizio3(String emailServizio3) {
		this.emailServizio3 = emailServizio3;
	}
	public void setUo(String uo) {
		this.uo = uo;
	}
	public String getUo() {
		return uo;
	}
	public void setDsUo(String dsUo) {
		this.dsUo = dsUo;
	}
	public String getDsUo() {
		return dsUo;
	}
	public void setOggetto(String oggetto) {
		this.oggetto = oggetto;
	}
	public String getOggetto() {
		return oggetto;
	}
	public void setDenominazioneServizio(String denominazioneServizio) {
		this.denominazioneServizio = denominazioneServizio;
	}
	public String getDenominazioneServizio() {
		return denominazioneServizio;
	}
	public String getEmailIstituto() {
		return emailIstituto;
	}
	public void setEmailIstituto(String emailIstituto) {
		this.emailIstituto = emailIstituto;
	}
	public String getEmailIstituto2() {
		return emailIstituto2;
	}
	public void setEmailIstituto2(String emailIstituto2) {
		this.emailIstituto2 = emailIstituto2;
	}
	public String getEmailIstituto3() {
		return emailIstituto3;
	}
	public void setEmailIstituto3(String emailIstituto3) {
		this.emailIstituto3 = emailIstituto3;
	}
	public void setCds(String cds) {
		this.cds = cds;
	}
	public String getCds() {
		return cds;
	}
	public void setDsCds(String dsCds) {
		this.dsCds = dsCds;
	}
	public String getDsCds() {
		return dsCds;
	}
	public void setSiglaCds(String siglaCds) {
		this.siglaCds = siglaCds;
	}
	public String getSiglaCds() {
		return siglaCds;
	}
	public void setSiglaUo(String siglaUo) {
		this.siglaUo = siglaUo;
	}
	public String getSiglaUo() {
		return siglaUo;
	}
	public void setCdsDest(String cdsDest) {
		this.cdsDest = cdsDest;
	}
	public String getCdsDest() {
		return cdsDest;
	}
	public void setDsCdsDest(String dsCdsDest) {
		this.dsCdsDest = dsCdsDest;
	}
	public String getDsCdsDest() {
		return dsCdsDest;
	}
	public void setSiglaCdsDest(String siglaCdsDest) {
		this.siglaCdsDest = siglaCdsDest;
	}
	public String getSiglaCdsDest() {
		return siglaCdsDest;
	}
	public void setUoDest(String uoDest) {
		this.uoDest = uoDest;
	}
	public String getUoDest() {
		return uoDest;
	}
	public void setDsUoDest(String dsUoDest) {
		this.dsUoDest = dsUoDest;
	}
	public String getDsUoDest() {
		return dsUoDest;
	}
	public void setSiglaUoDest(String siglaUoDest) {
		this.siglaUoDest = siglaUoDest;
	}
	public String getSiglaUoDest() {
		return siglaUoDest;
	}
	public void setNumeroRegistrazione(String numeroRegistrazione) {
		this.numeroRegistrazione = numeroRegistrazione;
	}
	public String getNumeroRegistrazione() {
		return numeroRegistrazione;
	}
}
