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
