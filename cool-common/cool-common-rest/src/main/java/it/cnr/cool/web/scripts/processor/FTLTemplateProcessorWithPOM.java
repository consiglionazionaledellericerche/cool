package it.cnr.cool.web.scripts.processor;

import it.cnr.cool.cmis.service.VersionService;

import java.util.Map;

import org.springframework.extensions.webscripts.processor.FTLTemplateProcessor;

public class FTLTemplateProcessorWithPOM extends FTLTemplateProcessor {
    private VersionService versionService;
    
    public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	@SuppressWarnings("unchecked")
	@Override
    protected void addProcessorModelExtensions(Object model) {
    	super.addProcessorModelExtensions(model);
    	String version = versionService.getVersion();
	    if (version != null)
	    	((Map<String, Object>)model).put("artifact_version", version);
    }
}