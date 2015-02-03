package it.cnr.cool.util;

public enum MimeTypes
{
    HTML ("text/html", ".html"),
    XHTML ("text/xhtml", ".xhtml"),
    TEXT ("text/plain", ".txt"),
    JAVASCRIPT ("text/javascript", ".js"),
    XML ("text/xml", ".xml"),
    PDF ("application/pdf", ".pdf"),
    ATOM ("application/atom+xml", ""),
    ATOMFEED ("application/atom+xml;type=feed", ""),
    ATOMENTRY ("application/atom+xml;type=entry", ""),
    FORMDATA ("multipart/form-data", ""),
    JSON ("application/json", ""),
    ZIP ("application/zip", ".zip");
    
    private String mimetype;



    private String extension;

    MimeTypes(String mimetype, String extension)
    {
        this.mimetype = mimetype;
        this.extension = extension;
    }
    
    public String mimetype()
    {
        return mimetype;
    }

    public String getExtension() {
        return extension;
    }
}