package net.theJ89.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.JavaVersion;

public class Platform {
    /**
     * Returns the bitness of the JVM.<br/>
     * Bitness is <a href="https://en.wiktionary.org/wiki/bitness">defined</a> as "The architecture of a computer system, in terms of how many bits compose a basic values it can deal with".
     * @return 32 for a 32-bit JVM or 64 for a 64-bit JVM.
     */
    public static int getBitness() {
        return Integer.parseInt( System.getProperty( "sun.arch.data.model" ) );
    }
    
    /**
     * Returns the current operating system
     * @return
     */
    public static OperatingSystem getOS() {
        return OperatingSystem.get( System.getProperty( "os.name" ) );
    }
    
    /**
     * Returns the version of the current operating system
     * @return
     */
    public static String getOSVersion() {
        return System.getProperty( "os.version" );
    }
    
    /**
     * Returns the architecture of the current operating system
     * @return
     */
    public static String getArchitecture() {
        return System.getProperty( "os.arch" );
    }
    
    /**
     * Returns the current target (operating system, version, and architecture).
     * @return
     */
    public static Target getTarget() {
        return new Target( getOS(), getOSVersion(), getArchitecture() );
    }
    
    /**
     * An absolute path to the user's home directory.
     * If Java cannot determine the user's home directory, returns the working directory.
     * @return The user's home directory.
     */
    public static Path getHomeDirectory() {
        return Paths.get( System.getProperty( "user.home", "." ) ).toAbsolutePath();
    }
    
    /**
     * Returns an absolute path to the current Java executable.
     * @param withConsole Chooses an appropriate JRE executable based on whether or not a console is desired.
     * If this is false, "javaw" is selected (doesn't spawn a console).
     * If this is true, "java" is selected (spawns a console).
     * @return Path to the requested executable.
     */
    public static Path getJavaExecutable( boolean withConsole ) {
        String executableName = getOS() == OperatingSystem.WINDOWS ? ( withConsole ? "java.exe" : "javaw.exe" ) : "java";
        return Paths.get( System.getProperty( "java.home" ) ).resolve( "bin" ).resolve( executableName ).toAbsolutePath();
    }
    
    /**
     * Returns the version of Java the current JVM is using.
     * @return The java version.
     */
    public static JavaVersion getJavaVersion() {
        return JavaVersion.valueOf( System.getProperty( "java.version" ) );
    }
}
