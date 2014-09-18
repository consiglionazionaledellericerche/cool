package it.cnr.mock;

import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.PageService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import freemarker.core.Environment;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

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