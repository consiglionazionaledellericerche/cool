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

package it.cnr.cool.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtilTest.class);

    @Test
    public void testGetMd5EmptyString() throws Exception {
        InputStream is = IOUtils.toInputStream("");
        String md5 = StringUtil.getMd5(is);
        LOGGER.info(md5);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5);
    }

    @Test
    public void testGetMd5Space() throws Exception {
        InputStream is = IOUtils.toInputStream(" ");
        String md5 = StringUtil.getMd5(is);
        LOGGER.info(md5);
        assertEquals("7215ee9c7d9dc229d2921a40e899ec5f", md5);
    }


    @Test
    public void testGetMd5String() throws Exception {
        InputStream is = IOUtils.toInputStream("foo bar baz");
        String md5 = StringUtil.getMd5(is);
        LOGGER.info(md5);
        assertEquals("ab07acbb1e496801937adfa772424bf7", md5);
    }

}