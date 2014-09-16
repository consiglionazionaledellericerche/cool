package it.cnr.cool.security.service.impl.alfresco;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.connector.User;

import com.google.gson.annotations.SerializedName;

public class CMISUser extends User{
	
	public static final long serialVersionUID = 1L;
	private Map<String, Boolean> immutability;
	private String userName;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private String telephone;
	private String mobile;
	private Boolean enabled;
	
	/** Attributi CNR */
	@SerializedName("cnrperson:matricola")
	private Integer matricola;
	@SerializedName("cnrperson:emailesterno")
	private String emailesterno;
	@SerializedName("cnrperson:emailcertificatoperpuk")	
	private String emailcertificatoperpuk;
	@SerializedName("cnrperson:codicefiscale")
	private String codicefiscale;
	@SerializedName("cnrperson:dataDiNascita")	
	private Date dataDiNascita;
	@SerializedName("cnrperson:straniero")
	private Boolean straniero;
	@SerializedName("cnrperson:sesso")	
	private String sesso;
	@SerializedName("cnrperson:statoestero")
	private String statoestero;
	@SerializedName("cnrperson:pin")
	private String pin;

	private Boolean ldapuser;
		
	private Boolean disableAccount;
	private List<CMISGroup> groups;
	
	public CMISUser() {
		super(null, new HashMap<String, Boolean>());
	}

	public CMISUser(String userName) {
		super(userName, new HashMap<String, Boolean>());
		this.userName = userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setCapabilities(Map<String, Boolean> capabilities) {
		getCapabilities().putAll(capabilities);
	}
	
	public Map<String, Boolean> getImmutability() {
		return immutability;
	}

	public void setImmutability(Map<String, Boolean> immutability) {
		this.immutability = immutability;
	}
		
	@Override
	public String getId() {
		return userName;
	}
	public String getFirstName() {
		return firstName;
	}

	public Boolean getDisableAccount() {
		return disableAccount;
	}

	public void setDisableAccount(Boolean disableAccount) {
		this.disableAccount = disableAccount;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		if (email.equalsIgnoreCase("nomail"))
			if (emailesterno != null)
				return emailesterno;
			else if (emailcertificatoperpuk != null)
				return emailcertificatoperpuk;
			else
				return "";
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserName() {
		return userName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<CMISGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<CMISGroup> groups) {
		this.groups = groups;
	}

	public String getCodicefiscale() {
		return codicefiscale;
	}

	public void setCodicefiscale(String codicefiscale) {
		this.codicefiscale = codicefiscale;
	}

	public Date getDataDiNascita() {
		return dataDiNascita;
	}

	public void setDataDiNascita(Date dataDiNascita) {
		this.dataDiNascita = dataDiNascita;
	}

	public Boolean getStraniero() {
		return straniero;
	}

	public void setStraniero(Boolean straniero) {
		this.straniero = straniero;
	}

	public String getSesso() {
		return sesso;
	}

	public void setSesso(String sesso) {
		this.sesso = sesso;
	}

	public String getStatoestero() {
		return statoestero;
	}

	public void setStatoestero(String statoestero) {
		this.statoestero = statoestero;
	}

	public Boolean getLdapuser() {
		return ldapuser;
	}

	public void setLdapuser(Boolean ldapuser) {
		this.ldapuser = ldapuser;
	}
	
	public Integer getMatricola() {
		return matricola;
	}

	public void setMatricola(Integer matricola) {
		this.matricola = matricola;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getEmailesterno() {
		return emailesterno;
	}

	public void setEmailesterno(String emailesterno) {
		this.emailesterno = emailesterno;
	}

	public String getEmailcertificatoperpuk() {
		return emailcertificatoperpuk;
	}

	public void setEmailcertificatoperpuk(String emailcertificatoperpuk) {
		this.emailcertificatoperpuk = emailcertificatoperpuk;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
}
