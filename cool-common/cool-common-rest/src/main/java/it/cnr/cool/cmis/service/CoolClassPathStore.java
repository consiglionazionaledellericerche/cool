package it.cnr.cool.cmis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CoolClassPathStore implements CoolStore {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CoolClassPathStore.class);

	@Autowired
	private VersionService versionService;

	@Override
	public String[] getAllDocumentPaths() {

		String[] paths;

		if (versionService.isProduction()) {
			// paths = getAllDocumentPathsInner();

			// LOGGER.info(paths.length + " document paths");
			paths = new String[0];
			LOGGER.error("RRD SERVICE NOT WORKING, PLEASE DO MANUAL UPLOAD");
		} else {
			LOGGER.warn("development mode, avoid scan document paths");
			paths = new String[0];
		}

		return paths;

	}

}
