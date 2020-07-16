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

package it.cnr.cool.security.service.impl.alfresco;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.cnr.cool.util.StringUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CMISUser implements java.security.Principal, Serializable {

	public static final long serialVersionUID = 1L;
	private String userName;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private String telephone;
	private String mobile;
	private Boolean enabled;
	private String homeFolder;

	public static final String CAPABILITY_ADMIN = "isAdmin";
	public static final String CAPABILITY_GUEST = "isGuest";
	public static final String CAPABILITY_MUTABLE = "isMutable";

	protected String fullName = null;

	public static String PROP_MIDDLE_NAME = "middleName";
	
	private Map<String, Boolean> immutability;
	private Map<String, Object> other = new HashMap<String, Object>();
	private Map<String, Boolean> capabilities = new HashMap<String, Boolean>();

	/** Attributi CNR */
	@JsonProperty("cnrperson:matricola")
	private Integer matricola;
	@JsonProperty("cnrperson:emailesterno")
	private String emailesterno;
	@JsonProperty("cnrperson:emailcertificatoperpuk")
	private String emailcertificatoperpuk;
	@JsonProperty("cnrperson:codicefiscale")
	private String codicefiscale;
	@JsonProperty("cnrperson:dataDiNascita")
	private Date dataDiNascita;
	@JsonProperty("cnrperson:straniero")
	private Boolean straniero;
	@JsonProperty("cnrperson:sesso")
	private String sesso;
	@JsonProperty("cnrperson:statoestero")
	private String statoestero;
	@JsonProperty("cnrperson:pin")
	private String pin;
	@JsonProperty("cmis:objectTypeId")
	private String objectTypeId = "cm:person";

	private Boolean ldapuser;

	private Boolean disableAccount;

	private List<CMISGroup> groups;
	private List<String> groupsArray;

	public CMISUser() {
	}

	public CMISUser(String userName) {
		this.userName = userName;
	}
	
	public Map<String, Boolean> getCapabilities() {
		return capabilities;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setCapabilities(Map<String, Boolean> capabilities) {
		this.capabilities.putAll(capabilities);
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

	@JsonIgnore
	public boolean isNoMail() {
		return Optional.ofNullable(email).filter(x -> x.equalsIgnoreCase("nomail")).isPresent();
	}

	public String getEmail() {
		if (isNoMail())
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

	public List<String> getGroupsArray() {
		return groupsArray;
	}
	
	public void setGroups(List<CMISGroup> groups) {
		this.groups = groups;
		this.groupsArray = Optional.ofNullable(groups)
				.map(x -> x.stream()
						.map(group -> group.getGroup_name())
						.collect(Collectors.toList()))
						.orElse(Collections.emptyList());
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
		return Optional.ofNullable(capabilities).map(x -> x.get(CAPABILITY_GUEST)).orElse(true);
	}


	public String getHomeFolder() {
		return homeFolder;
	}

	public void setHomeFolder(String homeFolder) {
		this.homeFolder = homeFolder;
	}

	@JsonIgnore
	public String getFullName() {
		if (this.fullName == null) {
			boolean hasFirstName = getFirstName() != null && getFirstName()
					.length() != 0;
			boolean hasLastName = getLastName() != null && getLastName()
					.length() != 0;

			// if they don't have a first name, then use their user id
			this.fullName = getId();
			if (hasFirstName) {
				this.fullName = getFirstName();

				if (hasLastName) {
					this.fullName += " " + getLastName();
				}
			}
		}
		return this.fullName;
	}


	/**
	 * Checks if is admin.
	 *
	 * @return the isAdmin
	 */
	public boolean isAdmin() {
		return Optional.ofNullable(capabilities).map(x -> x.get(CAPABILITY_ADMIN)).orElse(false);
	}

	@Override
	public String toString() {
		return userName;
	}
	
	@JsonAnyGetter
	public Map<String, Object> getOther() {
		return other;
	}

	@JsonAnySetter
	public void setOther(String name, Object value) {
		/** Attributi CNR */
		if (value != null) {
			if (name.equalsIgnoreCase("matricola"))
				setMatricola((Integer) value);
			else if (name.equalsIgnoreCase("emailesterno"))
				setEmailesterno((String) value);
			else if (name.equalsIgnoreCase("emailcertificatoperpuk"))
				setEmailcertificatoperpuk((String) value);		
			else if (name.equalsIgnoreCase("codicefiscale"))
				setCodicefiscale((String) value);
			else if (name.equalsIgnoreCase("dataDiNascita"))
				try {
					setDataDiNascita(StringUtil.CMIS_DATEFORMAT.parse((String)value));
				} catch (ParseException e) {
					setDataDiNascita(null);
				}
			else if (name.equalsIgnoreCase("straniero"))
				setStraniero((Boolean) value);
			else if (name.equalsIgnoreCase("sesso"))
				setSesso((String) value);
			else if (name.equalsIgnoreCase("statoestero"))
				setStatoestero((String) value);
			else if (name.equalsIgnoreCase("pin"))
				setPin((String) value);			
		}
		if (Arrays.asList("admin", "name", "id", "guest", "locale").stream().noneMatch(x -> x.equals(name)))
			Optional.ofNullable(value).map(x -> other.put(name, x));
	}

	public boolean hasUnknowProperties() {
		return !other.isEmpty();
	}
	
	public void clearForPersist(){
		groups = null;
		groupsArray = null;
		other = null;
		capabilities = null;
		immutability = null;
		homeFolder = null;
		objectTypeId = null;
	}
}