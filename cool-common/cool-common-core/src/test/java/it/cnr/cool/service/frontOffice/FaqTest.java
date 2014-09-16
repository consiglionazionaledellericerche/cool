package it.cnr.cool.service.frontOffice;

import static org.junit.Assert.assertTrue;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.util.Faq;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class FaqTest {
	@Autowired
	private FrontOfficeService frontOfficeService;
	@Autowired
	private CMISService cmisService;
	private Session cmisSession;
	private final String afterString = "2013-07-09";
	private String nodeRefFaq;
	private Date after;
	private Date before;
	private final String question = "Question ";
	private final String answer = "Answer";


	@Before
	public void createFaq() throws ParseException {
		SimpleDateFormat formatterEN = new SimpleDateFormat("yyyy-MM-dd");

		after = formatterEN.parse(afterString);
		before = formatterEN.parse("2013-07-10");
		cmisSession = cmisService.createAdminSession();
		String stackTrace = "{\"faq:data\":\""
				+ afterString
				+ "T14:00:00.000+02:00\",\"faq:type\":\"Direttori\",\"faq:question\":\""
				+ question + "\",\"faq:answer\":\"" + answer
				+ "\",\"faq:number\":\"10022\",\"faq:show\":\"true\"}";

		// Post
		Map<String, Object> mapPost = frontOfficeService.post(null, null, null,
				null, null, null, TypeDocument.Faq, stackTrace);
		nodeRefFaq = (String) mapPost.get("objectId");
		assertTrue(nodeRefFaq != null);
	}

	@After
	public void deleteFaq() {
		frontOfficeService.deleteSingleNode(
				cmisSession, nodeRefFaq);
	}

	@Test
	public void testGetFaq() throws IOException {
		// Recupero solo la faq creato da createFaq()
		Map<String, Object> response = frontOfficeService.getFaq(cmisSession,
				Long.toString(after.getTime()),
				Long.toString(before.getTime()), null, false, null);
		ArrayList<Faq> listFaq = (ArrayList<Faq>) response.get("docs");
		assertTrue(listFaq.get(0).getAnswer().equals(answer));
		assertTrue(listFaq.get(0).getQuestion().equals(question));

		// getFaq con editor = false
		response = frontOfficeService.getFaq(cmisSession, null, null, null,
				false, null);
		assertTrue(!response.containsKey("maxFaq"));
		ArrayList<Faq> faqEditorFalse = (ArrayList<Faq>) response.get("docs");
		assertTrue(faqEditorFalse.size() > 0);

		// getFaq con editor = true
		response = frontOfficeService.getFaq(
				cmisSession, null, null, null, true, null);
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
		nodeRefFaq = (String) frontOfficeService.post(null, null, null, null,
				null, null, TypeDocument.Faq, stackTraceUpdate).get("objectId");
		CmisObject faq = cmisSession.getObject(nodeRefFaq);

		assertTrue(faq.getPropertyValue(CoolPropertyIds.FAQ_QUESTION.value())
				.equals(questionUpdate));
		assertTrue(faq.getPropertyValue(CoolPropertyIds.FAQ_ANSWER.value())
				.equals(answerUpdate));
	}
}