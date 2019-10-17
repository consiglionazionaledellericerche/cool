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

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class CMISUtil {

    public static Map<String, Object> convertToProperties(QueryResult queryResult) {
        final HashMap<String, Object> collect1 = queryResult.getProperties()
                .stream()
                .collect(HashMap::new,
                        (m, c) -> m.put(c.getId(), getValue(c.getValues())),
                        (m, u) -> {
                        });
        collect1.put("allowableActions", Optional.ofNullable(queryResult.getAllowableActions())
                .map(AllowableActions::getAllowableActions)
                .orElse(Collections.EMPTY_SET)
                .stream()
                .collect(Collectors.toList()));
        return collect1;
    }

    public static Map<String, Object> convertToProperties(CmisObject cmisObject) {
        final HashMap<String, Object> collect1 = cmisObject.getProperties()
                .stream()
                .collect(HashMap::new,
                        (m, c) -> m.put(c.getId(), getValue(c.getValues(), c.isMultiValued())),
                        (m, u) -> {
                        });
        collect1.put("allowableActions", Optional.ofNullable(cmisObject.getAllowableActions())
                .map(AllowableActions::getAllowableActions)
                .orElse(Collections.EMPTY_SET)
                .stream()
                .collect(Collectors.toList()));
        return collect1;
    }

    private static Object getValue(List<?> result, Boolean isMultiValued) {
        return Optional.ofNullable(result)
                .filter(objects -> !objects.isEmpty())
                .map(objects -> {
                    if (!isMultiValued) {
                        return getValue(objects.get(0));
                    } else {
                        return objects
                                .stream()
                                .map(o -> getValue(o))
                                .collect(Collectors.toList());
                    }
                }).orElse(null);
    }

    private static Object getValue(Object result) {
        return Optional.ofNullable(result)
                .map(o -> {
                    if (Optional.of(o).filter(GregorianCalendar.class::isInstance).isPresent()) {
                        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                                Optional.of(o)
                                        .filter(GregorianCalendar.class::isInstance)
                                        .map(GregorianCalendar.class::cast)
                                        .map(GregorianCalendar::toZonedDateTime)
                                        .orElse(null)
                        );
                    }
                    return o;
                }).orElse(null);
    }

}
