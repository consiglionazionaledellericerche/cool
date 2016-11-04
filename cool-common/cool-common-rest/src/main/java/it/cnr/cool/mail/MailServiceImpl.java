package it.cnr.cool.mail;

import freemarker.template.*;
import it.cnr.cool.mail.model.AttachmentBean;
import it.cnr.cool.mail.model.EmailMessage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.service.I18nService;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;

/**
 *
 * Mail service
 *
 */
public class MailServiceImpl implements MailService, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

	@Autowired
	private I18nService i18nService;

	private String mailFromDefault;

	private String mailToAdmin, mailToErrorMessage, mailToHelpDesk;

	private boolean mailErrorEnabled = true;

	public void setMailErrorEnabled(boolean mailErrorEnabled) {
		this.mailErrorEnabled = mailErrorEnabled;
	}

	class CoolMimeMessagePreparator implements MimeMessagePreparator{
		private final EmailMessage emailMessage;

		public CoolMimeMessagePreparator(EmailMessage emailMessage) {
			super();
			this.emailMessage = emailMessage;
		}

		@Override
		public void prepare(MimeMessage mimeMessage) throws Exception {
			StringBuffer bodyMessage=null;

			// controlli preliminari
			if (emailMessage == null) {
				throw new Exception("MailService: EmailMessage null.");
			}
			if (emailMessage.getRecipients() == null || emailMessage.getRecipients().isEmpty()) {
				throw new Exception("MailService: Destinatario non presente.");
			}
			if (emailMessage.getBody() == null || emailMessage.getBody().length() == 0) {
				if (emailMessage.getTemplateBody() == null)
					throw new Exception("MailService: Corpo non presente.");
				else
					bodyMessage = getBodyFromTemplate(emailMessage);
			} else {
				bodyMessage = emailMessage.getBody();
			}

			emailMessage.setSentDate(new GregorianCalendar().getTime());

			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			String from = emailMessage.getSender() != null ? emailMessage.getSender() : mailFromDefault;
			message.setFrom(from);

			String[] to = emailMessage.getRecipients().toArray(new String[emailMessage.getRecipients().size()]);
			message.setTo(to);

			message.setSubject(emailMessage.getSubject());
			message.setText(bodyMessage.toString(), emailMessage.isHtmlBody());

			if (emailMessage.getCcRecipients()!=null && !emailMessage.getCcRecipients().isEmpty())
				message.setCc(emailMessage.getCcRecipients().toArray(new String[emailMessage.getCcRecipients().size()]));
			if (emailMessage.getBccRecipients()!=null && !emailMessage.getBccRecipients().isEmpty())
				message.setBcc(emailMessage.getBccRecipients().toArray(new String[emailMessage.getBccRecipients().size()]));
			message.setSentDate(emailMessage.getSentDate());
			message.getMimeMessage().setHeader("X-Mailer", "JavaMailer");
			if (emailMessage.getAttachments()!=null && !emailMessage.getAttachments().isEmpty()) {
				for (AttachmentBean attachment : emailMessage.getAttachments())
					message.addAttachment(attachment.getFileName(), new ByteArrayResource(attachment.getAttachmentByte()));
			}
		}
	}

	public void setMailFromDefault(String mailFromDefault) {
		this.mailFromDefault = mailFromDefault;
	}

	public void setMailToAdmin(String mailToAdmin) {
		this.mailToAdmin = mailToAdmin;
	}

	@Override
	public String getMailToHelpDesk() {
		return mailToHelpDesk;
	}

	public void setMailToHelpDesk(String mailToHelpDesk) {
		this.mailToHelpDesk = mailToHelpDesk;
	}

	public void setMailToErrorMessage(String mailToErrorMessage) {
		this.mailToErrorMessage = mailToErrorMessage;
	}

	@Override
	public void send(final String subject, final String text) throws MailException{
		send(null, subject, text);
	}

	@Override
	public void send(final String to, final String subject, final String text) throws MailException{
		EmailMessage message = new EmailMessage();
		message.setSender(mailFromDefault);
		List<String> recipients = Arrays.asList(to != null ? to : mailToAdmin);
		message.setRecipients(recipients);
		message.setSubject(subject);
	    message.setBody(text);
	    send(message);
	}

	@Override
	public void sendErrorMessage(final String currentUser, String subject, String body) throws MailException{
		if (!mailErrorEnabled || mailToErrorMessage == null || mailToErrorMessage.isEmpty())
			return;
		EmailMessage message = new EmailMessage();
		message.setSender(mailFromDefault);
		List<String> mailErrorRecipients = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(mailToErrorMessage, ",");
		while(st.hasMoreTokens())
			mailErrorRecipients.add(st.nextToken());
		message.setRecipients(mailErrorRecipients);
		message.setSubject(subject);
		message.setBody(body);
	    send(message);
	}

	@Override
	@SuppressWarnings("PMD.AvoidThreadGroup")
	public void sendErrorMessage(final String currentUser, String url, String serverPath, final Exception we) throws MailException{
		if (!mailErrorEnabled || mailToErrorMessage == null || mailToErrorMessage.isEmpty())
			return;
		EmailMessage message = new EmailMessage();
		message.setSender(mailFromDefault);
		List<String> mailErrorRecipients = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(mailToErrorMessage, ",");
		while(st.hasMoreTokens())
			mailErrorRecipients.add(st.nextToken());
		message.setRecipients(mailErrorRecipients);
		message.setSubject("Error inside application for user:"+currentUser+" and server: "+ serverPath);
		StringWriter sw = new StringWriter();
		we.printStackTrace(new java.io.PrintWriter(sw));
		StringBuffer sb = new StringBuffer();
		sb.append("Request URL is: "+ url);
		if (we.getCause() instanceof CmisBaseException){
			sb.append("<HR>");
			sb.append(((CmisBaseException)we.getCause()).getErrorContent());
		}
		sb.append("<HR><BR>");
		sb.append(sw.toString());
		message.setBody(sb);
	    send(message);
	}

	@Override
	public void send(EmailMessage emailMessage) throws MailException {
		try{
			mailSender.send(new CoolMimeMessagePreparator(emailMessage));
		} catch (Exception e) {
                        LOGGER.error(emailMessage.getBody().toString());
			LOGGER.error("Errore nell'invio della mail", e);
			throw new MailSendException("Errore nell'invio della mail", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("multipart/mixed;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("application/pdf;; x-java-content-handler=com.sun.mail.handlers.text_html");
	}

	protected HashMap<String, Serializable> addToTemplateModel(HashMap<String, Serializable> templateModel){
		if (templateModel==null) templateModel=new HashMap();
		templateModel.put("message", new EmailMessageMethod());
        return templateModel;
	}

	public class EmailMessageMethod implements TemplateMethodModelEx, Serializable{
		/**
	     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
	     */
	    @Override
		public Object exec(List args) throws TemplateModelException
	    {
	        String result = null;
	        int argSize = args.size();

	        if (argSize != 0)
	        {
	            String id = null;
	            Object arg0 = args.get(0);
	            if (arg0 instanceof TemplateScalarModel)
	            {
	                id = ((TemplateScalarModel)arg0).getAsString();
	            }

	            if (id != null)
	            {
	                if (argSize == 1)
	                {
	                    // shortcut for no additional msg params
						LOGGER.warn("uso default locale ENGLISH");
						result = i18nService.getLabel(id, Locale.ENGLISH);
	                }
	                else
	                {
	                    Object arg;
	                    Object[] params = new Object[argSize - 1];
	                    for (int i = 0; i < argSize-1; i++)
	                    {
	                        // ignore first passed-in arg which is the msg id
	                        arg = args.get(i + 1);
	                        if (arg instanceof TemplateScalarModel)
	                        {
	                            params[i] = ((TemplateScalarModel)arg).getAsString();
	                        }
	                        else if (arg instanceof TemplateNumberModel)
	                        {
	                            params[i] = ((TemplateNumberModel)arg).getAsNumber();
	                        }
	                        else if (arg instanceof TemplateDateModel)
	                        {
	                            params[i] = ((TemplateDateModel)arg).getAsDate();
	                        }
	                        else
	                        {
	                            params[i] = "";
	                        }
	                    }

						LOGGER.warn("ignoro parametri " + params);
						LOGGER.warn("uso default locale ENGLISH");
						result = i18nService.getLabel(id, Locale.ENGLISH);
	                }
	            }
	        }

	        return (result != null ? result : "");
	    }
	}

	private StringBuffer getBodyFromTemplate(EmailMessage message) {
		final StringWriter htmlWriter = new StringWriter();
		if (message != null && message.getTemplateBody() != null) {
			String templateBody = message.getTemplateBody().toString();
			HashMap<String, Serializable> templateModel = message.getTemplateModel();
			HashMap<String, Serializable> model = addToTemplateModel(templateModel);

			InputStream is = new ByteArrayInputStream(templateBody.getBytes());

			try {
				Template t = Util.getTemplate("mailTemplate", is);

				Map<String, Object> tempMap = new HashedMap();
				tempMap.putAll(model);

				String content = Util.processTemplate(tempMap, t);
				InputStream contentStream = IOUtils.toInputStream(content);
				IOUtils.copy(contentStream, htmlWriter);
			} catch (TemplateException e) {
				LOGGER.error("unable to process template", e);
			} catch (IOException e) {
				LOGGER.error("unable to process template", e);
			}

		}
		return htmlWriter.getBuffer();
	}
}
