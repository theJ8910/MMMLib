package net.theJ89.http;

//Some common mime-types
public final class ContentType {
    public static final String TEXT_PLAIN            = "text/plain";
    public static final String APPLICATION_JSON      = "application/json";
    public static final String APPLICATION_XML       = "application/xml";
    public static final String X_WWW_FORM_URLENCODED = "x-www-form-urlencoded";
    
    private ContentType() { throw new Error(); };
}
