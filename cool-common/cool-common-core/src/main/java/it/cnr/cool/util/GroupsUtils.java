package it.cnr.cool.util;

import it.cnr.cool.security.service.impl.alfresco.CMISGroup;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;

import java.util.ArrayList;
import java.util.List;

public class GroupsUtils {

    public static List<String> getGroups(CMISUser user) {

        List<String> groups = new ArrayList<String>();

        if (user.getGroups() != null) {
            for (CMISGroup g : user.getGroups()) {
                groups.add(g.getGroup_name());
            }
        }

        return groups;

    }
}
