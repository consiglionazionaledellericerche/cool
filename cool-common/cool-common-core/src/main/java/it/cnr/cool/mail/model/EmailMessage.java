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

package it.cnr.cool.mail.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Astrazione di un messaggio di posta elettronica
 * 
 */
public class EmailMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String sender;
	private String subject;
	private StringBuffer body;
	private String templateBody;
	private HashMap<String, Serializable> templateModel;
	private List<String> recipients;
	private List<String> ccRecipients;
	private List<String> bccRecipients;
	private Date sentDate;
	private List<AttachmentBean> attachments;
	private boolean htmlBody;

	public EmailMessage() {
		setHtmlBody(true);
	}
	
	public List<AttachmentBean> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<AttachmentBean> attachments) {
		this.attachments = attachments;
	}
	public Date getSentDate() {
		return sentDate;
	}
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}
	public List<String> getBccRecipients() {
		return bccRecipients;
	}
	public void setBccRecipients(List<String> bccRecipients) {
		this.bccRecipients = bccRecipients;
	}
	public StringBuffer getBody() {
		if (body == null) body = new StringBuffer();
		return body;
	}
	public void setBody(StringBuffer body) {
		this.body = body;
	}
	public void setBody(String body) {
		this.body = new StringBuffer(body);
	}
	public List<String> getCcRecipients() {
		return ccRecipients;
	}
	public void setCcRecipients(List<String> ccRecipients) {
		this.ccRecipients = ccRecipients;
	}
	public List<String> getRecipients() {
		return recipients;
	}
	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void addRecipient(String address) {
		if (recipients == null) recipients = new ArrayList<String>();
		recipients.add(address);
	}
	public void addCcRecipient(String address) {
		if (ccRecipients == null) ccRecipients = new ArrayList<String>();
		ccRecipients.add(address);
	}
	public void addBccRecipient(String address) {
		if (bccRecipients == null) bccRecipients = new ArrayList<String>();
		bccRecipients.add(address);
	}
	public void addAttachment(AttachmentBean attachment) {
		if (attachments == null) attachments = new ArrayList<AttachmentBean>();
		attachments.add(attachment);
	}
	private List<String> testRecipients;
	
	public List<String> getTestRecipients() {
		return testRecipients;
	}
	public void setTestRecipients(List<String> testRecipients) {
		this.testRecipients = testRecipients;
	}
	public String getTemplateBody() {
		return templateBody;
	}
	public void setTemplateBody(String templateBody) {
		this.templateBody = templateBody;
	}
	public HashMap<String, Serializable> getTemplateModel() {
		return templateModel;
	}
	public void setTemplateModel(HashMap<String, Serializable> templateModel) {
		this.templateModel = templateModel;
	}
	public boolean isHtmlBody() {
		return htmlBody;
	}
	public void setHtmlBody(boolean htmlBody) {
		this.htmlBody = htmlBody;
	}
}
