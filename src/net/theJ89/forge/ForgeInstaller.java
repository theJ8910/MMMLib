package net.theJ89.forge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.tukaani.xz.XZInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.launcher.updater.Library;
import net.theJ89.http.HTTP;
import net.theJ89.http.HTTPResponse;
import net.theJ89.json.ISO8601_OffsetDateTime_TypeAdapter;
import net.theJ89.minecraft.MinecraftConstants;
import net.theJ89.mmm.MMM;
import net.theJ89.mmm.Side;
import net.theJ89.util.IO;
import net.theJ89.util.Misc;

public class ForgeInstaller {
    private static final String MINECRAFT_LIBRARIES_URL = "https://libraries.minecraft.net/";
    private static final String PACK_NAME               = ".pack.xz";
    private static final String VERSION_INFO_FILENAME   = "version.json";
    private static final String CHECKSUMS_FILENAME      = "checksums.sha1";
    
    private static Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( OffsetDateTime.class, new ISO8601_OffsetDateTime_TypeAdapter() );
        gb.setPrettyPrinting();
        gb.enableComplexMapKeySerialization();
        
        gson = gb.create();
    }
    
    private Path     directory;
    private String   mc_name;
    private String   forge_name;
    private Side     side;
    
    public ForgeInstaller( final Path directory, final String mc_name, final String forge_name, final Side side ) {
        this.directory  = directory;
        this.mc_name    = mc_name;
        this.forge_name = forge_name;
        this.side       = side;
    }
    
    public void install() throws IOException {
        Path   directory  = this.directory;
        String mc_name    = this.mc_name;
        String forge_name = this.forge_name;
        Side   side       = this.side;
        
        Path tempForgeBinary = null;
        try {
            System.out.println( "Installing Minecraft Forge " + forge_name + "..." );
            
            //We need the Forge binary ahead of time.
            //It contains a version.json file with information needed by the Forge installation.
            tempForgeBinary = download();
            //tempForgeBinary = MMM.getTempDirectory().resolve( forge_name + ".jar" );
            
            //Get the version info for this version of Forge.
            ForgeVersionInfo fvi = getVersionInfo( tempForgeBinary );
            if( fvi == null )
                throw new RuntimeException( "Couldn't find version.json." );
            
            //Get the "official" name for this version.
            //It generally follows this format:
            //{mcver}-Forge({mcver}-)?{forgever}(-{mcver})?
            //where {mcver} is the Minecraft version, {forgever} is the standard forge version, and (x)? means x may or may not appear.
            String officialName = fvi.getID();
            
            //Create a custom Minecraft version info file.
            //This tells the launcher how to launch Minecraft with this version of Forge.
            if( side == Side.CLIENT ) {
                Path versionDir = directory.resolve( MinecraftConstants.VERSIONS_DIRECTORY ).resolve( officialName );
                Path fviPath = versionDir.resolve( officialName + ".json" );
                if( !Files.exists( fviPath ) ) {
                    //TEMP
                    //The Simple Forge installer makes a copy of the Minecraft client .jar and renames it.
                    //A version file can specify a jar to use a different version's jar, however.
                    //This eliminates the need to copy the file (in newer Forge installations).
                    //For the moment I'm opting to use the jar approach, but this will change as needed.
                    fvi.setJar( mc_name );
                    
                    if( !Files.exists( versionDir ) ) {
                        Files.createDirectory( versionDir );
                    }
                    System.out.println( "Creating version info file at \"" + fviPath + "\"..." );
                    try( Writer writer = Files.newBufferedWriter( fviPath ) ) {
                        gson.toJson( fvi, writer );
                    }
                }
            }
            
            //Get the installation path for the Forge binary and move our forge binary out of its temporary location
            Path librariesDir = directory.resolve( MinecraftConstants.LIBRARIES_DIRECTORY );
            Path forgeBinary = getForgeBinaryPath( fvi, librariesDir );
            if( !Files.exists( forgeBinary ) ) {
                System.out.println( "Installing Minecraft Forge binary to \"" + forgeBinary + "\"..." );
                Files.createDirectories( forgeBinary.getParent() );
                Files.move( tempForgeBinary, forgeBinary );
            }
            
            //Select a random mirror
            ForgeMirror mirror    = ForgeVersions.getRandomMirror();
            String      mirrorURL = mirror != null ? mirror.getURL() : null;
            
            //Download and install other libraries Forge needs
            List< ForgeLibrary > libs = fvi.getLibraries();
            List< ForgeLibrary > side_libs = null;
            if(      side == Side.CLIENT ) { side_libs = getClientLibraries( libs ); }
            else if( side == Side.SERVER ) { side_libs = getServerLibraries( libs ); }
            
            //Download the given libraries
            for( ForgeLibrary fl : side_libs ) {
                //Determine install path for this library
                String libraryPath = Library.getPathFromName( fl.getName() );
                Path installPath = librariesDir.resolve( libraryPath );
                
                //Do we already have it?
                if( Files.exists( installPath ) )
                    continue;
                
                //Select an appropriate source to download the library from
                String libMirror = fl.getURL();
                String baseURL;
                if( libMirror == null )      //Library did not specify a URL; assume this is a Minecraft library
                    baseURL = MINECRAFT_LIBRARIES_URL;
                else if( mirrorURL == null ) //Library specified a URL. We have no mirrors, so just use the library's URL.
                    baseURL = libMirror;
                else                         //Library specified a URL, but we have mirrors. Prefer downloading from the mirror.
                    baseURL = mirrorURL;
                
                System.out.println( "Installing library \"" + fl.getName() + "\"..." );
                
                //Download the library to the install path.
                //We'll try downloading a packed version of the library first.
                //Failing that, we'll try for an uncompressed version of the library.
                String url = baseURL + libraryPath;
                if( !downloadPackedLibrary( new URL( url + PACK_NAME ), installPath ) &&
                    !downloadLibrary(       new URL( url ),             installPath )
                ) {
                    throw new RuntimeException( "Failed to download library." );
                }
            }
        } catch( SQLException e ) {
            throw new RuntimeException( e );
        } finally {
            if( tempForgeBinary != null )
                Files.deleteIfExists( tempForgeBinary );
        }
    }
    
    public void uninstall() {
        //TODO
    }
    
    public Path download() throws IOException, SQLException {
        String name = this.forge_name;
        Side   side = this.side;
        
        //Locate a download for the requested Forge version
        ForgeDownload fd = ForgeVersions.getDownload( name, side );
        if( fd == null )
            throw new RuntimeException( "Can't find download for given version of Forge." );
        
        //Determine where we will (temporarily) download the file to.
        URL url = new URL( fd.getURL() );
        String[] filename = Misc.splitFilename( url.getPath() );
        
        Path path = Files.createTempFile( MMM.getTempDirectory(), filename[0] + "_", "." + filename[1] );
        
        //Download the file if it doesn't already exist.
        System.out.println( "Downloading Forge binary from \"" + url + "\" to temporary location \"" + path + "\"..." );
        try(
            HTTPResponse res = HTTP.get( url );
            OutputStream out = IO.newBufferedFileOutputStream( path )
        ) {
            if( !res.ok() )
                throw new RuntimeException( "Error downloading Forge binary." );
        
            IO.copy_MD5_SHA1( res.getInputStream(), out, fd.getMD5(), fd.getSHA1() );
        //Ensure temporary file is deleted in the case of an exception
        } catch( Throwable t ) {
            Files.deleteIfExists( path );
            throw t;
        }
        
        return path;
    }
    
    /**
     * Returns the path we should install the Forge binary to
     * @param fvi
     * @param librariesDir
     * @return
     */
    private Path getForgeBinaryPath( final ForgeVersionInfo fvi, final Path librariesDir ) {
        //Find which library is the Forge binary
        //Ideally we'd extract this from the "install" info, but that's in the forge installer.
        String forgeLibName = fvi.getForgeLibraryName();
        if( forgeLibName == null )
            throw new RuntimeException( "Couldn't determine the library name for this version of Minecraft Forge." );
        
        //TODO For the server, I'd prefer that it have the same name that its URL does, but this should do for now.
        if( this.side == Side.CLIENT ) { return librariesDir.resolve( Library.getPathFromName( forgeLibName ) ); }
        else                           { return this.directory.resolve( "forge-"+ this.mc_name + "-" + this.forge_name + "-universal.jar" ); }
    }
    
    /**
     * Extracts the version info from the Forge binary at the given path.
     * If the binary doesn't contain a version info file  (e.g. older version of Forge), returns null.
     * @param from
     * @return
     * @throws IOException
     */
    private static ForgeVersionInfo getVersionInfo( final Path from ) throws IOException {
        try( ZipFile zf = IO.getZipFile( from ) ) {
            //Find the version.json file in the forge binary
            ZipEntry entry = zf.getEntry( VERSION_INFO_FILENAME );
            if( entry == null )
                return null;
            
            //Deserialize the JSON into a ForgeVersionInfo
            try( Reader reader = IO.newBufferedU8ISReader( zf.getInputStream( entry ) ) ) {
                return gson.fromJson( reader, ForgeVersionInfo.class );
            }
        }
    }
    
    /**
     * Returns a list of libraries that this version of Minecraft Forge needs on the client that we should download
     * @param allLibraries
     * @return
     */
    private static List< ForgeLibrary > getClientLibraries( final List< ForgeLibrary > allLibraries ) {
        //NOTE:
        //As weird as this is, the client needs ALL of the libraries listed, including libraries
        //that aren't marked as "clientreq": true. These are distributed via Mojang's website.
        //Apparantly the way the Simple Forge installer does it, any libraries not marked as "clientreq": true
        //are not installed by it, but instead are downloaded at launch time by the Mojang launcher.
        //We, on the other hand, want to install all the libraries Forge needs during installation.
        //Best guess for why things are like this is that Forge relies on libraries that newer versions of Minecraft no longer needs.
        return allLibraries;
    }
    
    /**
     * Returns a list of libraries that this version of Minecraft Forge needs on the server that we should download
     * @param allLibraries
     * @return
     */
    private static List< ForgeLibrary > getServerLibraries( final List< ForgeLibrary > allLibraries ) {
        List< ForgeLibrary > fls = new ArrayList< ForgeLibrary >();
        for( ForgeLibrary fl : allLibraries ) {
            if( fl.isServerReq() )
                fls.add( fl );
        }
        return fls;
    }
    
    /**
     * Attempts to download and unpack the packed and compressed (.pack.xz) library at the given URL to the given path.
     * Returns true if the library was successfully downloaded and unpacked.
     * Returns false if the URL doesn't exist.
     * 
     * @param from
     * @param to
     * @return
     * @throws IOException
     */
    private static boolean downloadPackedLibrary( final URL from, final Path to ) throws IOException {
        try( HTTPResponse res = HTTP.get( from ) ) {
            if( !res.ok() )
                return false;
            
            System.out.println( "Downloading packed library from \"" + from + "\" to \"" + to + "\"..." );
            
            //Unpack the library to its desired location
            unpack( res.getInputStream(), to );
            return true;
        }
    }
    
    /**
     * Attempts to download the library at the given URL to the given path.
     * Returns true if the library was successfully downloaded.
     * Returns false if the URL doesn't exist.
     * 
     * @param from
     * @param to
     * @return
     * @throws IOException
     */
    private static boolean downloadLibrary( final URL from, final Path to ) throws IOException {
        try( HTTPResponse res = HTTP.get( from ) ) {
            if( !res.ok() )
                return false;
            
            System.out.println( "Downloading library from \"" + from + "\" to \"" + to + "\"..." );
            
            //Copy the library to its desired location
            Files.createDirectories( to.getParent() );
            Files.copy( res.getInputStream(), to );
            return true;
        }
    }
    
    /**
     * Unpack the .jar.pack.xz libraries we downloaded from the Forge mirror to a .jar at the given location.
     * Libraries hosted by Minecraft Forge are first packed with Pack200, then appended with metadata, then XZ compressed.
     * This function reverses this process, outputting the original library (with an additional checksum file).
     * @param in - Input stream to get the compressed library from
     * @param to - Path to output the decompressed library to.
     * @throws IOException
     */
    private static void unpack( final InputStream in, final Path to ) throws IOException {
        String[] filename = Misc.splitFilename( to );
        Path temp = Files.createTempFile( MMM.getTempDirectory(), filename[0] + "_", "." + filename[1] + ".pack" );
        try {
            //Decompress the data to a temporary file
            try(
                InputStream in2 = new XZInputStream( in );
                OutputStream out = IO.newBufferedFileOutputStream( temp )
            ) {
                IO.copy( in2, out );
            }
            
            //Read and then truncate the metadata from temporary file
            byte[] buf;
            try( RandomAccessFile raf = new RandomAccessFile( temp.toString(), "rw" ) ) {
                //Libraries hosted on Forge mirrors append some metadata to the end of the packed file before compressing it.
                //The metadata consists of:
                // * A checksums file. This is a list of newline delimited SHA-1 checksums (one for each file in the .jar).
                // * The length of the checksums file (as an unsigned 4-byte little-endian integer)
                // * The magic string "SIGN"
                long length = raf.length();
                if( length < 8 )
                    throw new RuntimeException( "Filesize is too small." );
                
                //Read the checksum lengths + magic string into a buffer
                long checksumsEnd = length - 8;
                buf = IO.read( raf, checksumsEnd, 8 );
                
                //Ensure the magic string is present
                String end = new String( buf, 4, 4, StandardCharsets.US_ASCII );
                if( !Objects.equals( end, "SIGN" ) )
                    throw new RuntimeException( "No signature." );
                
                //Decode the checksums length.
                //The (byte & 0xFF) is necessary to cast from signed bytes [-128,127] to unsigned bytes [0,255]. 
                int checksumsLength = (
                    ( buf[ 0 ] & 0xFF )       |
                    ( buf[ 1 ] & 0xFF ) << 8  |
                    ( buf[ 2 ] & 0xFF ) << 16 |
                    ( buf[ 3 ] & 0xFF ) << 24
                );
                
                if( checksumsLength > checksumsEnd )
                    throw new RuntimeException( "Invalid checksum file size." );
                
                //Read the checksums file to buf
                long checksumsBegin = checksumsEnd - checksumsLength;
                buf = IO.read( raf, checksumsBegin, checksumsLength );
                
                //Truncate the additional data
                raf.setLength( checksumsBegin );
            }
            
            //Unpack the library using Pack200 and attach the checksums to it
            Files.createDirectories( to.getParent() );
            try(
                InputStream     in2 = IO.newBufferedFileInputStream( temp );
                JarOutputStream out = IO.newBufferedJarFileOutputStream( to )
            ) {
                //Unpack the jar to the file at the given path
                Pack200.newUnpacker().unpack( in2, out );
                
                //Add the checksums file to the .jar as "checksums.sha1"
                JarEntry checksumsFile = new JarEntry( CHECKSUMS_FILENAME );
                checksumsFile.setTime( 0 );
                out.putNextEntry( checksumsFile );
                out.write( buf );
                out.closeEntry();
            }
        } finally {
            Files.deleteIfExists( temp );
        }
    }
}
