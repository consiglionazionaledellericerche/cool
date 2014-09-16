package it.cnr.cool.security.service;


import it.cnr.cool.exception.CoolUserFactoryException;

import java.io.InputStream;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.springframework.extensions.webscripts.connector.User;

public interface UserService {
	User loadUser(String userId, BindingSession cmisSession) throws CoolUserFactoryException;
	User loadUserForConfirm(String userId) throws CoolUserFactoryException;
	User createUser(User user) throws CoolUserFactoryException;
	User updateUser(User user) throws CoolUserFactoryException;
	User findUserByEmail(String email, BindingSession cmisSession) throws CoolUserFactoryException;
	User findUserByCodiceFiscale(String codicefiscale, BindingSession cmisSession) throws CoolUserFactoryException;	
	InputStream findUser(String term, BindingSession cmisSession) throws CoolUserFactoryException;
	User changeUserPassword(final User user, String newPassword) throws CoolUserFactoryException;
	void disableAccount(String userName) throws CoolUserFactoryException;
}
