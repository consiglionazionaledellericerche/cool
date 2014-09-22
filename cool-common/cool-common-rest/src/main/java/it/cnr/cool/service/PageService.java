package it.cnr.cool.service;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.web.PermissionService;
import it.cnr.mock.CnrRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PageService {

	private static final String ORDER_ID = "order-id";


	private static final String FORMAT_ID = "format-id";


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

	@Autowired
	private CMISService cmisService;

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
		home.setFormatId("navbar");
		home.setAuthentication(CoolPage.Authentication.USER);
		home.setOrderId(0);
		pages.put("home", home);

		CoolPage jsConsole = new CoolPage(
				"/surf/webscripts/jsConsole/console.get.html.ftl");
		jsConsole.setFormatId("navbar/admin");
		jsConsole.setOrderId(3);
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

		CoolPage accounting = new CoolPage(
				"/surf/webscripts/accounting.get.html.ftl");
		accounting.setAuthentication(CoolPage.Authentication.USER);
		accounting.setOrderId(100);
		accounting.setFormatId("navbar");
		pages.put("accounting", accounting);

		CoolPage frontOffice = new CoolPage(
				"/surf/webscripts/frontOffice/reader.get.html.ftl");
		frontOffice.setAuthentication(CoolPage.Authentication.USER);
		frontOffice.setOrderId(9);
		frontOffice.setFormatId("navbar/admin");
		pages.put("frontOffice", frontOffice);

		CoolPage frontOfficeCreateModify = new CoolPage(
				"/surf/webscripts/frontOfficeCreateModify/createModify.get.html.ftl");
		frontOfficeCreateModify.setAuthentication(CoolPage.Authentication.USER);
		frontOfficeCreateModify.setOrderId(9);
		frontOfficeCreateModify.setFormatId("navbar/admin");
		pages.put("frontOfficeCreateModify", frontOfficeCreateModify);

		CoolPage gestioneUtenti = new CoolPage(
				"/surf/webscripts/gestioneUtenti/gestione-utenti.get.html.ftl");
		gestioneUtenti.setAuthentication(CoolPage.Authentication.USER);
		gestioneUtenti.setOrderId(11);
		gestioneUtenti.setFormatId("navbar/admin");
		pages.put("gestione-utenti", gestioneUtenti);

		CoolPage groups = new CoolPage(
				"/surf/webscripts/groups/main.get.html.ftl");
		groups.setAuthentication(CoolPage.Authentication.USER);
		groups.setOrderId(2);
		groups.setFormatId("navbar/admin");
		pages.put("groups", groups);


		CoolPage rbacAdmin = new CoolPage(
				"/surf/webscripts/rbac-admin/rbac-admin.get.html.ftl");
		rbacAdmin.setAuthentication(CoolPage.Authentication.USER);
		rbacAdmin.setOrderId(10);
		rbacAdmin.setFormatId("navbar/admin");
		pages.put("rbac-admin", rbacAdmin);

		CoolPage ricerca = new CoolPage(
				"/surf/webscripts/ricerca/ricerca.get.html.ftl");
		ricerca.setAuthentication(CoolPage.Authentication.USER);
		ricerca.setOrderId(11);
		ricerca.setFormatId("navbar/admin");
		pages.put("ricerca", ricerca);

		CoolPage createAccount = new CoolPage(
				"/surf/webscripts/security/create/account.get.html.ftl");
		createAccount.setAuthentication(CoolPage.Authentication.GUEST);
		createAccount.setOrderId(1);
		createAccount.setFormatId("navbar/admin");
		pages.put("create-account", createAccount);


		CoolPage dashboard = new CoolPage(
				"/surf/webscripts/dashboard/dashboard.get.html.ftl");
		dashboard.setAuthentication(CoolPage.Authentication.USER);
		dashboard.setOrderId(50);
		dashboard.setFormatId("navbar/workflow");
		pages.put("dashboard", dashboard);

		CoolPage updateModel = new CoolPage(
				"/surf/webscripts/modelDesigner/createModify.get.html.ftl");
		updateModel.setAuthentication(CoolPage.Authentication.USER);
		updateModel.setOrderId(101);
		updateModel.setFormatId("navbar/workflow");
		pages.put("updateModel", updateModel);

		CoolPage modelDesigner = new CoolPage(
				"/surf/webscripts/modelDesigner/modelDesigner.get.html.ftl");
		modelDesigner.setAuthentication(CoolPage.Authentication.USER);
		modelDesigner.setOrderId(99);
		modelDesigner.setFormatId("navbar/admin");
		pages.put("modelDesigner", modelDesigner);

		CoolPage editView = new CoolPage(
				"/surf/webscripts/editView/editView.get.html.ftl");
		editView.setAuthentication(CoolPage.Authentication.USER);
		editView.setOrderId(123);
		editView.setFormatId("navbar/admin");
		pages.put("editView", editView);


		CoolPage workflow = new CoolPage("/pages/workflow/main.get.html.ftl");
		workflow.setAuthentication(CoolPage.Authentication.USER);
		workflow.setOrderId(0);
		pages.put("workflow", workflow);

		CoolPage zipperReader = new CoolPage(
				"/pages/zipper/zipper.get.html.ftl");
		zipperReader.setAuthentication(CoolPage.Authentication.USER);
		zipperReader.setOrderId(1);
		editView.setFormatId("navbar");
		pages.put("zipperReader", zipperReader);

		CoolPage workflowHistory = new CoolPage(
				"/pages/workflow/history.get.html.ftl");
		workflowHistory.setAuthentication(CoolPage.Authentication.USER);
		workflowHistory.setOrderId(3);
		editView.setFormatId("navbar/workflow");
		pages.put("workflowHistory", workflowHistory);

		CoolPage workflowManagement = new CoolPage(
				"/pages/workflow/management.get.html.ftl");
		workflowManagement.setAuthentication(CoolPage.Authentication.USER);
		workflowManagement.setOrderId(4);
		editView.setFormatId("navbar/workflow");
		pages.put("workflowManagement", workflowManagement);

		CoolPage search = new CoolPage("/pages/search/main.post.html.ftl");
		search.setAuthentication(CoolPage.Authentication.GUEST);
		pages.put("search", search);

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
	 * @param urlContext
	 * @return
	 */
	public Map<String, Object> getModel(HttpServletRequest req, String pageId,
			String urlContext,
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
		url.put("context", urlContext);
		model.put("url", url);

		CMISUser user = cmisService.getCMISUserFromSession(req
				.getSession(false));
		Map<String, Object> context = getContext(user);

		context.put("properties", new HashMap<String, Object>());
		Map<String, Object> currentPage = new HashMap<String, Object>();
		currentPage.put("id", pageId);
		context.put("page", currentPage);
		model.put("context", context);

		model.put("locale", "en_US");

		model.put("pages", getPages());

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

		Map paramz = req.getParameterMap();
		for (Object key : paramz.keySet()) {
			String [] valuez =  (String[]) paramz.get(key);
			if (valuez.length > 0) {
				args.put((String) key, valuez[0]);
			}

		}

		model.put("args", args);

		return model;
	}

	private List<Map<String, Object>> getPages() {

		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();

		for (Map.Entry<String, CoolPage> c : pages.entrySet()) {

			CoolPage page = c.getValue();

			if (page.getFormatId() != null) {

				Map<String, Object> item = new HashMap<String, Object>();
				item.put("id", c.getKey());
				item.put(FORMAT_ID, page.getFormatId());
				item.put(ORDER_ID, page.getOrderId());
				l.add(item);

			}

		}

		Collections.sort(l, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> page1,
					Map<String, Object> page2) {
				return normalizeFormatId(page1.get(ORDER_ID)).compareTo(
						normalizeFormatId(page2.get(ORDER_ID)));
			}

			private Integer normalizeFormatId(Object formatId) {

				Integer n;
				try {
					if (formatId != null) {
						n = Integer.parseInt(formatId.toString());
					} else {
						n = Integer.MAX_VALUE;
					}
				} catch (NumberFormatException e) {
					n = Integer.MAX_VALUE;
				}

				return n;

			}
		});

		return l;
	}

	public Map<String, Object> getContext(CMISUser user) {
		Map<String, Object> context = new HashMap<String, Object>();

		if (user == null) {
			user = new CMISUser("guest");
			Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
			capabilities.put(CMISUser.CAPABILITY_GUEST, true);
			user.setCapabilities(capabilities);
		}
		context.put("user", user);
		return context;
	}

}
