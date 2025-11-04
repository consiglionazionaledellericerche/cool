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

package it.cnr.cool.security.service;


import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.listener.LoginListener;
import it.cnr.cool.listener.LogoutListener;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface UserService {
	CMISUser loadUser(String userId, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISUser loadUserForConfirm(String userId) throws CoolUserFactoryException;
	CMISUser createUser(CMISUser user) throws CoolUserFactoryException;
	CMISUser updateUser(CMISUser user) throws CoolUserFactoryException;
	void deleteUser(CMISUser user) throws CoolUserFactoryException;
	CMISUser findUserByEmail(String email, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISUser findUserByCodiceFiscale(String codicefiscale, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISUser findUserByCodiceFiscale(String codicefiscale, BindingSession cmisSession, List<String> userNames, String email) throws CoolUserFactoryException;
	InputStream findUser(String term, BindingSession cmisSession) throws CoolUserFactoryException;
	boolean isUserExists(String userId);
	CMISUser changeUserPassword(final CMISUser user, String newPassword) throws CoolUserFactoryException;
	void disableAccount(String userName) throws CoolUserFactoryException;
	void enableAccount(String userName) throws CoolUserFactoryException;
	List<String> findMembers(String groupName, BindingSession cmisSession) throws CoolUserFactoryException;
	boolean addLogoutListener(LogoutListener logoutListener);
	boolean addLoginListener(LoginListener logintListener);
	void logout(String userId);
	void successfulLogin(String userId);
	URI getRedirect(CMISUser cmisUser, URI uri);
}