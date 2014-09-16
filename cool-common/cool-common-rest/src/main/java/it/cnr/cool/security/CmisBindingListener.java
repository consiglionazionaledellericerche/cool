package it.cnr.cool.security;

import it.cnr.cool.cmis.service.CMISService;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;

public class CmisBindingListener implements HttpSessionListener {

	public static final String SERVLET_NAME = "Spring Surf Dispatcher Servlet"; //TODO: scegliere un nome migliore

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		createCMISSession(se);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		createCMISSession(se);
	}

	private void createCMISSession(HttpSessionEvent se){

		// TODO: non verra' mai trovato.....

		ApplicationContext applicationContext = (ApplicationContext) 
				se.getSession().getServletContext().
				getAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX+ SERVLET_NAME);
			if (se.getSession().getAttribute(CMISService.DEFAULT_SERVER) == null)
				se.getSession().setAttribute(CMISService.DEFAULT_SERVER, 
						applicationContext.getBean("cmisService", CMISService.class).createSession());
			if (se.getSession().getAttribute(CMISService.BINDING_SESSION) == null)
				se.getSession().setAttribute(CMISService.BINDING_SESSION, 
						applicationContext.getBean("cmisService", CMISService.class).createBindingSession());			
	}
}
