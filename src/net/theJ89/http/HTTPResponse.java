package net.theJ89.http;

import org.apache.commons.io.Charsets;

public class HTTPResponse {
    private int status;
    private byte[] response;
    
    public HTTPResponse( int status, byte[] response ) {
        this.status = status;
        this.response = response;
    }
    
    public byte[] getResponse() {
        return this.response;
    }
    
    public String getText() {
        return new String( this.response, Charsets.UTF_8 );
    }
    
    //Expects response to be JSON. Deserializes the JSON into an object of the given type and returns it.
    public <E> E getObject( Class<E> classOfE ) {
        return HTTP.gson.fromJson( this.getText(), classOfE );
    }
    
    public final int getStatus() {
        return this.status;
    }
}
