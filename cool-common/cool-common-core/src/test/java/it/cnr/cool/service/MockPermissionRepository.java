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

package it.cnr.cool.service;

import it.cnr.cool.repository.PermissionRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by francesco on 2/14/15.
 */

@Repository
@Primary
public class MockPermissionRepository implements PermissionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPermissionRepository.class);

    private String content;

    @Override
    public String getRbac() {

        if (content == null) {
            InputStream is = MockPermissionRepository.class.getResourceAsStream("/rbac.get.json.ftl");
            try {
                content = IOUtils.toString(is);
            } catch (IOException e) {
                LOGGER.error("unable to load resource", e);
            }

        }
        return content;
    }

    @Override
    public boolean update(String s) {
        content = s;
        return true;
    }
}
