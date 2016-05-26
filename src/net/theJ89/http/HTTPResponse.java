package net.theJ89.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonSyntaxException;

import net.theJ89.util.IO;

public class HTTPResponse implements AutoCloseable {
    private int         status;
    private Long        contentLength;
    private InputStream inputStream;
    
    public HTTPResponse( int status, Long contentLength, InputStream inputStream ) {
        this.status        = status;
        this.contentLength = contentLength;
        this.inputStream   = inputStream;
    }
    
    /**
     * Return the input stream for the response.
     * @return
     */
    public InputStream getInputStream() {
        return this.inputStream;
    }
    
    /**
     * Returns the response as a byte array.
     * @return
     * @throws IOException 
     */
    public byte[] getResponse() throws IOException {
        try( InputStream is = this.inputStream ) {
            return IO.toByteArray( this.inputStream );
        }        
    }
    
    /**
     * Returns the response as a UTF-8 encoded string.
     * @return
     * @throws IOException 
     */
    public String getText() throws IOException {
        try( InputStream is = this.inputStream ) {
            return new String( IO.toByteArray( this.inputStream ), StandardCharsets.UTF_8 );
        }
    }
    
    /**
     * Expects response to be JSON. Deserializes the response JSON into an object of the given type and returns it.
     * @param classOfE
     * @return
     * @throws IOException 
     * @throws JsonSyntaxException 
     */
    public <E> E getObject( Class<E> classOfE ) throws IOException {
        try( InputStream is = this.inputStream ) {
            return HTTP.gson.fromJson( IO.newBufferedU8ISReader( this.inputStream ), classOfE );
        }
    }
    
    /**
     * Returns the status code
     * @return
     */
    public final int getStatus() {
        return this.status;
    }
    
    /**
     * Returns true if status code is 200 (OK). Returns false otherwise.
     * @return
     */
    public boolean ok() {
        return this.status == 200;
    }
    
    /**
     * Returns the value of the Content-Length header.
     * Returns null if the server did not provide a Content-Length.
     * @return
     */
    public Long getContentLength() {
        return this.contentLength;
    }

    @Override
    public void close() {
        IO.closeQuietly( this.inputStream );
    }
}
