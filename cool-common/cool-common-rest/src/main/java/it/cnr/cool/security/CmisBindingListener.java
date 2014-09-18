package it.cnr.cool.security;

import it.cnr.cool.cmis.service.CMISService;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.context.ApplicationContext;

public class CmisBindingListener implements HttpSessionListener {

	private static final String ATTRIBUTE_NAME = "org.springframework.web.context.WebApplicationContext.ROOT";

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		createCMISSession(se);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		createCMISSession(se);
	}

	private void createCMISSession(HttpSessionEvent se){

		ApplicationContext xmlWebApplicationContext = (org.springframework.web.context.support.XmlWebApplicationContext) se
				.getSession()
				.getServletContext()
				.getAttribute(
						ATTRIBUTE_NAME);
		
		CMISService cmisService = (CMISService) xmlWebApplicationContext.getBean("cmisService");

			if (se.getSession().getAttribute(CMISService.DEFAULT_SERVER) == null)
				se.getSession().setAttribute(CMISService.DEFAULT_SERVER, 
					cmisService.createSession());
			if (se.getSession().getAttribute(CMISService.BINDING_SESSION) == null)
				se.getSession().setAttribute(CMISService.BINDING_SESSION, 
					cmisService.createBindingSession());
	}
}
