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

        if (!user.isGuest()) {
            groups.add("GROUP_EVERYONE");
        }

        return groups;

    }
}
