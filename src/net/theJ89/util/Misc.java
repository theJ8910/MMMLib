package net.theJ89.util;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class Misc {
    private static final Pattern RE_MD5  = Pattern.compile( "^[0-9a-fA-F]{32}$" );
    private static final Pattern RE_SHA1 = Pattern.compile( "^[0-9a-fA-F]{40}$" );
    
    /**
     * Same as {@link #getExtension(Path)} but takes a String for the path.
     * @param path
     * @return
     */
    public static String getExtension( final String path ) {
        return getExtension( Paths.get( path ) );
    }
    
    /**
     * Returns the extension of the file described by the given path.
     * The returned extension will always be in lowercase. The leading dot is omitted.
     * Returns "" if the extension could not be determined.
     * 
     * This function defines the extension of the given path to be the substring between the last dot and the end of the string.
     * e.g. getExtension( "mydir/myfile.tar.gz" ) would return "gz".
     * @param path
     * @return
     */
    public static String getExtension( final Path path ) {
        //Reduce path to filename
        String filename = path.getFileName().toString();
        
        //Find last dot (if any)
        int idx = filename.lastIndexOf( "." );
        if( idx == -1 )
            return "";
        
        //Extract the extension (idx + 1 to not include the dot)
        return filename.substring( idx + 1 ).toLowerCase();
    }
    
    /**
     * Same as {@link #getFileNameNoExtension(Path)} but takes a String for the path.
     * @param path
     * @return
     */
    public static String getFileNameNoExtension( final String path ) {
        return getFileNameNoExtension( Paths.get( path ) );
    }
    
    /**
     * Returns the filename of the file described by the given path without the extension (if any).
     * 
     * This function defines the extension of the given path to be the substring between the last dot and the end of the string.
     * @param path
     * @return
     */
    public static String getFileNameNoExtension( final Path path ) {
        //Reduce path to filename
        String filename = path.getFileName().toString();
        
        //Find last dot (if any)
        int idx = filename.lastIndexOf( "." );
        if( idx == -1 )
            return filename;
        
        //Trim the extension from the filename
        return filename.substring( 0, idx );
    }
    
    /**
     * Same as {@link #splitFilename(Path)} but takes a String for the path.
     * @param path
     * @return
     */
    public static String[] splitFilename( final String path ) {
        return splitFilename( Paths.get( path ) );
    }
    
    /**
     * Takes a path and returns an array of two strings: { filename, extension }
     * 
     * This function defines the extension of the given path to be the substring between the last dot and the end of the string.
     * @param path
     * @return
     */
    public static String[] splitFilename( final Path path ) {
        String filename = path.getFileName().toString();
        
        //Find last dot (if any)
        int idx = filename.lastIndexOf( "." );
        if( idx == -1 )
            return new String[] { filename, "" };
        
        //Return filename and extension
        return new String[] {
            filename.substring( 0, idx ),
            filename.substring( idx + 1 ).toLowerCase()
        };
    }
    
    /**
     * Returns a new MD5 instance.
     * @return
     */
    public static MessageDigest newMD5() {
        try                                 { return MessageDigest.getInstance( "MD5" ); }
        catch( NoSuchAlgorithmException e ) { throw new Error( e );                      }
    }
    
    /**
     * Returns a new SHA-1 instance.
     * @return
     */
    public static MessageDigest newSHA1() {
        try                                 { return MessageDigest.getInstance( "SHA-1" ); }
        catch( NoSuchAlgorithmException e ) { throw new Error( e );                        }
    }
    
    /**
     * Returns true if the given string represents a valid MD5 hash in hexadecimal form, false otherwise.
     * This function is non-case sensitive.
     * @param md5 - The string to check
     * @return
     */
    public static boolean isValidMD5( final String md5 ) {
        return RE_MD5.matcher( md5 ).matches();
    }
    
    /**
     * Returns true if the given string represents a valid SHA1 hash in hexadecimal form, false otherwise.
     * This function is non-case sensitive.
     * @param sha1 - The string to check
     * @return
     */
    public static boolean isValidSHA1( final String sha1 ) {
        return RE_SHA1.matcher( sha1 ).matches();
    }
    
    /**
     * Opens the given URI in the system's web browser.
     * Prints an error message if an exception occurs.
     * @param uri - URI of the address to visit.
     * @return true if the browser was launched, false otherwise.
     */
    public static boolean browse( final URI uri ) {
        try {
            Desktop.getDesktop().browse( uri );
            return true;
        } catch( UnsupportedOperationException | IOException e ) {
            System.err.println( "Couldn't open the browser. An error occurred:" );
            e.printStackTrace();
            return false;
        }
    }
}
