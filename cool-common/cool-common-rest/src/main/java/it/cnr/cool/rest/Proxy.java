package it.cnr.cool.rest;


import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.interceptor.ProxyInterceptor;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.net.SocketException;
import java.util.Arrays;


@Path("proxy")
@Component
public class Proxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

	@Autowired
	private CMISService cmisService;

	private ProxyInterceptor proxyInterceptor;

	// TODO: try-catchare tutto

	@GET
	public void get(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {

		BindingSession currentBindingSession = cmisService
				.getCurrentBindingSession(req);
		UrlBuilder url = getUrl(req);

		Response resp = CmisBindingsHelper
				.getHttpInvoker(currentBindingSession).invokeGET(url,
						currentBindingSession);

		int responseCode = resp.getResponseCode();
		res.setStatus(responseCode);
		res.setContentType(resp.getContentTypeHeader());
		res.setCharacterEncoding(resp.getCharset().toUpperCase());

		OutputStream outputStream = res.getOutputStream();
		if (responseCode == Status.OK.getStatusCode()) {
			IOUtils.copy(resp.getStream(), outputStream);
		} else {
			IOUtils.write("error " + responseCode, outputStream);
		}

		outputStream.flush();


	}


	@POST
	public void post(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {
		processRequest(req, res, true);

	}

	@PUT
	public void put(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {
		processRequest(req, res, false);

	}

	/**
	 *
	 * Process POST or PUT request
	 *
	 * @param req
	 * @param res
	 * @param isPost
	 * @return
	 * @throws IOException
	 */
	private void processRequest(HttpServletRequest req,
			HttpServletResponse res, boolean isPost) throws IOException {

		BindingSession currentBindingSession = cmisService
				.getCurrentBindingSession(req);
		UrlBuilder url = getUrl(req);
		String urlParam = getUrlParam(req);

		final InputStream is = req.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileCopyUtils.copy(is, baos);
		final InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
		final InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
		final InputStream is3 = new ByteArrayInputStream(baos.toByteArray());
		proxyInterceptor.invokeBeforePost(urlParam, req, is1);
		Output writer = new Output() {
			@Override
			public void write(OutputStream out) throws Exception {
				FileCopyUtils.copy(is2, out);
			}
		};

		Response resp;

		if (isPost) {
			resp = CmisBindingsHelper.getHttpInvoker(currentBindingSession).invokePOST(url, req.getContentType(), writer, currentBindingSession);
		} else {
			resp = CmisBindingsHelper.getHttpInvoker(currentBindingSession).invokePUT(url, req.getContentType(), null, writer, currentBindingSession);
		}
		proxyInterceptor.invokeAfterPost(urlParam, req, is3, resp);

		process(res, resp);


	}




	@DELETE
	public void delete(@Context HttpServletRequest req,
			@Context HttpServletResponse res) throws IOException {

		BindingSession currentBindingSession = cmisService.getCurrentBindingSession(req);
		UrlBuilder url = getUrl(req);

		Response resp = CmisBindingsHelper
				.getHttpInvoker(currentBindingSession).invokeDELETE(url,
						currentBindingSession);

		res.setStatus(resp.getResponseCode());
		res.setContentType(resp.getContentTypeHeader());
		res.setCharacterEncoding(resp.getCharset().toUpperCase());

		if (resp.getResponseCode() == Status.OK.getStatusCode()) {
			OutputStream outputStream = res.getOutputStream();
			IOUtils.copy(resp.getStream(), outputStream);
			outputStream.flush();
		} else {
			LOGGER.info("DELETE failed with code " + resp.getResponseCode());
		}


	}

	// utility methods

	private UrlBuilder getUrl(HttpServletRequest req) {

		String urlParam = getUrlParam(req);
		String link = cmisService.getBaseURL().concat(urlParam);
		if (req.getQueryString() != null)
			link = link.concat("?").concat(req.getQueryString());
		return new UrlBuilder(link);
	}

	private String getUrlParam(HttpServletRequest req) {

		String urlParam = null;
		if (req.getParameter("url") != null) {
			urlParam = it.cnr.cool.util.UriUtils.encode(req.getParameter("url"));
			//urlParam = req.getParameter("url");
		}
		else
			urlParam = it.cnr.cool.util.UriUtils.encode(req.getPathInfo()
					.replaceFirst(
					"/[a-zA-Z\\-]*/", ""));

		return urlParam;

	}

	private void process(HttpServletResponse res, Response outcome)
			throws IOException {

		ServletOutputStream outputStream = res.getOutputStream();

		int status = outcome.getResponseCode();
		if (status != Status.OK.getStatusCode()) {
			res.setStatus(status);
			IOUtils.copy(new ByteArrayInputStream(outcome.getErrorContent()
					.getBytes()), outputStream);
			outputStream.flush();
			return;
		}
		InputStream result = outcome.getStream();
		try {
			for (String header : Arrays.asList("Date", "Cache-Control")) {
				String value = outcome.getHeader(header);
				if (value != null && !value.trim().isEmpty()) {
					res.setHeader(header, value);
				}
			}

			res.setContentType(outcome.getContentTypeHeader());
			res.setCharacterEncoding(outcome.getCharset().toUpperCase());
			IOUtils.copy(result, outputStream);
		} catch (SocketException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Client aborted stream read:\n\tcontent: "
						+ e1.getMessage());
		} catch (IOException e) {

			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			IOUtils.copy(new ByteArrayInputStream(e.getMessage().getBytes()),
					outputStream);

		}

		outputStream.flush();
	}

	public void setProxyInterceptor(ProxyInterceptor proxyInterceptor) {
		this.proxyInterceptor = proxyInterceptor;
	}
}
