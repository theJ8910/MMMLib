package net.theJ89.mmm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.sql.SQLException;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;

import net.theJ89.forge.ForgeInstaller;
import net.theJ89.minecraft.MinecraftInstaller;
import net.theJ89.util.Password;

public class Main {
    private static final BufferedReader in  = new BufferedReader( new InputStreamReader( System.in ) );
    private static final String USERDATA_FILE = "userdata.json";
    
    public static void main( String[] args ) {
        PrintStream out = System.out;
        
        try {
            MMM.init();
            MMM.update();
            
            Path mmmDir = MMM.getDirectory();
            Path userdataFile = mmmDir.resolve( USERDATA_FILE );
            Auth.loadSettings( userdataFile );
            
            try {
                Auth.logInWithAccessToken();
                out.println( "Logged in with access token." );
                
            } catch( InvalidCredentialsException e ) {
                out.print( "Please enter your Minecraft username / email: " );
                String username = in.readLine();
                
                out.print( "Please enter your Minecraft password: " );
                Password password = Password.read();
                
                //Attempt login
                try     { Auth.logInWithPassword( username, password ); }
                finally { password.clear();                             }
            }
            
            Auth.saveSettings( userdataFile );
            
            Path idir  = mmmDir.resolve( "instances/1.8.9" );
            Side iside = Side.CLIENT;
            Instance i = new Instance( "MMM Install Test", "1.8.9", iside, idir );
            
            //Install minecraft
            MinecraftInstaller installer = new MinecraftInstaller( idir, "1.8.9", iside );
            installer.install();
            
            //Install forge
            ForgeInstaller installer2 = new ForgeInstaller( idir, "1.8.9", "11.15.1.1902", iside );
            installer2.install();
            
            //Launch minecraft (TEMP)
            i.launch( "1.8.9-forge1.8.9-11.15.1.1902-1.8.9", Auth.getUserData() );
            
            MMM.close();
        } catch( AuthenticationException | IOException | SQLException e ) {
            throw new Error( e );
        }
    }
}
