package org.springframework.extensions.webscripts.connector;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Mock User class, to be overridden at runtime by spring-webscripts
 * org.springframework.extensions.webscripts.connector.User
 *
 * @author Francesco Uliana
 *
 */
public class User {

	private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

	private final String userName;

	public User(String userName, Map<String, Boolean> hashMap) {
		LOGGER.error("MOCK CLASS - DO NOT USE IN PRODUCTION");
		this.userName = userName;
	}

	public String getId() {
		LOGGER.error("MOCK CLASS - DO NOT USE IN PRODUCTION");
		return userName;
	}

	public boolean isGuest() {
		LOGGER.error("MOCK CLASS - DO NOT USE IN PRODUCTION");
		return userName == null || userName.length() == 0;
	}

	public String getEmail() {
		LOGGER.error("MOCK CLASS - DO NOT USE IN PRODUCTION");
		return null;
	}

	public String getFullName() {
		LOGGER.error("MOCK CLASS - DO NOT USE IN PRODUCTION");
		return null;
	}

	public Map <String, Boolean> getCapabilities()
    {
		LOGGER.error("MOCK CLASS - DO NOT USE IN PRODUCTION");	
		return null;
    }

}
