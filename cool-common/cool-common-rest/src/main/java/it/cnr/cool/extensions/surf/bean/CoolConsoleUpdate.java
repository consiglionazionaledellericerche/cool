package it.cnr.cool.extensions.surf.bean;

import it.cnr.cool.cmis.service.CacheService;
import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.web.PermissionServiceImpl;

import java.util.Map;

import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.ModelObjectPersister;
import org.springframework.extensions.surf.bean.ConsoleUpdate;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CoolConsoleUpdate extends ConsoleUpdate implements ApplicationContextAware {
	private FolderService folderService;
	private PermissionServiceImpl permission;
	private CacheService cacheService;
	
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void setFolderService(FolderService folderService) {
		this.folderService = folderService;
	}
	
	public void setPermission(PermissionServiceImpl permission) {
		this.permission = permission;
	}

	@Override
	protected void executeFinallyImpl(WebScriptRequest req, Status status,
			Cache cache, Map<String, Object> model) {
		super.executeFinallyImpl(req, status, cache, model);
        // actions
        boolean resetWebscripts = false;
        boolean resetTemplates = false;
        boolean resetObjects = false;

        // reset index
        String reset = req.getParameter("reset");
        
        if ("webscripts".equalsIgnoreCase(reset))
        {
            resetWebscripts = true;
        }
        if ("templates".equalsIgnoreCase(reset))
        {
            resetTemplates = true;
        }
        if ("objects".equalsIgnoreCase(reset))
        {
            resetObjects = true;
        }
        if ("all".equalsIgnoreCase(reset))
        {
            resetWebscripts = true;
            resetTemplates = true;
            resetObjects = true;
        }

		if (resetWebscripts||resetTemplates||resetObjects){
        	permission.loadPermission();
        	cacheService.clearCache();
        	folderService.clearFolderCache();
        }
        if (resetObjects){
            for(ModelObjectPersister persister: ThreadLocalRequestContext.getRequestContext().
            		getServiceRegistry().getPersisterService().getPersisters())
            {
            	persister.init(ThreadLocalRequestContext.getRequestContext().
            			getServiceRegistry().getObjectPersistenceService().getPersistenceContext());
            }
        }
	}
}
