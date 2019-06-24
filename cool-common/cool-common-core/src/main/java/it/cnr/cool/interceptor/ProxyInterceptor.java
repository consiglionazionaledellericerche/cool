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

package it.cnr.cool.interceptor;

import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
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
