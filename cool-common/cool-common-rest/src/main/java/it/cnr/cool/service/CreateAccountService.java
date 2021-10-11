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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Normalizer;
import java.util.*;
@Service
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

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${mail.from.default}")
    private String mailFromDefault;

    @Value("${mail.create.user.bcc.enabled}")
    private Boolean mailCreateUserBCCEnabled;

    public Map<String, Object> update(Map<String, List<String>> form, Locale locale) {
        try {
            LOGGER.debug(form.toString());
            CMISUser user = new CMISUser();
            CalendarUtil.getBeanUtils().populate(user, form);
            return manage(user, locale, false, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn(e.getMessage(), e);
            throw new CoolException(e.getMessage(), e);
        }
    }

    public Map<String, Object> create(Map<String, List<String>> form, Locale locale, String url) {
        try {
            LOGGER.debug(form.toString());
            CMISUser user = new CMISUser();
            CalendarUtil.getBeanUtils().populate(user, form);
            return manage(user, locale, true, url);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn(e.getMessage(), e);
            throw new CoolException(e.getMessage(), e);
        }
    }

    public CMISUser update(CMISUser user, Locale locale) {
        return manage(user, locale, false, null)
                .entrySet()
                .stream()
                .filter(stringObjectEntry -> stringObjectEntry.getKey().equals("user"))
                .map(Map.Entry::getValue)
                .findAny()
                .filter(CMISUser.class::isInstance)
                .map(CMISUser.class::cast)
                .orElseThrow(() -> new CoolException("Cannot update user"));
    }

    public CMISUser create(CMISUser user, Locale locale, String url) {
        return manage(user, locale, true, url)
                .entrySet()
                .stream()
                .filter(stringObjectEntry -> stringObjectEntry.getKey().equals("user"))
                .map(Map.Entry::getValue)
                .findAny()
                .filter(CMISUser.class::isInstance)
                .map(CMISUser.class::cast)
                .orElseThrow(() -> new CoolException("Cannot create user"));
    }

    private String normalize(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\W", "");
    }

    private Map<String, Object> manage(CMISUser user, Locale locale, boolean create, String url) {
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            String codicefiscale = user.getCodicefiscale();
            if (codicefiscale != null && !codicefiscale.equals(codicefiscale.toUpperCase())) {
                LOGGER.debug("transforming codicefiscale to UpperCase for user "
                        + user.getId() + " [" + codicefiscale + "]");
                user.setCodicefiscale(codicefiscale.toUpperCase());
            }
            LOGGER.debug((create ? "creation" : "update") + " for user " + user.getId() + " requested");
            if (create) {
                if (!Optional.ofNullable(user.getUserName())
                        .map(s -> s.trim()).isPresent()) {
                    String userName = normalize(user.getFirstName())
                            .toLowerCase()
                            .concat(".")
                            .concat(normalize(user.getLastName())
                                    .toLowerCase());
                    if (!userService.isUserExists(userName)) {
                        user.setUserName(userName);
                    } else {
                        for (int i = 1; i < 20; i++) {
                            final String concatUsername = userName.concat("0").concat(String.valueOf(i));
                            if (!userService.isUserExists(concatUsername)) {
                                user.setUserName(concatUsername);
                                break;
                            }
                        }
                    }
                }
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
                LOGGER.warn("possibile anomalia: " + model.get("error") + " userId:" + user.getId(), user, model);
                throw new CoolException(String.valueOf(model.get("error")));
            }

        } catch (UserFactoryException e) {
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
            if (tempUser.getEmail() != null && !tempUser.getEmail().equals(user.getEmail())) {
                CMISUser emailuser = userService.findUserByEmail(user.getEmail(), cmisService.getAdminSession());
                if (emailuser != null && !emailuser.getId().equals(tempUser.getId()))
                    model.put("error", "message.email.alredy.exists");
            } else if (tempUser.getCodicefiscale() != null && !tempUser.getCodicefiscale().equals(user.getCodicefiscale())) {
                CMISUser userCodfis = userService.findUserByCodiceFiscale(
                        user.getCodicefiscale(), cmisService.getAdminSession());
                if (userCodfis != null && !userCodfis.getId().equals(tempUser.getId())) {
                    userCodfis = userService.loadUserForConfirm(userCodfis.getUserName());
                    model.put("error", i18nService.getLabel("message.taxcode.alredy.exists", locale, userCodfis.getUserName(), (userCodfis.getImmutability() == null || userCodfis.getImmutability().isEmpty() ? "" : i18nService.getLabel("message.user.cnr", locale))));
                }
            }
            if (!model.containsKey("error")) {
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
                    model.put("error", i18nService.getLabel("message.taxcode.alredy.exists", locale, userCodfis.getUserName(), (userCodfis.getImmutability() == null || userCodfis.getImmutability().isEmpty() ? "" : i18nService.getLabel("message.user.cnr", locale))));
                    return model;
                }
            }
            CMISUser newUser = userService.createUser(user);
            model.put("account", newUser);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("User created " + newUser.getFullName());
            model.put("url", url);
            sendConfirmMail(model, locale);
            Timer timer = new Timer();
            timer.schedule(new DeleteUserTimerTask(newUser.getUserName()), 1000 * 60 * 60);

        } catch (CoolUserFactoryException e) {
            throw new UserFactoryException(e.getMessage(), e);
        }
        return model;
    }

    private void sendConfirmMail(Map<String, Object> model, Locale locale) {
        CMISUser user = (CMISUser) model.get("account");
        Map<String, Object> modelMail = new HashMap<String, Object>();
        modelMail.putAll(model);
        modelMail.put("message", applicationContext.getBean("messageMethod", locale));

        String templatePath = i18nService.getTemplate(FTL_PATH, locale);

        try {
            String content = Util.processTemplate(modelMail, templatePath);
            LOGGER.debug(content);
            String subject = i18nService.getLabel("subject-confirm-account", locale);
            mailService.send(user.getEmail(), null,
                    Optional.ofNullable(mailCreateUserBCCEnabled)
                        .filter(aBoolean -> aBoolean.equals(Boolean.TRUE))
                        .map(aBoolean -> mailFromDefault)
                        .orElse(null),
                    subject, content);
        } catch (TemplateException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    class DeleteUserTimerTask extends TimerTask {
        private String userName;

        public DeleteUserTimerTask(String userName) {
            this.userName = userName;
        }

        @Override
        public void run() {
            CMISUser user = Optional.ofNullable(
                    userService.loadUserForConfirm(Optional.ofNullable(userName).
                            orElseThrow(CoolUserFactoryException::new))).orElseThrow(CoolUserFactoryException::new);
            if (!user.getEnabled()) {
                userService.deleteUser(user);
            }
        }
    }
}
