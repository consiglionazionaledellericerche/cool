package it.cnr.cool.cmis.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class VersionService {
	private static final String PRODUCTION = "PRODUCTION";

	private static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionService.class);

    private String version;

	private boolean production;

	@Autowired
	private ServletContext context;	

	public void init() {
		InputStream is = context.getResourceAsStream(MANIFEST_PATH);
		if (is != null) {
			try {
				Manifest manifest = new Manifest(is);
				Attributes attributes = manifest.getMainAttributes();
				String mode = attributes.getValue("mode");
				if (mode != null) {
					version = attributes.getValue("Implementation-Version");
					production = mode.equalsIgnoreCase(PRODUCTION);
				}
			} catch (IOException e) {
				LOGGER.warn("unable to retrieve implementation version", e);
			}
		}
	}

	public String getVersion() {
		if (version == null) {

    	    // fallback to using Java API
    	    if (version == null) {
    	        Package aPackage = getClass().getPackage();
    	        if (aPackage != null) {
    	            version = aPackage.getImplementationVersion();
    	            if (version == null) {
    	                version = aPackage.getSpecificationVersion();
    	            }
    	        }
    	    }
    	    if (version == null)
    	    	version = "1.0";
		}
		return version;
	}

	public boolean isProduction() {
		return production;
	}
}
