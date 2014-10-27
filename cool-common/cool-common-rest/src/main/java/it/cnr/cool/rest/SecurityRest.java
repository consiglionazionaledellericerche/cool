package it.cnr.cool.rest;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.dto.Credentials;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.CMISAuthenticatorFactory;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.CreateAccountService;
import it.cnr.cool.service.I18nService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Path("security")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class SecurityRest {

	private static final String TEMPLATE = "/surf/webscripts/security/create/account.change_password.html";

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRest.class);

	@Autowired
	private UserService userService;

	@Autowired
	private MailService mailService;

	@Autowired
	private CMISService cmisService;

	@Autowired
	private I18nService i18nService;

	@Autowired
	private CreateAccountService createAccountService;

	@Autowired
	private CMISAuthenticatorFactory cmisAuthenticatorFactory;

	@POST
	@Path("create-account")
	public Response doCreateUser(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {
		return createAccountService.create(request, form, getUrl(request));
	}

	@PUT
	@Path("create-account")
	public Response doUpdateUser(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {
		return createAccountService.update(request, form);

	}

	@POST
	@Path("change-password")
	public Response changePassword(@Context HttpServletRequest req,
			@FormParam("userid") String userId,
			@FormParam("pin") String pin, @FormParam("password") String password) {

		ResponseBuilder response = null;
		String error = null;

		HttpSession session = req.getSession(false);

		if (userId == null || userId.isEmpty()) {
			LOGGER.warn("no userid given");
			error = "message.user.not.found";
		} else {

			CMISUser user;

			try {
				user = userService.loadUserForConfirm(userId);

				if (user != null) {

					LOGGER.info("user " + userId + " retrieved");

					String currentUserId = null;

					CMISUser cmisUserFromSession = cmisService
							.getCMISUserFromSession(session);

					if (cmisUserFromSession != null) {
						currentUserId = cmisUserFromSession.getId();
						LOGGER.info("user logged as " + currentUserId);
					} else {
						LOGGER.info("user is guest");
					}

					LOGGER.debug(user.getPin());

					if (!user.getId().equals(currentUserId)) {
						if (pin.isEmpty() || !user.getPin().equalsIgnoreCase(pin)) {

							LOGGER.warn("pin is not valid");

							error = "message.pin.not.valid";
						} else {
							user.setPin("");
							userService.updateUser(user);
							userService.changeUserPassword(user, password);
							if (session != null)
								session.invalidate();
							LOGGER.info("updated password for user " + userId);
							String json = getJson(user);
							response = Response.ok(json);
						}
					} else {
						LOGGER.warn("user " + currentUserId
								+ " cannot change password to user " + userId
								+ " (unimplemented)");
						error = "server error";
					}
				} else {
					LOGGER.warn("user " + userId + " not found");
					error = "message.user.not.found";
				}
			} catch (CoolUserFactoryException e) {
				LOGGER.warn("unable to update password for user " + userId, e);
				error = "message.user.not.found";
			}

		}

		if (error != null) {
			String json = getError(error);
			response = Response.serverError().entity(json);
		}

		return response.build();

	}

	@POST
	@Path("forgotPassword")
	public Response forgotPassword(@Context HttpServletRequest req,
			@FormParam("userName") String userName, @CookieParam("__lang") String __lang) {

		ResponseBuilder builder = null;

		String error = null;

		LOGGER.info("user " + userName + " forgot password");

		try {
			CMISUser user = userService.loadUserForConfirm(userName);
			if (user != null) {

				CMISUser cmisUser = user;

				LOGGER.debug("user retrieved" + user.getId());

				if (cmisUser.getImmutability() != null
						&& !cmisUser.getImmutability().isEmpty()) {

					error = "message.error.email.cnr.user";

				} else if (!cmisUser.getEnabled()) {
					error = "message.error.user.is.not.active";
				} else {
					cmisUser.setPin(UUID.randomUUID().toString());
					user = userService.updateUser(user);

					LOGGER.info("user " + user.getId() + " updated");

					Map<String, Object> model = new HashMap<String, Object>();
					model.put("account", user);

					model.put("url", getUrl(req));

					try {
						Locale locale = I18nService.getLocale(req, __lang);

						String path = i18nService.getTemplate(
								TEMPLATE, locale);
						String content = Util.processTemplate(model, path);
						String subject = i18nService.getLabel(
								"subject-change-password", locale);
						mailService.send(user.getEmail(), subject, content);
						String json = getJson(user);
						builder = Response.ok(json);
						LOGGER.debug("processed: " + content);
					} catch (Exception e) {
						LOGGER.warn("error processing template", e);
						error = "generic-error";
					}


				}
			} else {
				LOGGER.warn("user not found: " + userName);
				error = "user-not-found";
			}
		} catch (CoolUserFactoryException e) {
			LOGGER.error("unable to change password for user: " + userName);
			error = "user-not-found";
		}

		if (error != null) {
			String json = getError(error);
			builder = Response.serverError().entity(json);
		}

		return builder.build();

	}

	@POST
	@Path(Page.LOGIN_URL)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@Context HttpServletRequest req,
			@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("redirect") String redirect,
			@FormParam("queryString") String queryString) {

		boolean authenticated = cmisAuthenticatorFactory.authenticate(req,
				username, password);

		URI uri;

		if (authenticated) {
			uri = URI.create(redirect + "?" + queryString);
		} else {
			uri = URI.create("../" + Page.LOGIN_URL + "?failure=yes");
		}

		return Response.seeOther(uri).build();

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path(Page.LOGIN_URL)
	public Response loginJson(@Context HttpServletRequest req,
			Credentials credentials) {

		String username = credentials.getUsername();
		boolean authenticated = cmisAuthenticatorFactory.authenticate(req,
				username, credentials.getPassword());

		ResponseBuilder rb;
		if (authenticated) {
			rb = Response.ok();
		} else {
			rb = Response.status(Status.FORBIDDEN).entity(
					"access denied to user " + username);
		}

		return rb.build();

	}


	@GET
	@Path(Page.LOGOUT_URL)
	public Response logout(@Context HttpServletRequest req) {
		req.getSession(false).invalidate();
		URI uri = URI.create("../" + Page.LOGIN_URL);
		return Response.seeOther(uri).build();

	}

	static String getUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		int l = url.indexOf(req.getServletPath());
		return url.substring(0, l);
	}

	@GET
	@Path("confirm-account")
	public Response confirmAccount(@Context HttpServletRequest req, @QueryParam("userid") String userId,
			@QueryParam("pin") String pin) throws URISyntaxException {

		ResponseBuilder rb;

		try {
			CMISUser user = userService.loadUserForConfirm(userId);
			if (pin.equals(user.getPin())) {
				user.setDisableAccount(false);
				user.setPin("");
				user = userService.updateUser(user);
				LOGGER.info("user enabled");
				rb = Response.seeOther(new URI(getUrl(req) + "/login"));
				LOGGER.debug("User created " + user.getFullName());
			} else {
				if (user.getEnabled()) {
					String msg = "User " + user.getUserName() + " already active.";
					LOGGER.warn(msg);
					rb = Response.status(Status.BAD_REQUEST).entity(msg);
				} else {
					LOGGER.warn("user " + userId + ", PIN is not valid");
					rb = Response
							.status(Status.FORBIDDEN)
							.entity("Error: Confirm registration failed! PIN is not valid.");
				}
			}

		} catch (CoolUserFactoryException e) {
			LOGGER.error("Unable to confirm account, user " + userId, e);
			rb = Response.status(Status.UNAUTHORIZED);
		}

		return rb.build();
	}



	private String getError(String error) {
		JsonObject json = new JsonObject();
		json.addProperty("error", error);
		return json.toString();
	}

	private String getJson(CMISUser result) {
		JsonObject json = new JsonObject();
		json.addProperty("fullName", result.getFullName());
		json.addProperty("email", result.getEmail());
		String content = json.toString();
		LOGGER.debug(content);
		return content;
	}

}
