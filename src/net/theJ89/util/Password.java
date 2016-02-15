package net.theJ89.util;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Password {
    private char[] value;
    
    private enum MOVE { CONSTRUCTOR };
    
    /**
     * Reads a password from the standard input / console.
     * @return
     * @throws IOException - If there's an issue reading the password from the standard input / console.
     */
    public static Password read() throws IOException {
        //This will be null if we're running in Eclipse, which is totally STUPID
        Console c = System.console();
        if( c != null ) return new Password( c.readPassword(), MOVE.CONSTRUCTOR );
        else {
            BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
            return new Password( in.readLine().toCharArray(), MOVE.CONSTRUCTOR );
        }
    }
    
    /**
     * Initializes the stored password to null
     */
    public Password() {
        this.value = null;
    }
    
    
    /**
     * Initializes the stored password with a copy of the given password.
     * If it's no longer needed, you should manually clear the given password like so:
     *     Arrays.fill( password, '\0' );
     * @param password
     */
    public Password( char[] password ) {
        this.value = Arrays.copyOf( password, password.length );
    }
    
    
    /**
     * Initializes the stored password with a reference to the given character array.
     * Because the password is stored as a reference, this is a private constructor.
     * @param password
     * @param CONSTRUCTOR - The "MOVE CONSTRUCTOR" is only here for disambiguation purposes and is not used.
     * Pass MOVE.CONSTRUCTOR for it.
     */
    private Password( char[] password, MOVE CONSTRUCTOR ) {
        this.value = password;
    };
    
    /**
     * Returns a reference to the stored password's character array.
     * @return char[]
     */
    public char[] get() {
        return this.value;
    }
    
    /**
     * You should call this after you no longer need the password so it doesn't persist in memory
     */
    public void clear() {
        Arrays.fill( this.value, '\0' );
        this.value = null;
    }
}
