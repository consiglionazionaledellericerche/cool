package it.cnr.cool.security.service.impl.alfresco;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class CMISUser implements java.security.Principal {
	
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
	
	public static final String CAPABILITY_ADMIN = "isAdmin";
	public static final String CAPABILITY_GUEST = "isGuest";
	public static final String CAPABILITY_MUTABLE = "isMutable";

	protected String fullName = null;

	public static String PROP_MIDDLE_NAME = "middleName";

	protected final Map<String, Serializable> map = new HashMap<String, Serializable>(
			32);

	/** User object key in the session */
	public static String SESSION_ATTRIBUTE_KEY_USER_OBJECT = "_alf_USER_OBJECT";

	protected final Map<String, Boolean> capabilities = new HashMap<String, Boolean>();

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
	}

	public CMISUser(String userName) {
		this.userName = userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setCapabilities(Map<String, Boolean> capabilities) {
		capabilities.putAll(capabilities);
	}
	
	public Map<String, Boolean> getImmutability() {
		return immutability;
	}

	public void setImmutability(Map<String, Boolean> immutability) {
		this.immutability = immutability;
	}
		
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

	@Override
	public String getName() {
		return userName;
	}

	/**
	 * Returns <code>true</code> if this user is a guest user
	 * 
	 * @return <code>true</code> if this user is a guest user
	 */
	public boolean isGuest() {
		Boolean value = this.capabilities.get(CAPABILITY_GUEST);
		return value == null ? false : value;
	}

	/**
	 * Provides the full name for the user. This makes a best attempt at
	 * building the full name based on what it knows about the user.
	 * 
	 * If a first name is not known, the returned name will be the user id of
	 * the user.
	 * 
	 * If a first name is known, then the first name will be returned. If a
	 * first and middle name are known, then the first and middle name will be
	 * returned.
	 * 
	 * Valid full names are therefore:
	 * 
	 * jsmith Joe Joe D Joe Smith Joe D Smith
	 * 
	 * @return A valid full name
	 */
	public String getFullName() {
		if (this.fullName == null) {
			boolean hasFirstName = (getFirstName() != null && getFirstName()
					.length() != 0);
			boolean hasMiddleName = (getMiddleName() != null && getMiddleName()
					.length() != 0);
			boolean hasLastName = (getLastName() != null && getLastName()
					.length() != 0);

			// if they don't have a first name, then use their user id
			this.fullName = getId();
			if (hasFirstName) {
				this.fullName = getFirstName();

				if (hasMiddleName) {
					this.fullName += " " + getMiddleName();
				}

				if (hasLastName) {
					this.fullName += " " + getLastName();
				}
			}
		}

		return this.fullName;
	}

	/**
	 * Gets the middle name.
	 * 
	 * @return the middle name
	 */
	public String getMiddleName() {
		return getStringProperty(PROP_MIDDLE_NAME);
	}

	/**
	 * Gets the string property.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the string property
	 */
	public String getStringProperty(String key) {
		return (String) map.get(key);
	}

	/**
	 * Checks if is admin.
	 * 
	 * @return the isAdmin
	 */
	public boolean isAdmin() {
		Boolean value = this.capabilities.get(CAPABILITY_ADMIN);
		return value == null ? false : value;
	}
}
