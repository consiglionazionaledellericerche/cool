package it.cnr.cool.service;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.CalendarUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateAccountService {

	private static final String FTL_PATH = "/surf/webscripts/security/create/account.registration.html";

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateAccountService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

	@Autowired
	private CMISService cmisService;

	@Autowired
	private I18nService i18nService;


	public Response update(HttpServletRequest request,
			MultivaluedMap<String, String> form) {
		return manage(request, form, false, null);
	}

	public Response create(HttpServletRequest request,
			MultivaluedMap<String, String> form, String url) {
		return manage(request, form, true, url);
	}

	private Response manage(HttpServletRequest request,
			MultivaluedMap<String, String> form, boolean create, String url) {
		CMISUser user = new CMISUser();
		Map<String, Object> model = new HashMap<String, Object>();

		ResponseBuilder rb;
		try {
			LOGGER.info(form.toString());
	        CalendarUtil.getBeanUtils().populate(user, form);
			
			String codicefiscale = user.getCodicefiscale();
			if (codicefiscale != null && !codicefiscale.equals(codicefiscale.toUpperCase())) {
				LOGGER.info("transforming codicefiscale to UpperCase for user "
						+ user.getId() + " [" + codicefiscale + "]");
				user.setCodicefiscale(codicefiscale.toUpperCase());
			}
			

			LOGGER.info((create ? "creation" : "update") + " for user " + user.getId() + " requested");


			if (create) {
				model = createUser(user, request.getLocale(), url);
			} else {
				// aggiorna l'utente e se l'utente ha modificato i suoi dati li
				// ricarica in sessione
				model = saveUser(user);


				HttpSession session = request.getSession(false);
				CMISUser currentUser = cmisService.getCMISUserFromSession(session);

				boolean isLoggedUserData = user.getUserName().equalsIgnoreCase(currentUser.getId());

				if (!model.containsKey("error") && isLoggedUserData) {
					reloadUserInSession(user, request);
				}

			}

			if (model.containsKey("account")) {


				Map<String, Object> data = new HashMap<String, Object>();
				Map<String, Object> userData = new HashMap<String, Object>();

				CMISUser myUser = (CMISUser) model.get("account");

				LOGGER.info("operation successfull: " + myUser.toString());

				userData.put("fullName", myUser.getFullName());
				userData.put("email", myUser.getEmail());

				data.put("account", userData);
				rb = Response.ok(data);
			} else {
				LOGGER.warn("possibile anomalia " + model);
				rb = Response.ok(model);
			}

		} catch (UserFactoryException e) {
			LOGGER.warn(e.getMessage(), e);
			model.put("error", "message.user.alredy.exists");

			rb = Response.ok(model);
		} catch (IllegalAccessException e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return rb.build();

	}



	private Map<String, Object> saveUser(CMISUser user) throws UserFactoryException {
		Map<String, Object> model = new HashMap<String, Object>();
		CMISUser tempUser;
		try {
			tempUser = userService.loadUserForConfirm(user.getUserName());
			if (tempUser.getEmail() != null && !tempUser.getEmail().equals(user.getEmail())){
				CMISUser emailuser = userService.findUserByEmail(user.getEmail(), cmisService.getAdminSession());
				if(emailuser != null && !emailuser.getId().equals(tempUser.getId()))
					model.put("error", "message.email.alredy.exists");
			} else if (tempUser.getCodicefiscale() != null && !tempUser.getCodicefiscale().equals(user.getCodicefiscale())){
				CMISUser userCodfis = userService.findUserByCodiceFiscale(
						user.getCodicefiscale(), cmisService.getAdminSession());
				if(userCodfis != null && !userCodfis.getId().equals(tempUser.getId()))
					model.put("error", "message.taxcode.alredy.exists");
			}

			if (!model.containsKey("error")){
				userService.updateUser(user);
			}
		} catch (CoolUserFactoryException e) {
			throw new UserFactoryException("Error loading user: " + user.getId(), e);
		}

		return model;
	}

	private void reloadUserInSession(CMISUser oldUser, HttpServletRequest request) throws UserFactoryException {
		CMISUser user;
		try {
			BindingSession bindingSession = cmisService.getCurrentBindingSession(request);
			user = userService.loadUser(oldUser.getId(), bindingSession);
		} catch (CoolUserFactoryException e) {
			throw new UserFactoryException("Error loading user: " + oldUser.getId(), e);
		}
		request.getSession(false).setAttribute(CMISUser.SESSION_ATTRIBUTE_KEY_USER_OBJECT, user);
	}

	/*
	 * crea un nuovo utente e invia una mail di conferma
	 */
	private Map<String, Object> createUser(CMISUser user, Locale locale, String url)
			throws UserFactoryException {
		Map<String, Object> model = new HashMap<String, Object>();
		user.setDisableAccount(true);
		user.setPin(UUID.randomUUID().toString());

		try {
			if (userService.findUserByEmail(user.getEmail(), cmisService.getAdminSession()) != null)
				model.put("error", "message.email.alredy.exists");
			else if (user.getCodicefiscale() != null
					&& user.getCodicefiscale().length() > 0
					&& userService.findUserByCodiceFiscale(user.getCodicefiscale(),
							cmisService.getAdminSession()) != null)
				model.put("error", "message.taxcode.alredy.exists");
			else {
				CMISUser newUser = userService.createUser(user);
				model.put("account", newUser);
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("User created " + newUser.getFullName());
				model.put("url", url);
				sendConfirmMail(model, locale);
			}
		} catch (CoolUserFactoryException e) {
			throw new UserFactoryException(e.getMessage(), e);
		}
		return model;
	}

	private void sendConfirmMail(Map<String, Object> model, Locale locale) {
		CMISUser user = (CMISUser) model.get("account");

		String templatePath = i18nService.getTemplate(FTL_PATH, locale);

		try {
			String content = Util.processTemplate(model, templatePath);
			LOGGER.debug(content);
			String subject = i18nService.getLabel("subject-confirm-account", locale);
			mailService.send(user.getEmail(), subject, content);
		} catch (TemplateException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}


	}
}
