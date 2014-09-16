package it.cnr.cool.cmis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.ClassPathStore;

public class CoolClassPathStore implements CoolStore {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CoolClassPathStore.class);

	@Autowired
	private VersionService versionService;

	private ClassPathStore store;

	public void setStore(ClassPathStore store) {
		this.store = store;
	}

	@Override
	public String[] getAllDocumentPaths() {

		String[] paths;

		if (versionService.isProduction()) {
			paths = store.getAllDocumentPaths();
			LOGGER.info(paths.length + " document paths");
		} else {
			LOGGER.warn("development mode, avoid scan document paths");
			paths = new String[0];
		}

		return paths;

	}

}
