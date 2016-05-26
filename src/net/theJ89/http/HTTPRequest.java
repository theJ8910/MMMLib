package net.theJ89.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import net.theJ89.util.IO;

public class HTTPRequest {
    HttpURLConnection connection;
    String contentType;
    
    public HTTPRequest( URL url ) throws IOException {
        connection = null;
        
        HttpURLConnection u = (HttpURLConnection)url.openConnection();
        u.setConnectTimeout( 15000 );
        u.setReadTimeout( 15000 );
        u.setUseCaches( false );
        this.connection = u;
    }
    
    public HTTPRequest setContentType( String contentType ) {
        this.connection.setRequestProperty( "Content-Type", contentType );
        return this;
    }
    
    public HTTPRequest setContentType( String contentType, Charset charset ) {
        this.connection.setRequestProperty( "Content-Type", contentType + "; charset=" + charset.name().toLowerCase() );
        return this;
    }
    
    public HTTPResponse head() throws IOException {
        try { this.connection.setRequestMethod( "HEAD" ); }
        catch( ProtocolException e ) { throw new Error( e ); }
        
        return this.readResponse();
    }
    
    public HTTPResponse get() throws IOException {
        try                          { this.connection.setRequestMethod( "GET" ); }
        catch( ProtocolException e ) { throw new Error( e ); }
        
        return this.readResponse();
    }
    
    //Serializes the given object to JSON
    public <E> HTTPResponse post( E object ) throws IOException {
        return post( HTTP.gson.toJson( object ) );
    }
    
    public HTTPResponse post( String data ) throws IOException {
        return this.post( data.getBytes( StandardCharsets.UTF_8 ) );
    }
    
    public HTTPResponse post( byte[] data ) throws IOException {
        try                          { this.connection.setRequestMethod( "POST" ); }
        catch( ProtocolException e ) { throw new Error( e ); }
        
        connection.setRequestProperty( "Content-Length", Integer.toString( data.length ) );
        connection.setDoOutput( true );
        
        //Write POST data
        try( OutputStream out = connection.getOutputStream() ) {
            out.write( data );
        }
        
        return this.readResponse();
    }
    
    private HTTPResponse readResponse() throws IOException {
        InputStream in = null;
        try {
            in = this.connection.getInputStream();
            long contentLength = this.connection.getContentLengthLong();
            return new HTTPResponse( this.connection.getResponseCode(), contentLength != -1 ? contentLength : null, in );
        } catch( IOException e ) {
            IO.closeQuietly( in );
            in = this.connection.getErrorStream();
            return new HTTPResponse( this.connection.getResponseCode(), null, in );
        }
    }
}