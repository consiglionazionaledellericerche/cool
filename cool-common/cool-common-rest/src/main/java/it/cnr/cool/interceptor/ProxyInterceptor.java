package it.cnr.cool.interceptor;

import it.cnr.cool.cmis.service.CMISService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;

public class ProxyInterceptor {
	/** A list containing all the processor extenstions */
    protected Map<String, ProxyInterceptor> interceptorExtensions = new HashMap<String, ProxyInterceptor>();
    protected CMISService cmisService;
    private ProxyInterceptor interceptor;
    private String path;
    
    public void register(){
    	interceptor.interceptorExtensions.put(path, this);
    }
    
    public void setInterceptor(ProxyInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	public void setCmisService(CMISService cmisService) {
		this.cmisService = cmisService;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public void invokeBeforePost(String url, HttpServletRequest req, InputStream content) {
		for (String path : interceptorExtensions.keySet()) {
			if (url.startsWith(path)) {
				interceptorExtensions.get(path).invokeBeforePost(url, req, content);
			}
		}
	}

	public void invokeAfterPost(String url, HttpServletRequest req, InputStream content, Response resp) {
		for (String path : interceptorExtensions.keySet()) {
			if (url.startsWith(path)) {
				interceptorExtensions.get(path).invokeAfterPost(url, req, content, resp);
			}
		}
	}
	
}
