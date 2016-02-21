package net.theJ89.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.commons.io.Charsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.util.ResponseDeserializer;
import com.mojang.util.UUIDTypeAdapter;

public class HTTP {
    protected static Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( UUID.class,        new UUIDTypeAdapter()        );
        gb.registerTypeAdapter( Response.class,    new ResponseDeserializer()   );
        gb.registerTypeAdapter( PropertyMap.class, new PropertyMap.Serializer() );
        gson = gb.create();
    }
    
    /**
     * Converts the given URL from a string to a URL object.
     * @param url - The URL in string form.
     * @return The URL in object form.
     * @throws Error If the given string is a malformed URL.
     */
    public static URL stringToURL( String url ) {
        try {
            return new URL( url );
        } catch( MalformedURLException e ) {
            throw new Error( "Invalid URL: " + url, e );
        }
    }
    
    /**
     * Convenience function to perform a GET request to the given URL.
     * @param url - Target URL for the request.
     * @return HTTPResponse - Result of the request.
     * @throws IOException
     */
    public static HTTPResponse get( URL url ) throws IOException {
        return new HTTPRequest( url ).get();
    }
    
    /**
     * Convenience function to perform a POST request to the given URL.
     * The given object is serialized to JSON, encoded as UTF-8, and POSTed to the given URL.
     * @param url - Target URL for the request.
     * @param object - Can be any object that can be serialized to JSON.
     * @return HTTPResponse - Result of the request.
     * @throws IOException
     */
    public static <E> HTTPResponse post( URL url, E object ) throws IOException {
        return new HTTPRequest( url )
                  .setContentType( ContentType.APPLICATION_JSON, Charsets.UTF_8 )
                  .post( object );
    }
    /**
     * Convenience function to perform a POST request to the given URL.
     * Accepts a string as POST data.
     * @param url - Target URL for the request.
     * @param contentType - MIME type of the data being POSTed.
     * @param charset - Charset of the data being POSTed (e.g. UTF-8).
     * @param data - Data to POST.
     * @return HTTPResponse - Result of the request.
     * @throws IOException
     */
    public static HTTPResponse post( URL url, String contentType, Charset charset, String data ) throws IOException {
        return new HTTPRequest( url )
                  .setContentType( contentType, charset )
                  .post( data );
    }
    
    /**
     * Convenience function to perform a POST request to the given URL.
     * Accepts an array of bytes as POST data.
     * @param url - Target URL for the request.
     * @param contentType - MIME type of the data being POSTed.
     * @param charset - Charset of the data being POSTed (e.g. UTF-8).
     * @param data - Data to POST.
     * @return HTTPResponse - Result of the request.
     * @throws IOException
     */
    public static HTTPResponse post( URL url, String contentType, Charset charset, byte[] data ) throws IOException {
        return new HTTPRequest( url )
                  .setContentType( contentType, charset )
                  .post( data );
    }
}
