package net.theJ89.MMMLib;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.GameProfile;
import com.mojang.authlib.yggdrasil.User;
import com.mojang.authlib.yggdrasil.request.AuthenticationRequest;
import com.mojang.authlib.yggdrasil.request.InvalidateRequest;
import com.mojang.authlib.yggdrasil.request.RefreshRequest;
import com.mojang.authlib.yggdrasil.request.SignoutRequest;
import com.mojang.authlib.yggdrasil.request.ValidateRequest;
import com.mojang.authlib.yggdrasil.response.AuthenticationResponse;
import com.mojang.authlib.yggdrasil.response.ErrorResponse;
import com.mojang.authlib.yggdrasil.response.Response;

import net.theJ89.http.HTTP;
import net.theJ89.util.Password;

public class Auth {
    private static final String BASE_URL        = "https://authserver.mojang.com";
    private static final URL ROUTE_AUTHENTICATE = HTTP.stringToURL( BASE_URL + "/authenticate" );
    private static final URL ROUTE_REFRESH      = HTTP.stringToURL( BASE_URL + "/refresh"      );
    private static final URL ROUTE_VALIDATE     = HTTP.stringToURL( BASE_URL + "/validate"     );
    private static final URL ROUTE_SIGNOUT      = HTTP.stringToURL( BASE_URL + "/signout"      );
    private static final URL ROUTE_INVALIDATE   = HTTP.stringToURL( BASE_URL + "/invalidate"   );

    private static final Gson gson = new Gson();
    private static UserData userdata = new UserData();
    
    public static void loadSettings( Path path ) throws IOException {
        //File does not exist
        if( !Files.isRegularFile( path ) )
            return;
        
        Auth.userdata = gson.fromJson( Files.newBufferedReader( path, StandardCharsets.UTF_8 ), UserData.class );
    }
    
    public static void saveSettings( Path path ) throws IOException {
        gson.toJson( Auth.userdata, Files.newBufferedWriter( path, StandardCharsets.UTF_8 ) );
    }
    
    public static UserData getUserData() {
        return Auth.userdata;
    }
    
    
    
    
    /**
     * Logs the user in when provided with a valid username, password, and client token.
     * If successful, returns an access token.
     * The client / access token pair functions like a temporary username / password.
     * The combination of the two can be used to log in with {@link #logInWithAccessToken(UUID, UUID) loginWithAccessToken}.
     * Yggdrasil associates the given client / access token pair with your account on their end.
     * Logging in with a different client token will invalidate the previous client/access token pair.
     * This is presumably so you can't be logged in at two locations with the same account simultaneously.
     * @param username - The user's username / email.
     * @param password - The user's password.
     * @param clientToken - A UUID. 
     * @throws AuthenticationException - If the user could not be logged in for any reason.
     */
    public static void logInWithPassword( String username, Password password ) throws AuthenticationException {
        updateUserData( send(
            ROUTE_AUTHENTICATE,
            new AuthenticationRequest(
                username,
                new String( password.get() ),
                userdata.getClientToken()
            ),
            AuthenticationResponse.class
        ) );
    }
    
    /**
     * Logs the user in when provided with a valid client token and access token.
     * Access tokens expire after a given period of time.
     * Logging in with your client / access token pair will produce a new access token if it isn't.
     * @param clientToken - The client's UUID.
     * @param accessToken - Last known access token associated with the given clientToken.
     * @throws AuthenticationException - If the user could not be logged in for any reason
     */
    public static void logInWithAccessToken() throws AuthenticationException {
        UUID accessToken = userdata.getAccessToken();
        if( accessToken == null )
            throw new InvalidCredentialsException();
        
        //Check if the access token we already have is still valid or not. If it is, we can stop here.
        if( isAccessTokenValid() )
            return;
        
        //Access token is invalid; we need to refresh it.
        //Note: Yggdrasil responds with the same JSON for a RefreshRequest as it does for an AuthenticationResponse.
        updateUserData( send(
            ROUTE_REFRESH,
            new RefreshRequest( userdata.getClientToken(), accessToken ),
            AuthenticationResponse.class
        ) );
    }
    
    private static void updateUserData( AuthenticationResponse res ) throws AuthenticationException {
        //Not sure why the server sends the client token we sent it back to us, or why we need to check it here.
        if( !userdata.getClientToken().equals( res.getClientToken() ) )
            throw new AuthenticationException( "Client token mismatch between client and server." );
        
        //Update user data.
        GameProfile p = res.getSelectedProfile();
        User        u = res.getUser();
        userdata.setUsername( p.getName() );
        userdata.setUserID( u.getId() );
        userdata.setUUID( p.getId() );
        userdata.setAccessToken( res.getAccessToken() );
        userdata.setUserType( p.isLegacy() ? UserType.LEGACY : UserType.MOJANG );
        
        //Note: Both users and user profiles have a property map, but it seems like only the user's property map is used.
        PropertyMap properties = u.getProperties();
        userdata.setProperties( properties != null ?  properties : new PropertyMap() );
    }
    
    /**
     * Logs the user out when provided with a valid username and password.
     * @param username - The user's username / email.
     * @param password - The user's password.
     * @throws AuthenticationException - If the user could not be logged out for any reason
     */
    public static void logOutWithPassword( String username, Password password ) throws AuthenticationException {
        send( ROUTE_SIGNOUT, new SignoutRequest( username, new String( password.get() ) ) );
    }
    
    /**
     * Logs the user out when provided with a valid clientToken and accessToken.
     * @param clientToken - The client's UUID.
     * @param accessToken - An access token associated with the given clientToken.
     * @throws AuthenticationException If the user could not be logged out for any reason
     */
    public static void logOutWithAccessToken() throws AuthenticationException {
        UUID accessToken = userdata.getAccessToken();
        if( accessToken == null )
            throw new InvalidCredentialsException();
        send( ROUTE_INVALIDATE, new InvalidateRequest( userdata.getClientToken(), accessToken ) );
    }
    
    /**
     * Given a client / access token pair, checks if the pair is still valid.
     * @param clientToken - The client token.
     * @param accessToken - The access token.
     * @return true if the pair is valid, false otherwise.
     * @throws AuthenticationException - If the check could not be completed for any reason.
     */
    public static boolean isAccessTokenValid() throws AuthenticationException {
        UUID accessToken = userdata.getAccessToken();
        if( accessToken == null )
            return false;
        
        //If access token is invalid, send() will throw an InvalidCredentialsException is thrown with "Invalid token." as its error message.
        //All other exceptions are rethrown.
        //UserMigratedException is handled separately so that they aren't handled by the InvalidCredentialsException catch.
        try {
            send( ROUTE_VALIDATE, new ValidateRequest( userdata.getClientToken(), accessToken ) );
            return true;
        }
        catch( UserMigratedException e ) { throw e; } 
        catch( InvalidCredentialsException e ) {
            if( StringUtils.equals( e.getMessage(), "Invalid token." ) )
                return false;
            throw e;
        }
    }
    
    /**
     * Posts the given Yggdrasil request to the given route.
     * This should be a request for which no response is expected.
     * @param <E> - The request type.
     * @param url - The route to submit the request to.
     * @param req - The request to send.
     * @throws AuthenticationException If an error occurs while sending the request, processing the response, or if the server responds with an ErrorResponse.
     */
    private static <E> void send( URL url, E req ) throws AuthenticationException {
        Response any_res;
        try {
            any_res = HTTP.post( url, req ).getObject( Response.class );
            
            if( any_res instanceof ErrorResponse ) {
                ErrorResponse res = (ErrorResponse) any_res;
                if(      StringUtils.equals( res.getCause(), "UserMigratedException" ) )       throw new UserMigratedException(       res.getErrorMessage() );
                else if( StringUtils.equals( res.getError(), "ForbiddenOperationException" ) ) throw new InvalidCredentialsException( res.getErrorMessage() );
                else                                                                           throw new AuthenticationException(     res.getErrorMessage() );
            }
        }
        catch( IOException e )                                { throw new AuthenticationUnavailableException( e ); }
        catch( JsonParseException | IllegalStateException e ) { throw new AuthenticationException( e );            }
    };
    
    /**
     * Posts the given Yggdrasil request to the given route.
     * @param <E> - The request type.
     * @param <F> - The expected response type.
     * @param url - The route to submit the request to.
     * @param req - The request to send.
     * @param expectedResponseClass - Class of response we expect if the request is successful.
     * @return The response, or null if the response was empty.
     * @throws AuthenticationException If an error occurs while sending the request, processing the response, or if the server responds with an ErrorResponse.
     */
    private static <E,F> F send( URL url, E req, Class<F> expectedResponseClass ) throws AuthenticationException {
        Response any_res;
        try {
            //Convert request to JSON and post it to the given URL.
            //Then, convert the received response from JSON to some class implementing the Response interface (may or may not be the same as expectedResponseClass).
            //See ResponseDeserializer for how this is done.
            //If the JSON response is empty, this will be null.
            any_res = HTTP.post( url, req ).getObject( Response.class );
            
            //If we received an ErrorResponse, throw an appropriate AuthenticationException.
            if( any_res instanceof ErrorResponse ) {
                ErrorResponse res = (ErrorResponse) any_res;
                if(      StringUtils.equals( res.getCause(), "UserMigratedException" ) )       throw new UserMigratedException(       res.getErrorMessage() );
                else if( StringUtils.equals( res.getError(), "ForbiddenOperationException" ) ) throw new InvalidCredentialsException( res.getErrorMessage() );
                else                                                                           throw new AuthenticationException(     res.getErrorMessage() );
            //Otherwise, try to cast the response to the response we expect. Throws a ClassCastException if it's different from what we expect.
            } else {
                return expectedResponseClass.cast( any_res );
            }
        //Network problems
        } catch( IOException e ) {
            throw new AuthenticationUnavailableException( e );
        //Bad response
        } catch( JsonParseException | IllegalStateException | ClassCastException e ) {
            throw new AuthenticationException( e );
        }
    };
}