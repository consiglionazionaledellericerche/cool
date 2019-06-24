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

import it.cnr.cool.MainTestContext;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.AdminService;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.util.Notice;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class NoticeTest {

    private final String afterString = "2010-07-13";
    private final String beforeString = "2010-07-14";
    private final String title = "testEditNotice";
    private final String type = "Tempo Determinato";
    @Autowired
    private FrontOfficeService frontOfficeService;
    @Autowired
    private CMISService cmisService;
    @Autowired
    private AdminService adminService;
    private Date after;
    private Date before;
    private Session cmisSession;
    private String nodeRefNotice;

    @Before
    public void createNotice() throws ParseException {
        SimpleDateFormat formatterEN = new SimpleDateFormat("yyyy-MM-dd");
        after = formatterEN.parse(afterString);
        before = formatterEN.parse(beforeString);
        cmisSession = cmisService.createAdminSession();

        String stackTrace = "{\"avvisi:number\":\"100021\",\"avvisi:style\":\"success\",\"avvisi:type\":\""
                + type
                + "\",\"avvisi:data\":\""
                + afterString
                + "T14:00:00.000+02:00\",\"avvisi:title\":\""
                + title
                + "\",\"avvisi:text\":\"test\",\"avvisi:dataScadenza\":\"2019-10-18T14:00:00.000+02:00\",\"avvisi:authority\":\"GROUP_EVERYONE\"}";

        // Creazione Avviso
        Map<String, Object> response = frontOfficeService.post(null, null, null, null, TypeDocument.Notice, stackTrace);
        nodeRefNotice = (String) response.get("objectId");
        assertTrue(nodeRefNotice != null);
    }

    @After
    public void deleteNotice() {
        frontOfficeService.deleteSingleNode(
                cmisSession, nodeRefNotice);
    }


    @Test
    public void testGetNotice() throws IOException {
        // Recupero solo l'avviso creato da createNotice()
        Map<String, Object> responseGet = frontOfficeService.getNotice(
                cmisSession, cmisSession, Long.toString(after.getTime()),
                Long.toString(before.getTime()), false, null);
        ArrayList<Notice> docs = (ArrayList<Notice>) responseGet.get("docs");
        boolean solrQueryCmis = adminService.isEVENTUALsolrQueryCmis(cmisService.getAdminSession());
        Notice notice = null;
        /**
         * Se l'avviso non viene recuperato il sistema usa SOLR per le query!!!
         */
        if (!solrQueryCmis) {
            assertTrue(docs.size() > 0);
            notice = docs.get(0);
            // nn deve avere il campo maxNotice perché editor = false
            assertTrue(!responseGet.containsKey("maxNotice"));
        } else {
            notice = new Notice(cmisSession.getObject(nodeRefNotice), "admin");
        }
        assertTrue(notice.getType().equals(type));
        assertTrue(notice.getTitle().equals(title));

        // getNotice con editor = false
        responseGet = frontOfficeService.getNotice(cmisSession, cmisSession,
                null, null, false, null);
        assertTrue(!responseGet.containsKey("maxNotice"));
        ArrayList<Notice> noticeEditorFalse = (ArrayList<Notice>) responseGet
                .get("docs");
        if (!solrQueryCmis) {
            assertTrue(noticeEditorFalse.size() > 0);
        }
        // getNotice con editor = true
        responseGet = frontOfficeService.getNotice(cmisSession, cmisSession,
                null, null, true, null);
        assertTrue(responseGet.containsKey("maxNotice"));
        ArrayList<Notice> noticeEditor = (ArrayList<Notice>) responseGet
                .get("docs");
        assertTrue(noticeEditor.size() >= noticeEditorFalse.size());
    }


    @Test
    public void testEditNotice() throws IOException, ParseException {
        // modifico l'avviso creato con il setUp
        String titleUpdate = "testEditNotice UPDATE";
        String typeUpdate = "Tempo Determinato UPDATE";
        String stackTraceUpdate = "{\"avvisi:number\":\"21\",\"avvisi:style\":\"success\",\"avvisi:type\":\""
                + typeUpdate
                + "\",\"avvisi:data\":\""
                + afterString
                + "T14:00:00.000+02:00\",\"avvisi:title\":\""
                + titleUpdate
                + "\",\"avvisi:text\":\""
                + titleUpdate
                + "\",\"avvisi:dataScadenza\":\"2019-10-18T14:00:00.000+02:00\",\"avvisi:authority\":\"GROUP_EVERYONE\",\"nodeRefToEdit\":\""
                + nodeRefNotice + "\"}";
        nodeRefNotice = (String) frontOfficeService.post(null, null, null, null, TypeDocument.Notice, stackTraceUpdate).get("objectId");
        CmisObject notice = cmisSession.getObject(nodeRefNotice.split(";")[0]);
        assertEquals(titleUpdate, notice
                .getPropertyValue(CoolPropertyIds.NOTICE_TITLE.value()));
        assertEquals(typeUpdate, notice.getPropertyValue(CoolPropertyIds.NOTICE_TYPE.value()));
    }
}