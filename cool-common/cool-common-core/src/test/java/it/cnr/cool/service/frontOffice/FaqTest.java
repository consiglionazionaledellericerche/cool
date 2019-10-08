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
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.util.Faq;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MainTestContext.class})
public class FaqTest {
    private final String afterString = "2013-07-09";
    private final String question = "Question ";
    private final String answer = "Answer";
    @Autowired
    private FrontOfficeService frontOfficeService;
    @Autowired
    private CMISService cmisService;
    private Session adminSession;
    private String nodeRefFaq;
    private Date after;
    private Date before;

    @BeforeEach
    public void createFaq() throws ParseException, InterruptedException {
        SimpleDateFormat formatterEN = new SimpleDateFormat("yyyy-MM-dd");

        after = formatterEN.parse(afterString);
        before = formatterEN.parse("2013-07-10");
        adminSession = cmisService.createAdminSession();
        String stackTrace = "{\"faq:data\":\""
                + afterString
                + "T14:00:00.000+02:00\",\"faq:type\":\"Direttori\",\"faq:question\":\""
                + question + "\",\"faq:answer\":\"" + answer
                + "\",\"faq:number\":\"10022\",\"faq:show\":\"true\"}";

        // Post
        Map<String, Object> mapPost = frontOfficeService.post(null,
                null, null, null, TypeDocument.Faq, stackTrace);
        nodeRefFaq = (String) mapPost.get("objectId");
        assertTrue(nodeRefFaq != null);

        Document doc = adminSession.getLatestDocumentVersion(nodeRefFaq);
        assertEquals(doc.getPropertyValue(CoolPropertyIds.FAQ_ANSWER.value()), answer);
        assertEquals(doc.getPropertyValue(CoolPropertyIds.FAQ_QUESTION.value()), question);
        //sleep per consentire l'indicizzazione a solr
        Thread.sleep(15000);
    }

    @AfterEach
    public void deleteFaq() {
        frontOfficeService.deleteSingleNode(
                adminSession, nodeRefFaq);
    }

    @Test
    public void testGetFaq() throws IOException {
        // Recupero solo la faq creato da createFaq()
        Map<String, Object> response = frontOfficeService.getFaq(adminSession,
                Long.toString(after.getTime()),
                Long.toString(before.getTime()), null, false, null);
        ArrayList<Faq> listFaq = (ArrayList<Faq>) response.get("docs");
        assertTrue(listFaq.get(0).getAnswer().equals(answer));
        assertTrue(listFaq.get(0).getQuestion().equals(question));

        // getFaq con editor = false
        response = frontOfficeService.getFaq(adminSession, null, null, null,
                false, null);
        assertTrue(!response.containsKey("maxFaq"));
        ArrayList<Faq> faqEditorFalse = (ArrayList<Faq>) response.get("docs");
        assertTrue(faqEditorFalse.size() > 0);

        // getFaq con editor = true
        response = frontOfficeService.getFaq(
                adminSession, null, null, null, true, null);
        assertTrue(response.containsKey("maxFaq"));
        ArrayList<Faq> faqEditor = (ArrayList<Faq>) response.get("docs");
        assertTrue(faqEditor.size() >= faqEditorFalse.size());
    }

    @Test
    public void testEditFaq() throws IOException {

        String questionUpdate = "Question testPostDeleteFaq";
        String answerUpdate = "Answer testPostDeleteFaq";
        String stackTraceUpdate = "{\"faq:data\":\""
                + afterString
                + "T14:00:00.000+02:00\",\"faq:type\":\"Direttori\",\"faq:question\":\""
                + questionUpdate
                + "\",\"faq:answer\":\""
                + answerUpdate
                + "\",\"faq:number\":\"10022\",\"faq:show\":\"true\",\"nodeRefToEdit\":\""
                + nodeRefFaq + "\"}";
        nodeRefFaq = (String) frontOfficeService.post(null, null,
                null, null, TypeDocument.Faq, stackTraceUpdate).get("objectId");
        CmisObject faq = adminSession.getObject(nodeRefFaq.split(";")[0]);

        assertEquals(questionUpdate, faq.getPropertyValue(CoolPropertyIds.FAQ_QUESTION.value()));
        assertEquals(answerUpdate, faq.getPropertyValue(CoolPropertyIds.FAQ_ANSWER.value()));
    }
}