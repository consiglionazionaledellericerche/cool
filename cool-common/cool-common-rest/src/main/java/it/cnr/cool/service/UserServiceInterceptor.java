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

import it.cnr.cool.cmis.service.CMISService;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceInterceptor {
    @Autowired
    private CMISService cmisService;

    protected boolean isUsed(String username) {
        return cmisService.createAdminSession().query(
                String.format("select * from cmis:document where %s = '%s'", PropertyIds.CREATED_BY, username),
                false
        ).getTotalNumItems() != 0;
    }

}
