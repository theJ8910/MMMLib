package net.theJ89.mmm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import net.theJ89.forge.ForgeVersions;
import net.theJ89.minecraft.MinecraftVersions;
import net.theJ89.util.Platform;

public class MMM {
    //Absolute path to MMM's config directory.
    private static final Path MMM_CONFIG_DIR   = Platform.getAppDataDirectory().resolve( ".mmm" ).toAbsolutePath();
    
    //Name of the file containing the absolute path of MMM's installation directory
    private static final Path MMM_INSTALL_FILE = MMM_CONFIG_DIR.resolve( "install.txt" );
    
    //Name of the temporary directory inside of MMM's installation directory.
    private static final String TEMP_DIR_NAME = "temp";
    
    //Whether or not MMM is running in portable mode or not.
    private static boolean portable = true;
    
    //MMM's install directory.
    private static Path directory = null;
    private static Path tempDirectory = null;
    
    private MMM() {
        throw new Error();
    }
    
    /**
     * Initializes MMMLib.
     * Here we locate the MMM installation directory (if one exists).
     * @throws IOException
     */
    public static void init() throws IOException {
        //Locate MMM's install directory.
        if( Files.isRegularFile( MMM_INSTALL_FILE ) ) {
            Path dir = Paths.get( new String( Files.readAllBytes( MMM_INSTALL_FILE ), StandardCharsets.UTF_8 ) );
            
            //Verify that the directory named actually exists
            if( Files.isDirectory( dir ) ) {
                portable  = false;
                directory = dir;
                tempDirectory = dir.resolve( TEMP_DIR_NAME );
            }
        } else {
            System.err.println( "Couldn't locate MMM's installation directory, running MMM in portable mode." );
            portable = true;
            directory = Platform.getWorkingDirectory();
            tempDirectory = directory.resolve( TEMP_DIR_NAME );
        }
        
        //Init modules
        try {
            MinecraftVersions.init();
            ForgeVersions.init();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException( e );
        }
    }
    
    public static void update() throws IOException, SQLException {
        MinecraftVersions.update();
        ForgeVersions.update();
    }
    
    public static void close() {
        MinecraftVersions.close();
        ForgeVersions.close();
    }
    
    /**
     * TEMP
     * 
     * Installs MMM to the default location.
     * Equivalent to install(MMM_CONFIG_DIR).
     * @throws IOException 
     */
    public static void install() throws IOException {
        install( MMM_CONFIG_DIR );
    }
    
    /**
     * TEMP
     * 
     * Installs MMM to the given directory.
     * Creates a config directory in the user's home directory with a file recording
     * where the installation directory is to allow MMM to easily locate the directory between sessions.
     * @throws IOException 
     */
    public static void install( Path dir ) throws IOException {
        //If the config directory doesn't exist, create one.
        if( !Files.exists( MMM_CONFIG_DIR ) ) {
            Files.createDirectory( MMM_CONFIG_DIR );
        }
        
        //Fail if the directory is non-empty
        if( Files.list( dir ).findFirst().isPresent() )
            throw new IOException( "Directory is non-empty" );
        
        //Write the installation path to the file.
        Files.write( MMM_INSTALL_FILE, dir.toString().getBytes() );
        
        //Create the temporary files directory
        Path tempDir = dir.resolve( TEMP_DIR_NAME );
        Files.createDirectory( tempDir );
        
        portable = false;
        directory = dir;
        tempDirectory = tempDir;
    }
    
    /**
     * TEMP
     * 
     * Uninstalls MMM.
     * @throws IOException
     */
    public static void uninstall() throws IOException {
        //Delete the MMM installation directory
        if( directory != null && Files.isDirectory( directory ) ) {
            //TODO: Delete files installed by MMM in its installation directory
            Files.delete( tempDirectory );
            Files.delete( directory );
        }
        
        //Delete the MMM config directory
        if( Files.isDirectory( MMM_CONFIG_DIR ) ) {
            if( Files.isRegularFile( MMM_INSTALL_FILE ) )
                Files.delete( MMM_INSTALL_FILE );
            Files.delete( MMM_CONFIG_DIR );
        }
            
    }
    
    /**
     * Returns an absolute path to MMM's installation directory.
     * By default this is the same as MMM's config directory.
     * @return MMM's install directory.
     */
    public static Path getDirectory() {
        return directory;
    }
    
    /**
     * Returns an absolute path to a directory that can be used for temporary files.
     * By default this is the "temp" folder in MMM's installation directory.
     * @return
     */
    public static Path getTempDirectory() {
        return tempDirectory;
    }
    
    /**
     * Returns true if MMM is running in portable mode.
     * 
     * MMM can be run in portable mode if an installation of MMM on the target system is not desired.
     * 
     * While running in portable mode, shared resources (such as Minecraft binaries, libraries, assets, etc)
     * are installed directly to the instances requiring them, rather than to MMM's installation directory.
     * This eliminates the need for an installation of MMM, but may make inefficient use of disk space.
     * 
     * @return true if running in portable mode, false otherwise.
     */
    public static boolean isPortable() {
        return portable;
    }
}
