package it.cnr.cool.service;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.CalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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


	public Map<String, Object> update(Map<String,List<String>> form, Locale locale) {
		return manage(form, locale, false, null);
	}

	public Map<String, Object> create(Map<String,List<String>> form, Locale locale, String url) {
		return manage(form, locale, true, url);
	}

	private Map<String, Object> manage(Map<String,List<String>> form, Locale locale, boolean create, String url) {
		CMISUser user = new CMISUser();
		Map<String, Object> model = new HashMap<String, Object>();

		try {
			LOGGER.debug(form.toString());
	        CalendarUtil.getBeanUtils().populate(user, form);
			
			String codicefiscale = user.getCodicefiscale();
			if (codicefiscale != null && !codicefiscale.equals(codicefiscale.toUpperCase())) {
				LOGGER.debug("transforming codicefiscale to UpperCase for user "
						+ user.getId() + " [" + codicefiscale + "]");
				user.setCodicefiscale(codicefiscale.toUpperCase());
			}
			

			LOGGER.debug((create ? "creation" : "update") + " for user " + user.getId() + " requested");


			if (create) {
				model = createUser(user, locale, url);
			} else {
				// aggiorna l'utente e se l'utente ha modificato i suoi dati li
				// ricarica in sessione
				model = saveUser(user, locale);
			}

			if (model.containsKey("account")) {


				Map<String, Object> data = new HashMap<String, Object>();
				Map<String, Object> userData = new HashMap<String, Object>();

				CMISUser myUser = (CMISUser) model.get("account");

				LOGGER.debug("operation successfull: " + myUser.toString());

				userData.put("fullName", myUser.getFullName());
				userData.put("email", myUser.getEmail());
				userData.put("pin", myUser.getPin());

				data.put("account", userData);
				data.put("user", myUser);
				
				model.putAll(data);
			} else {
				LOGGER.warn("possibile anomalia: " + String.valueOf(model.get("error")) + " userId:" + user.getId(), user, model);
				throw new CoolException(String.valueOf(model.get("error")));
			}

		} catch (UserFactoryException |IllegalAccessException | InvocationTargetException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new CoolException(e.getMessage(), e);
		}
		return model;
	}



	private Map<String, Object> saveUser(CMISUser user, Locale locale) throws UserFactoryException {
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
				if(userCodfis != null && !userCodfis.getId().equals(tempUser.getId())) {
					userCodfis = userService.loadUserForConfirm(userCodfis.getUserName());				
					model.put("error", i18nService.getLabel("message.taxcode.alredy.exists", locale, userCodfis.getUserName(), (userCodfis.getImmutability() == null || userCodfis.getImmutability().isEmpty()? "" : i18nService.getLabel("message.user.cnr", locale))));					
				}
			}
			if (!model.containsKey("error")){
				model.put("account", userService.updateUser(user));
			}
		} catch (CoolUserFactoryException e) {
			throw new UserFactoryException("Error loading user: " + user.getId(), e);
		}
		return model;
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
			if (userService.findUserByEmail(user.getEmail(), cmisService.getAdminSession()) != null) {
				model.put("error", "message.email.alredy.exists");
				return model;
			} else if (user.getCodicefiscale() != null
					&& user.getCodicefiscale().length() > 0) {
				CMISUser userCodfis = userService.findUserByCodiceFiscale(
						user.getCodicefiscale(), cmisService.getAdminSession());
				if (userCodfis != null && !userCodfis.isNoMail()) {
					userCodfis = userService.loadUserForConfirm(userCodfis.getUserName());
					model.put("error", i18nService.getLabel("message.taxcode.alredy.exists", locale, userCodfis.getUserName(), (userCodfis.getImmutability() == null || userCodfis.getImmutability().isEmpty()? "" : i18nService.getLabel("message.user.cnr", locale))));					
					return model;
				}
			}
			CMISUser newUser = userService.createUser(user);
			model.put("account", newUser);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("User created " + newUser.getFullName());
			model.put("url", url);
			sendConfirmMail(model, locale);
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
