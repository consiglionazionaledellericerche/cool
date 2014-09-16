package it.cnr.cool.extensions.surf.mvc;

import it.cnr.cool.web.PermissionServiceImpl;

import org.springframework.extensions.surf.mvc.PageViewResolver;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.types.Page;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class CoolPageViewResolver extends PageViewResolver {
	protected PermissionServiceImpl permission;

	public void setPermission(PermissionServiceImpl permission) {
		this.permission = permission;
	}

	@Override
	protected AbstractUrlBasedView buildView(String viewName) {
		CoolPageView view = null;
        Page page = ThreadLocalRequestContext.getRequestContext().getPage();
        if (page != null)
        {
            view = new CoolPageView(getWebframeworkConfigElement(), 
                                getModelObjectService(), 
                                getWebFrameworkResourceService(), 
                                getWebFrameworkRenderService(),
                                getTemplatesContainer(), permission);
            view.setUrl(viewName);
            view.setPage(page);
            view.setUriTokens(ThreadLocalRequestContext.getRequestContext().getUriTokens());
        }
        return view;
	}
}
