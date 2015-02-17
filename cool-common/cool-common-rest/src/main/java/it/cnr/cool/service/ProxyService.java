package it.cnr.cool.service;

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
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by francesco on 17/02/15.
 */

@Component
public class ProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyService.class);

    @Autowired
    private CMISService cmisService;


    private Map<String, String> backends;

    @Autowired
    private ProxyInterceptor proxyInterceptor;


    private final static String [] ALLOWED_HEADERS = {"WWW-Authenticate", "Cache-Control"};

    /**
     *
     * Process POST or PUT request
     *
     * @param req
     * @param res
     * @param isPost
     * @return
     * @throws java.io.IOException
     */
    public void processRequest(HttpServletRequest req,
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


    public void processDelete (HttpServletRequest req, HttpServletResponse res) throws IOException {

        BindingSession currentBindingSession = cmisService.getCurrentBindingSession(req);
        UrlBuilder url = getUrl(req);

        Response resp = CmisBindingsHelper
                .getHttpInvoker(currentBindingSession).invokeDELETE(url,
                        currentBindingSession);

        res.setStatus(resp.getResponseCode());
        res.setContentType(resp.getContentTypeHeader());
        res.setCharacterEncoding(resp.getCharset().toUpperCase());

        if (resp.getResponseCode() == javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
            OutputStream outputStream = res.getOutputStream();
            IOUtils.copy(resp.getStream(), outputStream);
            outputStream.flush();
        } else {
            LOGGER.info("DELETE failed with code " + resp.getResponseCode());
        }

    }


    public void processGet (HttpServletRequest req, String backend, HttpServletResponse res) throws IOException {

        UrlBuilder url = getUrl(req, backend);

        String authorization = req.getHeader("Authorization");

        BindingSession currentBindingSession = createBindingSessionForBasicAuthentication(authorization);

        if (currentBindingSession == null) {
            LOGGER.info("no basic auth provided, using current binding session");
            currentBindingSession = cmisService.getCurrentBindingSession(req);
        } else {
            LOGGER.info("basic auth provided");
        }

        Response resp = CmisBindingsHelper
                .getHttpInvoker(currentBindingSession).invokeGET(url,
                        currentBindingSession);

        int responseCode = resp.getResponseCode();

        for (Map.Entry<String, List<String>> header : resp.getHeaders().entrySet()) {

            if (header.getValue().size() == 1) {
                String key = header.getKey();
                if (key != null && !key.isEmpty() && isHeaderToBeAdded(key)) {
                    String value = header.getValue().get(0);
                    LOGGER.debug("header {} = {}", key, value);
                    res.setHeader(key, value);
                }
            } else {
                LOGGER.error("error with headers: " + url.toString());
            }

        }


        res.setStatus(responseCode);
        res.setContentType(resp.getContentTypeHeader());
        res.setCharacterEncoding(resp.getCharset().toUpperCase());

        OutputStream outputStream = res.getOutputStream();

        if (resp != null && resp.getStream() != null) {

            if (responseCode != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                LOGGER.error("status code {} for request url", responseCode);

            }

            IOUtils.copy(resp.getStream(), outputStream);
        }

        outputStream.flush();

    }


    // utility methods

    private UrlBuilder getUrl(HttpServletRequest req) {
        return getUrl(req, null);
    }

    private UrlBuilder getUrl(HttpServletRequest req, String backend) {

        String urlParam = getUrlParam(req);

        String baseURL = null;
        if (backend != null && backends.containsKey(backend)) {

            baseURL = backends.get(backend);
            LOGGER.debug("using custom backend: " + backend + " " + baseURL);

        } else {
            baseURL = cmisService.getBaseURL();
        }

        String link = baseURL.concat(urlParam);
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
        if (status != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
            res.setStatus(status);

            if (outcome != null && outcome.getErrorContent() != null) {
                IOUtils.copy(new ByteArrayInputStream(outcome.getErrorContent()
                        .getBytes()), outputStream);
            }

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


    public void setBackends(Map<String, String> backends) {
        this.backends = backends;
    }


    private BindingSession createBindingSessionForBasicAuthentication(String authorization) {

        if (authorization == null || authorization.isEmpty()) {
            LOGGER.debug("no authorization header provided");
            return null;
        }

        String usernameAndPasswordBase64 = authorization.split(" ")[1];

        byte[] usernameAndPasswordByteArray = DatatypeConverter.parseBase64Binary(usernameAndPasswordBase64);

        String [] usernameAndPassword = new String(usernameAndPasswordByteArray).split(":");

        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        LOGGER.info("using BASIC auth for user: " + username);

        return cmisService.createBindingSession(username, password);

    }


    private boolean isHeaderToBeAdded(String key) {

        for (String allowed : ALLOWED_HEADERS) {

            if (key.equalsIgnoreCase(allowed)) {
                LOGGER.debug("setting HTTP header " + key);
                return true;
            }

        }

        LOGGER.debug("ignoring HTTP header " + key);

        return false;
    }


}
