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

package it.cnr.cool.service.frontOffice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.exception.CoolClientException;
import it.cnr.cool.frontOfficeHandler.AlfrescoHandler;
import it.cnr.cool.frontOfficeHandler.ErrorCode;
import it.cnr.cool.frontOfficeHandler.Log4jHandler;
import it.cnr.cool.service.util.AlfrescoDocument;
import it.cnr.cool.service.util.Faq;
import it.cnr.cool.service.util.Log;
import it.cnr.cool.service.util.Notice;
import it.cnr.si.opencmis.criteria.Criteria;
import it.cnr.si.opencmis.criteria.CriteriaFactory;
import it.cnr.si.opencmis.criteria.Order;
import it.cnr.si.opencmis.criteria.restrictions.Restrictions;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * FrontOffice service
 */
@Service
public class FrontOfficeService implements InitializingBean {

    private final static String USER_AGENT = "user-agent";
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontOfficeService.class);
    private final static SimpleDateFormat FORMATTER_ALFRESCO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final static SimpleDateFormat SDF = new SimpleDateFormat(
            "dd/MM/yyyy – HH:mm:ss");
    @Autowired
    private OperationContext cmisDefaultOperationContext;

    private OperationContext frontOfficeOperationContext;
    @Autowired
    private AlfrescoHandler alfrescoHandler;
    @Autowired
    private Log4jHandler log4jHandler;

    /**
     * get now
     *
     * @return now
     */
    public static String getNowUTC() {
        Calendar ora = Calendar.getInstance();
        return FORMATTER_ALFRESCO.format(ora.getTime());
    }

    public void afterPropertiesSet() {
        frontOfficeOperationContext = new OperationContextImpl(cmisDefaultOperationContext);
        frontOfficeOperationContext.setIncludeAllowableActions(false);
        //filtri sui campi utilizzati nelle restriction ==> i nuovi campi introdotti vanno aggiunti anche qui
        frontOfficeOperationContext.setFilter(new HashSet<String>(Arrays.asList(PropertyIds.NAME, PropertyIds.OBJECT_ID, PropertyIds.CREATION_DATE, PropertyIds.LAST_MODIFIED_BY,
                CoolPropertyIds.NOTICE_TITLE.value(), CoolPropertyIds.NOTICE_STYLE.value(), CoolPropertyIds.NOTICE_TEXT.value(), CoolPropertyIds.NOTICE_NUMBER.value(), CoolPropertyIds.NOTICE_DATA.value(), CoolPropertyIds.NOTICE_SCADENZA.value(),
                CoolPropertyIds.LOGGER_TYPE.value(), CoolPropertyIds.LOGGER_USER.value(), CoolPropertyIds.LOGGER_APPLICATION.value(), CoolPropertyIds.LOGGER_CODE.value(),
                CoolPropertyIds.FAQ_ANSWER.value(), CoolPropertyIds.FAQ_DATA.value(), CoolPropertyIds.FAQ_QUESTION.value(), CoolPropertyIds.FAQ_NUMBER.value(), CoolPropertyIds.FAQ_SHOW.value())));

        FORMATTER_ALFRESCO.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Map<String, Object> getNotice(Session cmisSession, Session adminSession, String after, String before, boolean editor, String typeBando) {
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            ItemIterable<QueryResult> queryResult;
            queryResult = executeQueryNotice(cmisSession, typeBando, after, before, editor);

            List<AlfrescoDocument> result = new ArrayList<AlfrescoDocument>();
            for (QueryResult qr : queryResult.getPage()) {
                ObjectId objId = new ObjectIdImpl(
                        qr.getPropertyValueById(PropertyIds.OBJECT_ID));
                result.add(new Notice(qr, null));
            }
            model.put("docs", result);
            if (editor)
                model.put("maxNotice", alfrescoHandler.getMax(CoolPropertyIds.NOTICE_QUERY_NAME.value(), CoolPropertyIds.NOTICE_NUMBER.value()));
        } catch (CmisUnauthorizedException| CmisPermissionDeniedException e) {
            LOGGER.error("cmis unauthorized exception", e);
        }
        return model;
    }

    public Map<String, Object> getFaq(Session cmisSession, String after, String before, String typeBando, boolean editor, String filterAnswer) {
        Map<String, Object> model = new HashMap<String, Object>();
        ItemIterable<QueryResult> queryResult = executeQueryFaq(cmisSession, typeBando, after, before, editor, filterAnswer);

        List<AlfrescoDocument> result = new ArrayList<AlfrescoDocument>();
        for (QueryResult qr : queryResult.getPage()) {
            result.add(new Faq(qr));
        }
        model.put("docs", result);
        if (editor)
            model.put("maxFaq", alfrescoHandler.getMax(CoolPropertyIds.FAQ_QUERY_NAME.value(), CoolPropertyIds.FAQ_NUMBER.value()));

        return model;
    }

    public List<AlfrescoDocument> getLog(Session cmisSession, String after,
                                         String before, String application, String typeLog, String userLog) {
        ItemIterable<QueryResult> queryResult;
        queryResult = executeQueryLog(typeLog, after, before, cmisSession, application, userLog);

        List<AlfrescoDocument> result = new ArrayList<AlfrescoDocument>();
        for (QueryResult qr : queryResult.getPage()) {
            result.add(new Log(qr));
        }
        return result;
    }

    public Map<String, Object> post(String ip, String userAgent, HashMap<String, String> requestHeader, HashMap<String, String> requestParameter, TypeDocument typeDocument, String stackTrace) {
        Map<String, Object> model = new HashMap<String, Object>();
        String objectId = null;
        try {
            JsonObject item = new JsonParser().parse(stackTrace).getAsJsonObject();
            switch (typeDocument) {
                case Notice:
                    objectId = alfrescoHandler.write(item.toString(), TypeDocument.Notice);
                    break;
                case Log:
                    GregorianCalendar gc = new GregorianCalendar();
                    item.addProperty("Date", SDF.format(gc.getTime()));
                    item.addProperty(USER_AGENT, userAgent);
                    item.addProperty("IP", ip);


                    //scrivo dolo sulla consolle un logger.error
                    if (ErrorCode.fromValue(item.get("codice").getAsInt()).isAlfrescoWrite())
                        log4jHandler.write(item.toString(), TypeDocument.Log);

                    break;
                case Faq:
                    objectId = alfrescoHandler.write(item.toString(), TypeDocument.Faq);
                    break;
            }
            // campo che serve per i test
            if (objectId != null) {
                model.put("objectId", objectId);
            }
        } catch (JsonParseException e) {
            LOGGER.warn("json exception", getErrorItem(requestHeader, requestParameter, userAgent, stackTrace, ip), e);
        } catch (NullPointerException e) {
            LOGGER.warn(NullPointerException.class.getSimpleName(), getErrorItem(requestHeader, requestParameter, userAgent, stackTrace, ip), e);
        } catch (Exception e) {
            throw new CoolClientException("stackTrace: " + stackTrace, e);
        }
        return model;
    }

    public void deleteSingleNode(Session cmisSession, String nodeRefToDelete) {
        Document doc = (Document) cmisSession.getObject(nodeRefToDelete);
        doc.delete(true);
    }

    // riempe il json da restituire in caso d'errore
    private JsonObject getErrorItem(Map<String, String> requestHeaders, Map<String, String> requestParameter, String userAgent, String stackTrace, String ip) {
        JsonObject errorItem = new JsonObject();
        errorItem.addProperty(USER_AGENT, userAgent);
        errorItem.addProperty("IP", ip);
        errorItem.addProperty("stackTrace", stackTrace);
        // request headers
        JsonObject headers = new JsonObject();
        for (String header : requestHeaders.keySet()) {
            headers.addProperty(header, requestHeaders.get(header));
        }
        errorItem.add("headers", headers);
        JsonObject parameters = new JsonObject();
        for (String param : requestParameter.keySet()) {
            parameters.addProperty(param, requestParameter.get(param));
        }
        errorItem.add("parameters", parameters);
        return errorItem;
    }

    private ItemIterable<QueryResult> executeQueryNotice(Session cmisSession, String typeBando, String after, String before, Boolean editor) {
        Calendar startDate = null;
        Calendar endDate = null;
        // usati nelle query con i campi  avvisi:data/faq:data (il widget passa ad Alfresco le 12:00 UTC (14 italiane) per il loro salvataggio ==> hanno tutti la stessa ora settata)
        if (after != null) {
            startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startDate.setTimeInMillis(Long.parseLong(after));
            startDate.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (before != null) {
            endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endDate.setTimeInMillis(Long.parseLong(before));
            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
        }
        frontOfficeOperationContext.setMaxItemsPerPage(Integer.MAX_VALUE);
        Criteria criteria = CriteriaFactory.createCriteria(CoolPropertyIds.NOTICE_QUERY_NAME.value());

        if (typeBando != null)
            criteria.add(Restrictions.eq(CoolPropertyIds.NOTICE_TYPE.value(), typeBando));
        if (startDate != null && endDate != null)
            criteria.add(Restrictions.between(CoolPropertyIds.NOTICE_DATA.value(), startDate.getTime(), endDate.getTime(), false, true, false));

        criteria.addOrder(Order.desc(CoolPropertyIds.NOTICE_NUMBER.value()));
        criteria.addOrder(Order.desc(CoolPropertyIds.NOTICE_DATA.value()));

        // con editor=false prende solo gli avvisi ancora validi(notice:dataScadenza >= oggi && notice:data(data di pubblicazione)<= oggi) altrimenti prende tutti gli avvisi
        if (!editor) {
            criteria.add(Restrictions.ge(CoolPropertyIds.NOTICE_SCADENZA.value(), getNowUTC()));
            criteria.add(Restrictions.le(CoolPropertyIds.NOTICE_DATA.value(), getNowUTC()));
        }
        return criteria.executeQuery(cmisSession, false, frontOfficeOperationContext);
    }

    private ItemIterable<QueryResult> executeQueryFaq(Session cmisSession, String typeBando, String after, String before, Boolean editor, String filterAnswer) {
        Calendar startDate = null;
        Calendar endDate = null;
        // usati nelle query con i campi  avvisi:data/faq:data (il widget passa ad Alfresco le 12:00 UTC (14 italiane) per il loro salvataggio ==> hanno tutti la stessa ora settata)
        if (after != null) {
            startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startDate.setTimeInMillis(Long.parseLong(after));
            startDate.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (before != null) {
            endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endDate.setTimeInMillis(Long.parseLong(before));
            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
        }
        frontOfficeOperationContext.setMaxItemsPerPage(Integer.MAX_VALUE);
        Criteria criteria = CriteriaFactory.createCriteria(CoolPropertyIds.FAQ_QUERY_NAME.value());

        if (filterAnswer != null)
            criteria.add(Restrictions.contains(CoolPropertyIds.FAQ_QUESTION.value(), filterAnswer));
        if (startDate != null && endDate != null)
            criteria.add(Restrictions.between(CoolPropertyIds.FAQ_DATA.value(), startDate.getTime(), endDate.getTime(), false, true, false));
        if (typeBando != null)
            criteria.add(Restrictions.eq(CoolPropertyIds.FAQ_TYPE.value(), typeBando));

        criteria.addOrder(Order.desc(CoolPropertyIds.FAQ_NUMBER.value()));
        criteria.addOrder(Order.desc(CoolPropertyIds.FAQ_DATA.value()));

        //con editor=FALSE prende solo le Faq da pubblicare (dataPubblicazione <= oggi) e flaq di visualizzazione settato a TRUE altrimenti prende tutte le Faq
        if (!editor) {
            criteria.add(Restrictions.le(CoolPropertyIds.FAQ_DATA.value(), getNowUTC()));
            criteria.add(Restrictions.eq(CoolPropertyIds.FAQ_SHOW.value(), true));
        }
        return criteria.executeQuery(cmisSession, false, frontOfficeOperationContext);
    }

    private ItemIterable<QueryResult> executeQueryLog(String typeLog, String after, String before, Session cmisSession, String application, String userLog) {
        Calendar startDate = null;
        Calendar endDate = null;
        // usati nelle query con i campi  avvisi:data/faq:data (il widget passa ad Alfresco le 12:00 UTC (14 italiane) per il loro salvataggio ==> hanno tutti la stessa ora settata)
        if (after != null) {
            startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startDate.setTimeInMillis(Long.parseLong(after));
            startDate.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (before != null) {
            endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endDate.setTimeInMillis(Long.parseLong(before));
            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
        }
        // restituisco solo i 200 log più recenti
        frontOfficeOperationContext.setMaxItemsPerPage(200);
        Criteria criteria;
        criteria = CriteriaFactory.createCriteria(CoolPropertyIds.LOGGER_QUERY_NAME.value());
        if (typeLog != null)
            criteria.add(Restrictions.eq(CoolPropertyIds.LOGGER_CODE.value(), typeLog));

        if (application != null)
            criteria.add(Restrictions.like(CoolPropertyIds.LOGGER_APPLICATION.value(), application));

        if (userLog != null)
            criteria.add(Restrictions.eq(CoolPropertyIds.LOGGER_USER.value(), userLog));

        if (startDate != null && endDate != null)
            criteria.add(Restrictions.between(PropertyIds.CREATION_DATE, startDate.getTime(), endDate.getTime(), false, true, false));

        criteria.addOrder(Order.desc(PropertyIds.CREATION_DATE));
        return criteria.executeQuery(cmisSession, false, frontOfficeOperationContext);
    }
}
