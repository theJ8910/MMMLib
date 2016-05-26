package net.theJ89.util;

/**
 * An enumeration listing all known versions of the Java specification (at the time of writing).
 * Based off of JavaVersion from Apache Commons Lang.
 */
public enum JavaVersion {
    /**
     * The Java version reported by Android.
     * Not an official Java version number.
     */
    JAVA_0_9( 0, 9 ),
    
    //Official versions of Java
    JAVA_1_1( 1, 1 ),
    JAVA_1_2( 1, 2 ),
    JAVA_1_3( 1, 3 ),
    JAVA_1_4( 1, 4 ),
    JAVA_1_5( 1, 5 ),
    JAVA_1_6( 1, 6 ),
    JAVA_1_7( 1, 7 ),
    JAVA_1_8( 1, 8 ),
    JAVA_1_9( 1, 9 ),
    
    /**
     * For all versions newer than Java 1.9.
     * Note: The name, major, and minor for JAVA_RECENT should be ignored.
     */
    JAVA_RECENT( 2, 0 );
    
    //Lookup array for Java 1.x
    private static final JavaVersion[] LOOKUP = new JavaVersion[] {
        null,
        JAVA_1_1,
        JAVA_1_2,
        JAVA_1_3,
        JAVA_1_4,
        JAVA_1_5,
        JAVA_1_6,
        JAVA_1_7,
        JAVA_1_8,
        JAVA_1_9
    };
    
    private final int major;
    private final int minor;
    
    /**
     * JavaVersion constructor.
     * @param major - The first part of the version (e.g. 1 in "1.7")
     * @param minor - The second part of the version (e.g. 7 in "1.7")
     */
    JavaVersion( final int major, final int minor ) {
        this.major = major;
        this.minor = minor;
    }
    
    /**
     * Parses a version name (e.g. "1.7") and return the associated JavaVersion enum (e.g. JAVA_1_7).
     * The string is expected to consist of at least two numbers separated by periods. null is returned if there are fewer than two parts.
     * Calls {@link #get(int,int)} with the  major and minor numbers parsed from the string.
     * @param name - a version name
     * @return a version enum
     */
    public static JavaVersion get( final String name ) {
        if( name == null )
            return null;
        String[] parts = name.split( "\\." );
        if( parts.length < 2 )
            return null;
        try { return get( Integer.parseInt( parts[0] ), Integer.parseInt( parts[1] ) ); }
        catch( NumberFormatException e ) { return null; }
    }
    
    /**
     * Returns the version enum corresponding to the given major and minor numbers (e.g. 1 and 7 returns JAVA_1_7).
     * If the version is unrecognized but less than the highest known version, the version is assumed invalid and null is returned.
     * If the version is unrecognized but higher than the highest known version, the version is assumed to be new and JAVA_RECENT is returned.
     * @param major - The first part of the version name (e.g. 1 in 1.7)
     * @param minor - The second part of the version name (e.g. 7 in 1.7)
     * @return
     */
    public static JavaVersion get( final int major, final int minor ) {
        //Invalid (negative) major / minor values
        if( major < 0 || minor < 0 )
            return null;
        
        if( major == 1 )
            if( minor < 10 )           return LOOKUP[minor]; //Java 1.0 - 1.9
            else                       return JAVA_RECENT;   //Java 1.10+
        if( major > 1)                 return JAVA_RECENT;   //Java 2+
        if( major == 0 && minor == 9 ) return JAVA_0_9;      //Java 0.9
        
        //Java 0.x (where x != 9)
        return null;
    }
    
    /**
     * Return true if this JavaVersion is at least the given JavaVersion.
     * 
     * <p>e.g.:<br/> {@code myVersion.atLeast( JavaVersion.JAVA_1_7 )};</p>
     * @param requiredVersion - The version to check this version against.
     * @return
     */
    public boolean atLeast( final JavaVersion requiredVersion ) {
        return this.ordinal() >= requiredVersion.ordinal();
    }
    
    /**
     * Returns the name of this version as a string.
     * @return
     */
    public String getName() {
        return String.format( "%d.%d", this.major, this.minor );
    }
    
    /**
     * Returns the first part of the version.
     * <p>e.g.:</p>
     * @return
     */
    public int getMajor() {
        return this.major;
    }
    
    /**
     * Returns the second part of the version.
     * @return
     */
    public int getMinor() {
        return this.minor;
    }
}
