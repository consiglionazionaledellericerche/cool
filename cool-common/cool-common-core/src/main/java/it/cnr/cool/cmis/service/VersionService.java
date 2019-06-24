/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.cmis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Service
public class VersionService implements InitializingBean {
	private static final String PRODUCTION = "PRODUCTION";

	private static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionService.class);


	private String version = "0.0";

	private boolean production =  false;

	@Autowired(required = false)
	private ServletContext context;

	public void afterPropertiesSet() {
		if (!Optional.ofNullable(context).isPresent()) {
			version = "UNKNOWN";
			return;
		}
		InputStream is = context.getResourceAsStream(MANIFEST_PATH);
		if (is != null) {
			try {
				Manifest manifest = new Manifest(is);
				Attributes attributes = manifest.getMainAttributes();
				String mode = attributes.getValue("mode");
				if (mode != null) {
					production = mode.equalsIgnoreCase(PRODUCTION);
				}
				String implementationVersion = attributes.getValue("Implementation-Version");
				if (implementationVersion != null) {
					version = implementationVersion;
				} else {
					version = "UNKNOWN-" + new Date().getTime();
				}
			} catch (IOException e) {
				LOGGER.warn("unable to retrieve implementation version", e);
			}
		}

		LOGGER.info("profilo: " + (production ? "PRODUZIONE" : "SVILUPPO"));
		LOGGER.info("versione: " + version);
	}

	public String getVersion() {
		return version;
	}

	public boolean isProduction() {
		return production;
	}
}
