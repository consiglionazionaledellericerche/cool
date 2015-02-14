package it.cnr.cool.service;

import it.cnr.cool.repository.PermissionRepository;
import it.cnr.cool.web.PermissionTest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by francesco on 2/14/15.
 */


@Repository
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
