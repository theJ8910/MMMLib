package net.theJ89.MMMLib;

public enum OperatingSystem {
	//Note: http://www.java-gaming.org/index.php/topic,14110
	LINUX( "Linux", "linux", "unix" ),
	WINDOWS( "Windows", "windows" ),
	OSX( "OS X", "mac", "os x" ),
	UNKNOWN( "Unknown" );
	
    //Only these are actually valid
    public static OperatingSystem[] validTypes = { LINUX, WINDOWS, OSX };
    
    //Printed name + list of identifying keywords.
	public String   name;
	public String[] keywords;
	
	OperatingSystem( String name, String... keywords ) {
		this.name     = name;
		this.keywords = keywords;
	}
	
	//Returns the current operating system
	public static OperatingSystem get() {
        return get( System.getProperty( "os.name" ) );
    }
	
	//Given an operating system name (e.g. "Windows XP"), returns an OperatingSystem enum.
	//Returns OperatingSystem.UNKNOWN if the given name is not recognized / the OS is not supported.
	public static OperatingSystem get( String name ) {
		name = name.toLowerCase();
		
		//Two versions of Windows can have slightly different names (e.g. Windows NT, Windows XP, etc).
        //Check for a keyword to determine what operating system this is.
		for( OperatingSystem os : validTypes )
			for( String keyword : os.keywords )
				if( name.contains( keyword ) )
					return os;
		return UNKNOWN;
	}
}
