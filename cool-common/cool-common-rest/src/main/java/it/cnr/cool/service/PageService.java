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

package it.cnr.cool.service;

import it.cnr.cool.cmis.service.VersionService;
import it.cnr.cool.dto.CoolPage;
import it.cnr.cool.dto.CoolPage.Authentication;
import it.cnr.cool.rest.SecurityRest;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.StringUtil;
import it.cnr.cool.web.PermissionService;
import it.cnr.mock.CnrRegion;
import it.cnr.mock.RequestUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@Service
public class PageService implements InitializingBean{

	private static final String PATH = "path";

	private static final String ID = "id";

	private static final String FORMAT_ID = "format-id";

	private static final String AUTHENTICATION = "authentication";

	private static final String ORDER_ID = "order-id";

	private static final String XML_PATH = "/pages/pages.xml";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PageService.class);
	public static final String SERVER_SERVLET_CONTEXT_PATH = "server.servlet.context-path";

	private Map<String, CoolPage> pages;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private CnrRegion cnrRegion;

	@Autowired
	private VersionService versionService;

	@Autowired
	private ApplicationContext applicationContext;

	@Inject
	private Environment env;

	private String overrideLang;

	public Map<String, CoolPage> loadPages() {
		return pages;
	}

	private Map<String, List<PageModel>> pageModels;

	/**
	 * 
	 * Initialization of a collection of CoolPages
	 * each one defines an ID and contains all the metadata relevant for the page
	 * 
	 */
	private void init() {

		Map<String, CoolPage> pages = new HashMap<String, CoolPage>();

		try {
			String xml = IOUtils.toString(PageService.class
					.getResourceAsStream(XML_PATH));
			LOGGER.debug(xml);
			Document doc = DocumentHelper.parseText(xml);

			@SuppressWarnings("unchecked")
			List<DefaultElement> pageElements = doc.getRootElement().elements(
					"page");

			for (DefaultElement pageElement : pageElements) {
				LOGGER.debug(pageElement.element(ID).getText());
				CoolPage page = getPage(pageElement);
				pages.put(pageElement.elementText(ID), page);
			}

		} catch (IOException e) {
			LOGGER.error("error loading document " + XML_PATH, e);

		} catch (DocumentException e) {
			LOGGER.error("error loading pages", e);
		}

		LOGGER.debug("available pages: " + pages.keySet().toString());

		this.pages = pages;
	}

	private CoolPage getPage(DefaultElement pageElement) {
		CoolPage page = new CoolPage(pageElement.elementText(PATH));
		page.setFormatId(pageElement.elementText(FORMAT_ID));

		// TODO: rifare con enum
		Authentication auth = null;

		if (pageElement.elementText(AUTHENTICATION).equalsIgnoreCase("admin")) {
			auth = Authentication.ADMIN;
		} else if (pageElement.elementText(AUTHENTICATION).equalsIgnoreCase(
				"user")) {
			auth = Authentication.USER;
		} else if (pageElement.elementText(AUTHENTICATION).equalsIgnoreCase(
				"guest")) {
			auth = Authentication.GUEST;
		} else {
			LOGGER.warn("error with page " + pageElement.elementText(ID));
		}

		page.setAuthentication(auth);

		if (pageElement.element(ORDER_ID) != null) {
			page.setOrderId(Integer.parseInt(pageElement.elementText(ORDER_ID)));
		}
		return page;
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
			String urlContext, final Locale locale, CMISUser user) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("message", applicationContext.getBean("messageMethod", locale, pageId));
		model.put("activeProfiles", env.getActiveProfiles());

		HashMap<String, String> pagex = new HashMap<String, String>();
		pagex.put(ID, pageId);
		model.put("page", pagex);

		HashMap<String, Object> url = new HashMap<String, Object>();
		url.put("context", urlContext);
		url.put("redirect", env.getProperty(SERVER_SERVLET_CONTEXT_PATH));
		model.put("url", url);

		Map<String, Object> context = getContext(user);

		context.put("properties", new HashMap<String, Object>());
		Map<String, Object> currentPage = new HashMap<String, Object>();
		currentPage.put(ID, pageId);
		context.put("page", currentPage);
		model.put("context", context);

		model.put("locale_suffix", Optional.ofNullable(locale.getLanguage())
				.filter(s -> s.matches(SecurityRest.REGEX))
				.orElse(RequestUtils.LANG.it.name()));

		model.put("pages", getPages());

		model.put("permission", permissionService);

		model.put("artifact_version", versionService.getVersion());

		Map<String, Object> request = new HashMap<String, Object>();
		Map<String, Object> requestContext = new HashMap<String, Object>();
		requestContext.put("requestMethod", req.getMethod());
		request.put("requestContext", requestContext);
		model.put("Request", request);
		model.put("queryString",
				Optional.ofNullable(req.getQueryString())
					.filter(s -> s.matches(SecurityRest.REGEX))
					.orElse(null)
		);
		model.put("region", cnrRegion);

		Map<String, Object> args = new HashMap<String, Object>();

		Map<String, String[]> paramz = req.getParameterMap();
		for (Object key : paramz.keySet()) {
			String[] valuez = (String[]) paramz.get(key);
			if (valuez.length > 0) {
				final String s = valuez[0];
				if (s.matches(SecurityRest.REGEX)) {
					args.put(StringUtil.removeXSS((String) key), StringUtil.removeXSS(s));
				}
			}
		}

		model.put("args", args);
		model.put("RequestParameters", args);
		if (Optional.ofNullable(pageModels)
				.filter(stringListMap -> stringListMap.containsKey(pageId))
				.isPresent()) {
			LOGGER.info("User: {} with IP: {} Path: {} {}", user.getId(), req.getRemoteAddr(), req.getMethod(), req.getPathInfo());
			pageModels.get(pageId)
					.stream()
					.forEach(pageModel -> model.putAll(pageModel.addToModel(paramz)));
		}
		Optional<Object> csrf = Optional.ofNullable(req.getAttribute("_csrf"));
		if (csrf.isPresent()) {
			model.put("_csrf", csrf.get());
		}
		return model;
	}

	private List<Map<String, Object>> getPages() {

		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();

		for (Map.Entry<String, CoolPage> c : pages.entrySet()) {

			CoolPage page = c.getValue();

			if (page.getFormatId() != null) {

				Map<String, Object> item = new HashMap<String, Object>();
				item.put(ID, c.getKey());
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

	public String getOverrideLang() {
		return overrideLang;
	}

	public void setOverrideLang(String overrideLang) {
		this.overrideLang = overrideLang;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void registerPageModels(String pageId, PageModel pageModel) {
		this.pageModels = Optional.ofNullable(pageModels)
				.orElse(new HashMap<>());
		if (pageModels.containsKey(pageId)) {
			pageModels.get(pageId).add(pageModel);
		} else {
			pageModels.put(pageId, Arrays.asList(pageModel));
		}
	}
}
