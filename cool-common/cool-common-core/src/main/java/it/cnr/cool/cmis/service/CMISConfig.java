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

package it.cnr.cool.cmis.service;

import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CMISConfig implements InitializingBean {
    public static final String GUEST_USERNAME = "user.guest.username";
    public static final String GUEST_PASSWORD = "user.guest.password";
    public static final String ADMIN_USERNAME = "user.admin.username";
    public static final String ADMIN_PASSWORD = "user.admin.password";

    @Autowired
    private Environment env;

    private Map<String, String> serverParameters = new HashMap<>();

    public Map<String, String> getServerParameters() {
        return serverParameters;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        serverParameters = Arrays.asList(CMISSessionParameter.values())
                .stream()
                .filter(cmisSessionParameter -> env.containsProperty(cmisSessionParameter.value()))
                .collect(Collectors.toMap(
                        cmisSessionParameter -> cmisSessionParameter.value,
                        cmisSessionParameter -> env.getProperty(cmisSessionParameter.value)
                ));
    }

    @Bean("cmisDefaultOperationContext")
    public OperationContext getCmisDefaultOperationContext() {
        OperationContext operationContext = new OperationContextImpl();
        operationContext.setMaxItemsPerPage(10);
        operationContext.setCacheEnabled(false);
        return operationContext;
    }

    public enum CMISSessionParameter {
        GUEST_USERNAME("user.guest.username"),
        GUEST_PASSWORD("user.guest.password"),
        ADMIN_USERNAME("user.admin.username"),
        ADMIN_PASSWORD("user.admin.password"),

        BINDING_TYPE("org.apache.chemistry.opencmis.binding.spi.type"),
        BINDING_SPI_CLASS("org.apache.chemistry.opencmis.binding.spi.classname"),
        FORCE_CMIS_VERSION("org.apache.chemistry.opencmis.cmisversion"),
        ATOMPUB_URL("org.apache.chemistry.opencmis.binding.atompub.url"),
        WEBSERVICES_REPOSITORY_SERVICE("org.apache.chemistry.opencmis.binding.webservices.RepositoryService"),
        WEBSERVICES_NAVIGATION_SERVICE("org.apache.chemistry.opencmis.binding.webservices.NavigationService"),
        WEBSERVICES_OBJECT_SERVICE("org.apache.chemistry.opencmis.binding.webservices.ObjectService"),
        WEBSERVICES_VERSIONING_SERVICE("org.apache.chemistry.opencmis.binding.webservices.VersioningService"),
        WEBSERVICES_DISCOVERY_SERVICE("org.apache.chemistry.opencmis.binding.webservices.DiscoveryService"),
        WEBSERVICES_RELATIONSHIP_SERVICE("org.apache.chemistry.opencmis.binding.webservices.RelationshipService"),
        WEBSERVICES_MULTIFILING_SERVICE("org.apache.chemistry.opencmis.binding.webservices.MultiFilingService"),
        WEBSERVICES_POLICY_SERVICE("org.apache.chemistry.opencmis.binding.webservices.PolicyService"),
        WEBSERVICES_ACL_SERVICE("org.apache.chemistry.opencmis.binding.webservices.ACLService"),
        WEBSERVICES_REPOSITORY_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.RepositoryService.endpoint"),
        WEBSERVICES_NAVIGATION_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.NavigationService.endpoint"),
        WEBSERVICES_OBJECT_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.ObjectService.endpoint"),
        WEBSERVICES_VERSIONING_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.VersioningService.endpoint"),
        WEBSERVICES_DISCOVERY_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.DiscoveryService.endpoint"),
        WEBSERVICES_RELATIONSHIP_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.RelationshipService.endpoint"),
        WEBSERVICES_MULTIFILING_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.MultiFilingService.endpoint"),
        WEBSERVICES_POLICY_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.PolicyService.endpoint"),
        WEBSERVICES_ACL_SERVICE_ENDPOINT("org.apache.chemistry.opencmis.binding.webservices.ACLService.endpoint"),
        WEBSERVICES_MEMORY_THRESHOLD("org.apache.chemistry.opencmis.binding.webservices.memoryThreshold"),
        WEBSERVICES_REPSONSE_MEMORY_THRESHOLD("org.apache.chemistry.opencmis.binding.webservices.responseMemoryThreshold"),
        WEBSERVICES_TEMP_DIRECTORY("org.apache.chemistry.opencmis.binding.webservices.tempDirectory"),
        WEBSERVICES_TEMP_ENCRYPT("org.apache.chemistry.opencmis.binding.webservices.tempEncrypt"),
        WEBSERVICES_PORT_PROVIDER_CLASS("org.apache.chemistry.opencmis.binding.webservices.portprovider.classname"),
        WEBSERVICES_JAXWS_IMPL("org.apache.chemistry.opencmis.binding.webservices.jaxws.impl"),
        BROWSER_URL("org.apache.chemistry.opencmis.binding.browser.url"),
        BROWSER_SUCCINCT("org.apache.chemistry.opencmis.binding.browser.succinct"),
        BROWSER_DATETIME_FORMAT("org.apache.chemistry.opencmis.binding.browser.datetimeformat"),
        LOCAL_FACTORY("org.apache.chemistry.opencmis.binding.local.classname"),
        AUTHENTICATION_PROVIDER_CLASS("org.apache.chemistry.opencmis.binding.auth.classname"),
        AUTH_HTTP_BASIC("org.apache.chemistry.opencmis.binding.auth.http.basic"),
        AUTH_HTTP_BASIC_CHARSET("org.apache.chemistry.opencmis.binding.auth.http.basic.charset"),
        AUTH_OAUTH_BEARER("org.apache.chemistry.opencmis.binding.auth.http.oauth.bearer"),
        AUTH_SOAP_USERNAMETOKEN("org.apache.chemistry.opencmis.binding.auth.soap.usernametoken"),
        OAUTH_CLIENT_ID("org.apache.chemistry.opencmis.oauth.clientId"),
        OAUTH_CLIENT_SECRET("org.apache.chemistry.opencmis.oauth.clientSecret"),
        OAUTH_CODE("org.apache.chemistry.opencmis.oauth.code"),
        OAUTH_TOKEN_ENDPOINT("org.apache.chemistry.opencmis.oauth.tokenEndpoint"),
        OAUTH_REDIRECT_URI("org.apache.chemistry.opencmis.oauth.redirectUri"),
        OAUTH_ACCESS_TOKEN("org.apache.chemistry.opencmis.oauth.accessToken"),
        OAUTH_REFRESH_TOKEN("org.apache.chemistry.opencmis.oauth.refreshToken"),
        OAUTH_EXPIRATION_TIMESTAMP("org.apache.chemistry.opencmis.oauth.expirationTimestamp"),
        OAUTH_DEFAULT_TOKEN_LIFETIME("org.apache.chemistry.opencmis.oauth.defaultTokenLifetime"),
        CLIENT_CERT_KEYFILE("org.apache.chemistry.opencmis.clientcerts.keyfile"),
        CLIENT_CERT_PASSPHRASE("org.apache.chemistry.opencmis.clientcerts.passphrase"),
        HTTP_INVOKER_CLASS("org.apache.chemistry.opencmis.binding.httpinvoker.classname"),
        COMPRESSION("org.apache.chemistry.opencmis.binding.compression"),
        CLIENT_COMPRESSION("org.apache.chemistry.opencmis.binding.clientcompression"),
        COOKIES("org.apache.chemistry.opencmis.binding.cookies"),
        HEADER("org.apache.chemistry.opencmis.binding.header"),
        CONNECT_TIMEOUT("org.apache.chemistry.opencmis.binding.connecttimeout"),
        READ_TIMEOUT("org.apache.chemistry.opencmis.binding.readtimeout"),
        PROXY_USER("org.apache.chemistry.opencmis.binding.proxyuser"),
        PROXY_PASSWORD("org.apache.chemistry.opencmis.binding.proxypassword"),
        CSRF_HEADER("org.apache.chemistry.opencmis.binding.csrfheader"),
        USER_AGENT("org.apache.chemistry.opencmis.binding.useragent"),
        CACHE_SIZE_OBJECTS("org.apache.chemistry.opencmis.cache.objects.size"),
        CACHE_TTL_OBJECTS("org.apache.chemistry.opencmis.cache.objects.ttl"),
        CACHE_SIZE_PATHTOID("org.apache.chemistry.opencmis.cache.pathtoid.size"),
        CACHE_TTL_PATHTOID("org.apache.chemistry.opencmis.cache.pathtoid.ttl"),
        CACHE_PATH_OMIT("org.apache.chemistry.opencmis.cache.path.omit"),
        CACHE_SIZE_REPOSITORIES("org.apache.chemistry.opencmis.binding.cache.repositories.size"),
        CACHE_SIZE_TYPES("org.apache.chemistry.opencmis.binding.cache.types.size"),
        CACHE_SIZE_LINKS("org.apache.chemistry.opencmis.binding.cache.links.size"),
        LOCALE_ISO639_LANGUAGE("org.apache.chemistry.opencmis.locale.iso639"),
        LOCALE_ISO3166_COUNTRY("org.apache.chemistry.opencmis.locale.iso3166"),
        LOCALE_VARIANT("org.apache.chemistry.opencmis.locale.variant"),
        OBJECT_FACTORY_CLASS("org.apache.chemistry.opencmis.objectfactory.classname"),
        CACHE_CLASS("org.apache.chemistry.opencmis.cache.classname"),
        TYPE_DEFINITION_CACHE_CLASS("org.apache.chemistry.opencmis.cache.types.classname"),
        REPOSITORY_ID("org.apache.chemistry.opencmis.session.repository.id"),
        INCLUDE_OBJECTID_URL_PARAM_ON_CHECKOUT("org.apache.chemistry.opencmis.workaround.includeObjectIdOnCheckout"),
        INCLUDE_OBJECTID_URL_PARAM_ON_MOVE("org.apache.chemistry.opencmis.workaround.includeObjectIdOnMove"),
        OMIT_CHANGE_TOKENS("org.apache.chemistry.opencmis.workaround.omitChangeTokens"),
        ADD_NAME_ON_CHECK_IN("org.apache.chemistry.opencmis.workaround.addNameOnCheckIn"),
        LATEST_VERSION_WITH_VERSION_SERIES_ID("org.apache.chemistry.opencmis.workaround.getLatestVersionWithVersionSeriesId");

        private final String value;

        CMISSessionParameter(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

    }
}
