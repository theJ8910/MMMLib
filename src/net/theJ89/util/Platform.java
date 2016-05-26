package net.theJ89.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Platform {
    /**
     * If this is true, we manually set the operating system name and version to Windows 10.
     */
    private static boolean winTenHack = false;

    /**
     * Call this to manually set the operating system to Windows 10 (in case Java doesn't detect this correctly)
     */
    public static void setWinTenHack() {
        System.setProperty( "os.name", "Windows 10" );
        System.setProperty( "os.version", "10" );
        winTenHack = true;
    }

    /**
     * If this returns true, setWinTenHack() was called to manually set the operating system name / version to Windows 10.
     * If this returns false, it was not set manually.
     * @return
     */
    public static boolean getWinTenHack() {
        return winTenHack;
    }

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
     * An absolute path to the user's application data directory.
     * These are all relative to the user's home directory.
     * On Windows, this is %APPDATA% (e.g. C:/Users/Owner/AppData/Roaming) or the user's home directory.
     * On Mac OS X, this is ~/Library/Application Support/ (e.g. /home/Owner/Library/Application Support).
     * On all other platforms, this is the same as the user's home directory (e.g. /home/owner).
     * @return The user's application data directory.
     */
    public static Path getAppDataDirectory() {
        Path home = Platform.getHomeDirectory();
        switch( Platform.getOS() ) {
        case WINDOWS:
            String appdata = System.getenv("APPDATA");
            return appdata != null ? Paths.get( appdata ) : home;
        case OSX:
            return home.resolve( "Library" ).resolve( "Application Support" );
        case LINUX:
        default:
            return home;
        }
    }

    /**
     * Returns an absolute path to the current working directory.
     * @return
     */
    public static Path getWorkingDirectory() {
        return Paths.get( "" ).toAbsolutePath();
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
     * Returns the version of Java the current JVM is using, or null if the version cannot be determined.
     * @return The java version.
     */
    public static JavaVersion getJavaVersion() {
        return JavaVersion.get( System.getProperty( "java.version" ) );
    }
}
