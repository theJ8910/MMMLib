package net.theJ89.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.launcher.updater.DateTypeAdapter;
import com.mojang.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import com.mojang.launcher.updater.download.assets.AssetIndex;
import com.mojang.launcher.updater.download.assets.AssetIndex.AssetObject;
import com.mojang.launcher.versions.ExtractRules;

import net.minecraft.launcher.updater.Artifact;
import net.minecraft.launcher.updater.CompleteMinecraftVersion;
import net.minecraft.launcher.updater.DownloadType;
import net.minecraft.launcher.updater.Executable;
import net.minecraft.launcher.updater.Library;
import net.theJ89.http.HTTP;
import net.theJ89.http.HTTPResponse;
import net.theJ89.mmm.Side;
import net.theJ89.util.IO;
import net.theJ89.util.Misc;
import net.theJ89.util.Platform;
import net.theJ89.util.Target;

public class MinecraftInstaller {
    private static final String            BASE_ASSETS_URL         = "http://resources.download.minecraft.net/";
    private static final URI               MINECRAFT_EULA_URL      = HTTP.stringToURI( "https://account.mojang.com/documents/minecraft_eula" );
    private static final String            MINECRAFT_EULA_FILENAME = "eula.txt";
    private static final DateTimeFormatter FMT                     = DateTimeFormatter.ofPattern( "EEE MMMM d HH:mm:ss zzz yyyy" ); //Thu May 26 15:48:28 CDT 2016
    
    private static final Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( Date.class, new DateTypeAdapter() );
        gb.registerTypeAdapterFactory( new LowerCaseEnumTypeAdapterFactory() );
        gb.setPrettyPrinting();
        //Needed because we're serializing enum maps, which have non-primitive keys.
        //By enabling complex map serialization, map keys can be serialized by type adapters, instead of being converted directly to strings.
        //This allows LowerCaseEnumTypeAdapterFactory convert the map keys to lower-case before they're written.
        gb.enableComplexMapKeySerialization();
        
        gson = gb.create();
    }
    
    private Path   directory;
    private String name;
    private Side   side;
    
    
    /**
     * MinecraftInstaller constructor.
     * @param directory - Directory to install Minecraft to.
     * @param name - The name of the Minecraft version to install (e.g. "1.7.10", "1.8.9")
     * @param side - Which side to install (e.g. Side.CLIENT or Side.SERVER)
     * @throws IOException
     */
    public MinecraftInstaller( final Path directory, final String name, final Side side ) {
        this.directory = directory;
        this.name      = name;
        this.side      = side;
    }
    
    public void install() throws IOException {
        Path                     directory = this.directory;
        
        String                   name      = this.name;
        CompleteMinecraftVersion version   = MinecraftVersions.get( name );
        
        String                   assetName = version.getAssets();
        AssetIndex               assets    = MinecraftVersions.getAssetIndex( assetName );
        
        Side                     side      = this.side;
        Target                   target    = Platform.getTarget();
        
        Map< DownloadType, Executable > downloads = version.getDownloads();
        if( side == Side.CLIENT ) {
            //Create versions/ directory
            Path versionDir = directory.resolve( MinecraftConstants.VERSIONS_DIRECTORY ).resolve( name );
            Files.createDirectories( versionDir );
            
            //Download client executable
            Executable dl = downloads.get( DownloadType.CLIENT );
            download( dl.getURL(), versionDir.resolve( name + ".jar" ), dl.getSha1() );
            
            //Create version info file
            Path versionInfoPath = versionDir.resolve( name + ".json" );
            System.out.println( "Creating version info file at \"" + versionInfoPath + "\"..." );
            
            try( Writer writer = IO.newBufferedU8FileWriter( versionInfoPath ) ) {
                gson.toJson( version, writer );
            }
            
            //Create assets/ directories
            Path assetsDir     = directory.resolve( MinecraftConstants.ASSETS_DIRECTORY );
            Path assetsIndices = assetsDir.resolve( MinecraftConstants.ASSETS_INDICES_DIRECTORY );
            Files.createDirectories( assetsIndices );
            Path assetsObjects = assetsDir.resolve( MinecraftConstants.ASSETS_OBJECTS_DIRECTORY );
            Files.createDirectories( assetsObjects );
            
            //Create asset index file
            Path assetIndexPath = assetsIndices.resolve( assetName + ".json" );
            System.out.println( "Creating asset index file at \"" + assetIndexPath + "\"..." );
            try( Writer writer = IO.newBufferedU8FileWriter( assetIndexPath ) ) {
                gson.toJson( assets, writer );
            }
            
            //Download assets
            boolean virtual = assets.isVirtual();
            if( virtual ) {
                //Create assets/virtual/ directory
                Path assetsVirtual = assetsDir.resolve( MinecraftConstants.ASSETS_VIRTUAL_DIRECTORY );
                Files.createDirectories( assetsVirtual );
                
                //Download assets to the virtual directory according to their original paths
                for( Entry< String, AssetObject> entry : assets.getObjects().entrySet() ) {
                    AssetObject asset = entry.getValue();
                    String path = asset.getPath();
                    
                    download( new URL( BASE_ASSETS_URL + path ), assetsVirtual.resolve( entry.getKey() ) , asset.getHash() );
                }
            } else {
                //Download assets to the objects directory according to their hashes
                for( Entry< String, AssetObject> entry : assets.getObjects().entrySet() ) {
                    AssetObject asset = entry.getValue();
                    String path = asset.getPath();
                    
                    download( new URL( BASE_ASSETS_URL + path ), assetsObjects.resolve( path ), asset.getHash() );
                }
            }
            
            //Download libraries
            Path librariesDir = directory.resolve( MinecraftConstants.LIBRARIES_DIRECTORY );
            Path nativesDir   = directory.resolve( MinecraftConstants.NATIVES_DIRECTORY );
            
            for( Library library : version.getLibraries() ) {
                Artifact artifact = library.getArtifact( target );
                if( artifact == null )
                    continue;
                
                Path libPath = librariesDir.resolve( artifact.getPath() );
                download( artifact.getURL(), libPath, artifact.getSha1() );
                
                //Extract the library if it contains natives
                if( library.isNative() )
                    extract( libPath, nativesDir, library.getExtract() );
            }
        } else if( side == Side.SERVER ) {
            //Download server executable
            Executable dl = downloads.get( DownloadType.SERVER );
            download( dl.getURL(), directory.resolve( "minecraft_server." + name + ".jar" ), dl.getSha1() );
            
            //If eula.txt doesn't exist, ask user if they want to generate a signed eula.txt.
            Path eulaPath = directory.resolve( MINECRAFT_EULA_FILENAME );
            if( Files.exists( eulaPath ) || !this.eulaPrompt() )
                return;
            
            System.out.println( "Generating signed \"" + MINECRAFT_EULA_FILENAME + "\"..." );
            try( Writer out = IO.newBufferedU8FileWriter( eulaPath ) ) {
                out.write(
                    "#By changing the setting below to TRUE you are indicating your agreement to our EULA ("+ MINECRAFT_EULA_URL + ").\n" +
                    "#" + ZonedDateTime.now().format( FMT ) + "\n" +
                    "eula=true\n"
                );
            }
        }
    }
    
    public void uninstall() {
        //TODO
    }
    
    public void download( final URL url, final Path path, final String sha1 ) throws IOException {
        if( Files.exists( path ) )
            return;
        System.out.println( "Downloading " + url + " to " + path + ":" );
        Files.createDirectories( path.getParent() );
        try(
            HTTPResponse res = HTTP.get( url );
            OutputStream out = IO.newBufferedFileOutputStream( path )
        ) {
            if( !res.ok() )
                throw new RuntimeException( "Error downloading \"" + url + "\"." );
            IO.copy_SHA1( res.getInputStream(), out, sha1 );
        }
    }
    
    public void extract( Path jar, Path dir, ExtractRules rules ) throws IOException {
        JarFile jarfile = new JarFile( jar.toFile() );
        Enumeration<JarEntry> e = jarfile.entries();
        
        System.out.println( "Extracting \"/" + jar + "\" to \"" + dir + "\"..." );
        try {
            while( e.hasMoreElements() ) {
                JarEntry entry = e.nextElement();
                
                //There's nothing we can do with directories.
                if( entry.isDirectory() )
                    continue;
                
                //The library's extract rules control what gets extracted from the .jar and what doesn't.
                String name = entry.getName();
                if( !rules.shouldExtract( name ) )
                    continue;
                
                //If we've already extracted this file, we don't need to do it a second time
                Path extractPath = dir.resolve( name );
                if( Files.exists( extractPath ) )
                    continue;
                
                //Extract the file to the desired directory, creating any missing directories it needs in the process.
                System.out.println( "* Unzipping \"/" + name + "\"..." );
                Files.createDirectories( extractPath.getParent() );
                Files.copy( jarfile.getInputStream( entry ), extractPath );
            }
        } finally {
            jarfile.close();
        }
    }
    
    private boolean eulaPrompt() throws IOException {
        System.out.println( "Since Minecraft 1.7.10, Mojang requires users to agree to an End User License Agreement (EULA) before they can use the Minecraft Server." );
        System.out.println( "You can view their EULA at the following address (or type \"r\" at the following prompt):" );
        System.out.println( MINECRAFT_EULA_URL + "\n" );
        
        System.out.println( "Normally a user must run the server once, let it generate a \"" + MINECRAFT_EULA_FILENAME + "\" file, and then edit it to indicate they agree to the EULA." );
        System.out.println( "MMM can automate this step and generate a signed \"" + MINECRAFT_EULA_FILENAME + "\" for you if desired.\n" );
        
        BufferedReader in = IO.newBufferedISReader( System.in );
        while( true ) {
            System.out.print( "Do you agree to the Minecraft EULA and want MMM to generate a signed \"" + MINECRAFT_EULA_FILENAME + "\" for you? (y/n/r): " );
            
            String response = in.readLine().toLowerCase();
            switch( response ) {
            case "yes":
            case "y":
                return true;
            case "no":
            case "n":
                return false;
            case "read":
            case "r":
                System.out.println( "Opening " + MINECRAFT_EULA_URL + " in a browser..." );
                Misc.browse( MINECRAFT_EULA_URL );
                break;
            default:
                System.out.println( "Invalid choice. Please enter \"y\", \"n\", or \"r\"." );
            }
        }
    }
}
