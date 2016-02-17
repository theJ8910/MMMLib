package net.theJ89.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Size {
    public static final long KILOBYTE = 1024;
    public static final long MEGABYTE = 1048576;
    public static final long GIGABYTE = 1073741824;
    //Note: "long" is the largest integer primitive that Java gives us (64-bits).
    //Unfortunately, because all integers in Java are signed, we can only represent numbers with it between [-2^32, 2^32-1].
    //Annoyingly, this means that TERABYTE = 2^40 = 1099511627776 is too large to represent here.
    //It's probably possible with BigInteger, but I'd prefer to stick with primitives here.
    
    public static final String[] BINARY_SUFFIXES = {
        " B",   //Bytes
        " KiB", //Kilobytes
        " MiB", //Megabytes
        " GiB"  //Gigabytes
    };
    public static final String[] METRIC_SUFFIXES = {
        " B",   //Bytes
        " KB",  //Kilobytes
        " MB",  //Megabytes
        " GB"   //Gigabytes
    };
    public static final String[] COMPACT_BINARY_SUFFIXES = {
        "",     //Bytes
        "K",    //Kilobytes
        "M",    //Megabytes
        "G"     //Gigabytes
    };
    public static final String[] VERBOSE_BINARY_SUFFIXES = {
        " bytes",     //Bytes
        " kibibytes", //Kilobytes
        " mebibytes", //Megabytes
        " gibibytes", //Gigabytes
    };
    public static final String[] VERBOSE_METRIC_SUFFIXES = {
        " bytes",     //Bytes
        " kilobytes", //Kilobytes
        " megabytes", //Megabytes
        " gigabytes", //Gigabytes
    };
    
    //Matches strings like:
    //102 MiB
    //5.3 GiB
    //1029.252e+16 B
    //5.06e-10 MiB
    //And so on
    private static final Pattern SIZE_STRING_PATTERN = Pattern.compile( "^(\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)( *[A-Za-z]*)$" );
    
    private Size() { throw new Error(); }
    
    /**
     * Takes a size string (such as one returned by {@link #toString()}) and returns the size in bytes.
     * Example:
     * <pre>
     *     Size.fromString( "10 MiB" ) //10485760
     * </pre>
     * @param str - The size string.
     * @return The size in bytes.
     * @throws NumberFormatException - If the size string is invalid.
     */
    public static long fromString( String str ) {
        return fromString( str, BINARY_SUFFIXES );
    }
    
    /**
     * Same as {@link #fromString( String )}, but you can specify the units.
     * Example:
     * <pre>
     *     Size.fromString( "1.5G", COMPACT_BINARY_SUFFIXES ) //1610612736
     * </pre>
     * @param str - The size string.
     * @param suffixes - An array of unit suffix strings, arranged like so:
     * <pre>
     * String[] suffixes = {
     *     " bytes",
     *     " kilobytes",
     *     " megabytes",
     *     " gigabytes"
     * };
     * </pre>
     * @return The size in bytes.
     * @throws NumberFormatException - If the size string is invalid.
     */
    public static long fromString( String str, String[] suffixes ) {
        Matcher m = SIZE_STRING_PATTERN.matcher( str );
        if( !m.matches() )
            throw new NumberFormatException( "The given string \"" + str + "\" is not a valid size string." );
        
        double size = Double.parseDouble( m.group( 1 ) );
        String suffix = m.group( 2 );
        if( suffix.equals( suffixes[0] ) ) {
            return (long)size;
        } else if( suffix.equals( suffixes[1] ) ) {
            return (long)(size * KILOBYTE);
        } else if( suffix.equals( suffixes[2] ) ) {
            return (long)(size * MEGABYTE);
        } else if( suffix.equals( suffixes[3] ) ) {
            return (long)(size * GIGABYTE);            
        } else {
            throw new NumberFormatException( "Missing or unrecognized suffix \"" + suffix + "\"." );
        }
    }
    
    /**
     * Rounds the given double to the given place.
     * Example:
     * <pre>
     * roundTo( 1234.567, -1 ) //1230
     * roundTo( 1234.567,  0 ) //1235
     * roundTo( 1234.567,  1 ) //1234.6
     * roundTo( 1234.567,  2 ) //1234.57
     * </pre>
     * @param d - The number to round
     * @param placesToRound - Number of places to round (negative to round to a place to the left of the decimal point).
     * @return
     */
    private static double roundTo( double d, int placesToRound ) {
        double factor = Math.pow( 10, placesToRound );
        return Math.round( d * factor ) / factor;
    }
    
    /**
     * Converts the given double to a string without trailing zeros.
     * @param d - The double to print as a string
     * @return
     */
    private static String doubleToStringNTZ( double d ) {
        long l = (long)d;
        if( l == d )
            return Long.toString( l );
        else
            return Double.toString( d );
    }
    
    /**
     * Returns a string describing the given size, converting the given size to appropriate units.
     * A suffix is attached to the end of the string to indicate which units are used.
     * Example:
     * <pre>
     *     Size.toString( 512               ); //"512 B"
     *     Size.toString( 1024              ); //"1 KiB"
     *     Size.toString( 4 * Size.GIGABYTE ); //"4 GiB"
     * </pre>
     * @param size
     * @return The size string.
     */
    public static String toString( long size ) {
        return toString( size, BINARY_SUFFIXES );
    }
    
    /**
     * Same as {@link #toString( long )}, but you can specify custom suffixes.
     * @param size - The size in bytes.
     * @param suffixes - An array of unit suffix strings arranged like so:
     * <pre>
     * String[] suffixes = {
     *     " bytes",
     *     " kilobytes",
     *     " megabytes",
     *     " gigabytes"
     * };
     * </pre>
     * @return A string indicating the size with unit suffix.
     */
    public static String toString( long size, String[] suffixes ) {
        if( size < KILOBYTE )
            return Long.toString(           size            ) + suffixes[0];
        if( size < MEGABYTE )
            return doubleToStringNTZ( (double)size / KILOBYTE ) + suffixes[1];
        if( size < GIGABYTE )
            return doubleToStringNTZ( (double)size / MEGABYTE ) + suffixes[2];
        
        return doubleToStringNTZ(     (double)size / GIGABYTE ) + suffixes[3];
    }
    
    /**
     * Same as {@link #toString( long )}, but rounds the size down to the given number of places.
     * @param size
     * @param placesToRound
     * @return
     */
    public static String toString( long size, int placesToRound ) {
        return toString( size, placesToRound, BINARY_SUFFIXES );
    }
    
    /**
     * Same as {@link #toString( long, String[] )}, but rounds the size down to the given number of places.
     * @param size
     * @param placesToRound
     * @param suffixes
     * @return
     */
    public static String toString( long size, int placesToRound, String[] suffixes ) {
        if( size < KILOBYTE )
            return Long.toString(              size                                     ) + suffixes[0];
        if( size < MEGABYTE )
            return doubleToStringNTZ( roundTo( (double)size / KILOBYTE, placesToRound ) ) + suffixes[1];
        if( size < GIGABYTE )
            return doubleToStringNTZ( roundTo( (double)size / MEGABYTE, placesToRound ) ) + suffixes[2];
        
        return doubleToStringNTZ(     roundTo( (double)size / GIGABYTE, placesToRound ) ) + suffixes[3];
    }
}
