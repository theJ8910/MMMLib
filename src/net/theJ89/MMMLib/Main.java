package net.theJ89.MMMLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;

import net.theJ89.util.Password;

public class Main {
    private static final BufferedReader in  = new BufferedReader( new InputStreamReader( System.in ) );
    private static final Path USERDATA_FILE = Paths.get( "userdata.json" );
    
    public static void main( String[] args ) {
        PrintStream out = System.out;
        
        try {
            Auth.loadSettings( USERDATA_FILE );
            
            try {
                Auth.logInWithAccessToken();
                out.print( "Logged in with access token." );
            } catch( InvalidCredentialsException e ) {
                out.print( "Please enter your Minecraft username / email: " );
                String username = in.readLine();
                
                out.print( "Please enter your Minecraft password: " );
                Password password = Password.read();
                
                //Attempt login
                Auth.logInWithPassword( username, password );
                password.clear();
                
                out.println( Auth.isAccessTokenValid() ? "Valid token." : "Invalid token." );
            }
            
            Auth.saveSettings( USERDATA_FILE );
        } catch( AuthenticationException | IOException e ) {
            throw new Error( e );
        }
    }
}
