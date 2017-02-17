package it.cnr.cool.security.service;


import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.listener.LogoutListener;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;

public interface UserService {
	CMISUser loadUser(String userId, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISUser loadUserForConfirm(String userId) throws CoolUserFactoryException;
	CMISUser createUser(CMISUser user) throws CoolUserFactoryException;
	CMISUser updateUser(CMISUser user) throws CoolUserFactoryException;
	CMISUser findUserByEmail(String email, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISUser findUserByCodiceFiscale(String codicefiscale, BindingSession cmisSession) throws CoolUserFactoryException;	
	InputStream findUser(String term, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISUser changeUserPassword(final CMISUser user, String newPassword) throws CoolUserFactoryException;
	void disableAccount(String userName) throws CoolUserFactoryException;
	void enableAccount(String userName) throws CoolUserFactoryException;
	List<String> findMembers(String groupName, BindingSession cmisSession) throws CoolUserFactoryException;
	boolean addLogoutListener(LogoutListener logoutListener);
	void logout(String userId);
	URI getRedirect(CMISUser cmisUser, URI uri);
}