package it.cnr.cool.extensions.surf.mvc;

import it.cnr.cool.extensions.surf.types.CoolPage;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.web.PermissionServiceImpl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.extensions.config.WebFrameworkConfigElement;
import org.springframework.extensions.surf.ModelObjectService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.TemplatesContainer;
import org.springframework.extensions.surf.mvc.PageView;
import org.springframework.extensions.surf.render.RenderService;
import org.springframework.extensions.surf.resource.ResourceService;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.types.Page;
import org.springframework.extensions.webscripts.connector.User;

public class CoolPageView extends PageView {
	protected PermissionServiceImpl permission;
	
	public CoolPageView(WebFrameworkConfigElement webFrameworkConfiguration,
			ModelObjectService modelObjectService,
			ResourceService resourceService, RenderService renderService,
			TemplatesContainer templatesContainer, PermissionServiceImpl permission) {
		super(webFrameworkConfiguration, modelObjectService, resourceService,
				renderService, templatesContainer);
		this.permission = permission;
	}

	@Override
	protected boolean loginRequiredForPage(RequestContext context,
			HttpServletRequest request, Page page) {
		CoolPage jcononPage = (CoolPage)page;
		
        boolean login = false;
        User user = context.getUser();
		if (!permission.isAuthorized(getUrl(), request.getMethod(), user.isGuest() ? null
				: (CMISUser) user)) {
			return true;
		}
        
        switch (jcononPage.getExtendedAuthentication())
        {
        	case none:
        	{
                break;
        	}
            case guest:
            {
                login = user == null;
                break;
            }
            
            case user:
            {
                login = user == null || AuthenticationUtil.isGuest(user.getId());
                break;
            }
            
            case admin:
            {
                login = user == null || !user.isAdmin();
                if (login)
                {
                    // special case for admin - need to clear user context before
                    // we can login again to "upgrade" our user authentication level
                    AuthenticationUtil.clearUserContext(request);
                }
                break;
            }
            
            default:
            {
                login = user == null || AuthenticationUtil.isGuest(user.getId()) || isAutenticated(user);
                break;
            }
            
        }
        return login;
	}
	
	private boolean isAutenticated(User user){
		if (user.isGuest())
			return true;
		boolean login = true;
        if (user.isAdmin())
        	login = false;
		return login;
	}
}
