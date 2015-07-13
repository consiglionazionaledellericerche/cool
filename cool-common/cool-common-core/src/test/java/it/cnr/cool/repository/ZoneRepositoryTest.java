package it.cnr.cool.repository;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ZoneRepositoryTest {

	@Autowired
	private ZoneRepository zoneRepository;

	@Test
	public void testGet() {
		String zones = zoneRepository.get();
		JSONObject json = new JSONObject(zones);
		assertTrue(json.has("AUTH.ALF"));
	}

}
