package it.cnr.cool.service;

import freemarker.core.Environment;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.util.StringUtil;
import it.cnr.cool.web.PermissionService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PageService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PageService.class);

	private static final Locale LOCALE = Locale.ENGLISH; // TODO: rimuovere

	private static final String MAIN_REGION = "main";

	private Map<String, CoolPage> pages;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private I18nService i18nService;

	@Autowired
	private VersionService versionService;

	public Map<String, CoolPage> loadPages() {
		return pages;
	}

	// TODO: generare dinamicamente???
	@SuppressWarnings("unused")
	private void init() {

		Map<String, CoolPage> pages = new HashMap<String, CoolPage>();

		// home
		CoolPage home = new CoolPage("/pages/home/main.get.html.ftl");
		home.setNavbar(true);
		home.setAuthentication(CoolPage.Authentication.USER);
		home.setOrderId(0);

		pages.put("home", home);

		// jsConsole
		CoolPage jsConsole = new CoolPage(
				"/surf/webscripts/jsConsole/console.get.html.ftl");
		jsConsole.setNavbar(true);
		jsConsole.setOrderId(20);
		jsConsole.setAuthentication(CoolPage.Authentication.ADMIN);
		pages.put("jsConsole", jsConsole);

		// login
		CoolPage login = new CoolPage(
				"/pages/security/login/login.get.html.ftl");
		login.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("login", login);

		// footer
		CoolPage footer = new CoolPage(
				"/surf/webscripts/footer/footer.get.html.ftl");
		footer.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("footer", footer);

		// header
		CoolPage header = new CoolPage(
				"/surf/webscripts/header/header.get.html.ftl");
		header.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("header", header);

		LOGGER.debug("available pages: " + pages.keySet().toString());

		this.pages = pages;
	}

	public Map<String, Object> getModel(String pageId, String contextS) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("msg", new CnrMsgMethod());
		model.put("message", new CnrMessageMethod());
		HashMap<String, String> pagex = new HashMap<String, String>();
		pagex.put("id", pageId);
		model.put("page", pagex);

		HashMap<String, Object> url = new HashMap<String, Object>();
		// Map<String, Object> context = new HashMap<String, Object>();
		// context.put("user", "user");
		url.put("context", contextS);
		model.put("url", url);

		Map<String, Object> context = new HashMap<String, Object>();
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("isGuest", true);
		context.put("user", user);
		context.put("properties", new HashMap<String, Object>());
		model.put("context", context);

		model.put("artifact_version", "0.0");
		model.put("locale", "en_US");

		List<Object> pagess = new ArrayList<Object>();
		Map<String, Object> myPage = new HashMap<String, Object>();
		myPage.put("id", "qualcosa");
		pagess.add(myPage);
		model.put("pages", pagess);

		model.put("permission", permissionService);

		Map<String, Object> request = new HashMap<String, Object>();
		Map<String, Object> requestContext = new HashMap<String, Object>();
		requestContext.put("requestMethod", "GET");
		request.put("requestContext", requestContext);
		model.put("Request", request);

		Map<String, Object> requestParameters = new HashMap<String, Object>();
		model.put("RequestParameters", requestParameters);

		model.put("region", new CnrRegion());

		model.put("cmisDateFormat", StringUtil.CMIS_DATEFORMAT);

		model.put("isProduction", versionService.isProduction());

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("failure", "");
		model.put("args", args);

		return model;
	}

	class CnrMsgMethod implements TemplateMethodModelEx {

		@Override
		public Object exec(@SuppressWarnings("rawtypes") List arguments)
				throws TemplateModelException {
			LOGGER.debug(arguments.size() + " arguments" + arguments.toString());
			return "MSG";
		}

	}

	class CnrMessageMethod implements TemplateMethodModelEx {

		@Override
		public Object exec(@SuppressWarnings("rawtypes") List arguments)
				throws TemplateModelException {
			LOGGER.debug(arguments.size() + " arguments: "
					+ arguments.toString());
			String key = ((SimpleScalar) arguments.get(0)).getAsString();
			String label = i18nService.getLabel(key, LOCALE);
			LOGGER.debug(key + ": " + label);
			return label != null ? label : key;
		}

	}

	class CnrRegion implements TemplateDirectiveModel {

		@Override
		public void execute(Environment env,
				@SuppressWarnings("rawtypes") Map params,
				TemplateModel[] loopVars, TemplateDirectiveBody body)
				throws TemplateException, IOException {

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

			CoolPage pageToDisplay = pages.get(idPage);
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

}
