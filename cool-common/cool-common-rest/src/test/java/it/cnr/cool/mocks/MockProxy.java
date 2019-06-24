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

package it.cnr.cool.mocks;

import it.cnr.cool.rest.Proxy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.AbstractMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Primary
@Path("proxy")
@Component
public class MockProxy extends Proxy implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        setBackends(Stream.of(
                new AbstractMap.SimpleEntry<>("helpdesk", Stream.of(
                        new AbstractMap.SimpleEntry<>("url", "https://testoil.si.cnr.it/helpdesk/rest/catg/HDConcorsi"),
                        new AbstractMap.SimpleEntry<>("userName", "app.hdselezioni"),
                        new AbstractMap.SimpleEntry<>("psw", "category!dyn!"))),
                new AbstractMap.SimpleEntry<>("alfresco", Stream.of(
                        new AbstractMap.SimpleEntry<>("url", "http://as1dock.si.cnr.it:8080/alfresco"),
                        new AbstractMap.SimpleEntry<>("userName", "admin"),
                        new AbstractMap.SimpleEntry<>("psw", "admin"))),
                new AbstractMap.SimpleEntry<>("alfresco-public", Stream.of(
                        new AbstractMap.SimpleEntry<>("url", "http://as1dock.si.cnr.it:8080/alfresco"))),
                new AbstractMap.SimpleEntry<>("missioni", Stream.of(
                        new AbstractMap.SimpleEntry<>("url", "http://as1dock.si.cnr.it:90/elasticsearch/")))

                ).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey,
                p -> p.getValue().collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))))
        );
    }
}
