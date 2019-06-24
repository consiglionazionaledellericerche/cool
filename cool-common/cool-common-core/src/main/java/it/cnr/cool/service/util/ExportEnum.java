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

package it.cnr.cool.service.util;

/**
 * Created by cirone on 16/03/2015.
 */
public enum ExportEnum {


    EXPORT_URL("service/export/exportContent"),
    QUERY_PARAM("query"),
    DESTINATION_PARAM("destination"),
    FILE_NAME_PARAM("filename"),
    NOACCENT_PARAM("noaccent"),
    FORMAT_PARAM("format"),
    NODES_PARAM("nodes"),
    DOWNLOAD_PARAM("download"),
    COMPRESS_PARAM("compress");

    private final String value;

    ExportEnum(String v) {
        value = v;
    }

    public static ExportEnum fromValue(String v) {
        for (ExportEnum c : ExportEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }


}
