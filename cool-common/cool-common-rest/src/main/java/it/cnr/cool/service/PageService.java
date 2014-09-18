package it.cnr.cool.service;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.web.PermissionService;
import it.cnr.mock.CnrRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PageService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PageService.class);


	private Map<String, CoolPage> pages;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private I18nService i18nService;

	@Autowired
	private CnrRegion cnrRegion;

	@Autowired
	private VersionService versionService;

	public Map<String, CoolPage> loadPages() {
		return pages;
	}

	@SuppressWarnings("unused")
	/**
	 * 
	 * Initialization of a collection of CoolPages
	 * each one defines an ID and contains all the metadata relevant for the page
	 * 
	 */
	private void init() {
		// TODO: generare dinamicamente???

		Map<String, CoolPage> pages = new HashMap<String, CoolPage>();

		CoolPage home = new CoolPage("/pages/home/main.get.html.ftl");
		home.setNavbar(true);
		home.setAuthentication(CoolPage.Authentication.USER);
		home.setOrderId(0);
		pages.put("home", home);

		CoolPage jsConsole = new CoolPage(
				"/surf/webscripts/jsConsole/console.get.html.ftl");
		jsConsole.setNavbar(true);
		jsConsole.setOrderId(20);
		jsConsole.setAuthentication(CoolPage.Authentication.ADMIN);
		pages.put("jsConsole", jsConsole);

		CoolPage login = new CoolPage(
				"/pages/security/login/login.get.html.ftl");
		login.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("login", login);

		CoolPage footer = new CoolPage(
				"/surf/webscripts/footer/footer.get.html.ftl");
		footer.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("footer", footer);

		CoolPage header = new CoolPage(
				"/surf/webscripts/header/header.get.html.ftl");
		header.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("header", header);

		LOGGER.debug("available pages: " + pages.keySet().toString());

		this.pages = pages;
	}

	/**
	 * 
	 * generate a model (i.e. a Map) used by Freemarker to populate the content
	 * of the page
	 * 
	 * @param pageId
	 *            CoolPage identifier
	 * @param contextS
	 * @return
	 */
	public Map<String, Object> getModel(String pageId, String contextS,
			final Locale locale) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("message", new TemplateMethodModelEx() {

			@Override
			public Object exec(List arguments) throws TemplateModelException {
				LOGGER.debug(arguments.size() + " arguments: "
						+ arguments.toString());
				String key = ((SimpleScalar) arguments.get(0)).getAsString();
				String label = i18nService.getLabel(key, locale);
				LOGGER.debug(key + ": " + label);
				return label != null ? label : key;
			}
		});

		HashMap<String, String> pagex = new HashMap<String, String>();
		pagex.put("id", pageId);
		model.put("page", pagex);

		HashMap<String, Object> url = new HashMap<String, Object>();
		url.put("context", contextS);
		model.put("url", url);

		Map<String, Object> context = new HashMap<String, Object>();
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("isGuest", true);
		context.put("user", user);
		context.put("properties", new HashMap<String, Object>());
		model.put("context", context);

		model.put("locale", "en_US");

		List<Object> pagess = new ArrayList<Object>();
		Map<String, Object> myPage = new HashMap<String, Object>();
		myPage.put("id", "qualcosa");
		pagess.add(myPage);
		model.put("pages", pagess);

		model.put("permission", permissionService);

		model.put("artifact_version", versionService.getVersion());

		Map<String, Object> request = new HashMap<String, Object>();
		Map<String, Object> requestContext = new HashMap<String, Object>();
		requestContext.put("requestMethod", "GET");
		request.put("requestContext", requestContext);
		model.put("Request", request);

		Map<String, Object> requestParameters = new HashMap<String, Object>();
		model.put("RequestParameters", requestParameters);

		model.put("region", cnrRegion);


		Map<String, Object> args = new HashMap<String, Object>();
		args.put("failure", "");
		model.put("args", args);

		return model;
	}

}
