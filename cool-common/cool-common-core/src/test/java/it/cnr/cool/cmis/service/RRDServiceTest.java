package it.cnr.cool.cmis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by francesco on 08/11/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/cool-common-core-test-context.xml" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RRDServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RRDServiceTest.class);

    @Autowired
    private RRDService rrdService;

    @Test
    public void getResources() throws Exception {

        long count = rrdService.getResources()
                .stream()
                .filter(Resource::isReadable)
                .map(Resource::getFilename)
                .peek(LOGGER::info)
                .count();

        assertEquals(3, count);

    }

}