package it.cnr.cool.security.service;

import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.impl.alfresco.CMISAuthority;
import it.cnr.cool.security.service.impl.alfresco.CMISGroup;

import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;

public interface GroupService {
	CMISGroup createGroup(String group_name, String display_name, BindingSession cmisSession) throws CoolUserFactoryException;
	CMISGroup loadGroup(String group_name, BindingSession cmisSession) throws CoolUserFactoryException;
	List<CMISAuthority> children(String group_name, BindingSession cmisSession) throws CoolUserFactoryException;
}
