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

import it.cnr.cool.MainTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Created by francesco on 08/11/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
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

        assertTrue(count >= 3);

    }

}
