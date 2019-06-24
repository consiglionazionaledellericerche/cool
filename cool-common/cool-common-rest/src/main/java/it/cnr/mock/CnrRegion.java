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

package it.cnr.mock;

import freemarker.core.Environment;
import freemarker.template.*;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CnrRegion implements TemplateDirectiveModel {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CnrRegion.class);

	private static final String MAIN_REGION = "main";

	@Autowired
	private PageService pageService;

	@Override
	public void execute(Environment env,
			@SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {

		String regionId = params.get("id").toString();
		LOGGER.debug("region id: " + regionId);

		Map<String, Object> regionModel = getRegionModel(env);

		String idPage;

		if (regionId.equals(MAIN_REGION)) {
			SimpleHash pageForRegion = (SimpleHash) env
					.getGlobalVariable("page");
			idPage = pageForRegion.get("id").toString();
		} else {
			idPage = regionId;
		}

		CoolPage pageToDisplay = pageService.loadPages().get(idPage);
		LOGGER.debug("will render: " + idPage);
		LOGGER.debug(pageToDisplay.toString());

		String regionContent = Util.processTemplate(regionModel,
				pageToDisplay.getUrl());

		LOGGER.debug(regionContent);

		Writer outt = env.getOut();
		outt.write(regionContent);
		outt.close();

	}

	private Map<String, Object> getRegionModel(Environment env)
			throws TemplateModelException {
		Map<String, Object> regionModel = new HashMap<String, Object>();

		@SuppressWarnings("unchecked")
		Set<String> names = env.getKnownVariableNames();
		for (String name : names) {
			LOGGER.debug("adding variable " + name);
			regionModel.put(name, env.getGlobalVariable(name));
		}
		return regionModel;
	}

}