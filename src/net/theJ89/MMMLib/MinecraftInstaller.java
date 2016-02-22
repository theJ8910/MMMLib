package net.theJ89.MMMLib;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import net.theJ89.util.Platform;
import net.theJ89.util.Target;

public class MinecraftInstaller {
    private static final Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( Date.class, new DateTypeAdapter() );
        gb.registerTypeAdapterFactory( new LowerCaseEnumTypeAdapterFactory() );
        gson = gb.create();
    }
    
    private static final String BASE_ASSETS_URL    = "http://resources.download.minecraft.net/";
    
    private Instance                 instance;
    private CompleteMinecraftVersion version;
    private AssetIndex               assets;
    private Path                     directory;
    
    public MinecraftInstaller( Instance instance ) throws IOException {
        this.instance  = instance;
        this.version   = MinecraftVersions.get( instance.getMinecraftVersion() );
        this.assets    = MinecraftVersions.getAssetIndex( this.version.getAssets() );
        this.directory = instance.getDirectory();
    }
    
    public void install() throws IOException {
        String id      = this.version.getId();
        String assetID = this.version.getAssets();
        Side   side    = this.instance.getSide();
        Target target  = Platform.getTarget();
        
        Map< DownloadType, Executable > downloads = this.version.getDownloads();
        if( side == Side.CLIENT ) {
            //Create versions/ directory
            Path versionDir = this.directory.resolve( MinecraftConstants.VERSIONS_DIRECTORY ).resolve( id );
            Files.createDirectories( versionDir );
            
            //Download client executable
            Executable dl = downloads.get( DownloadType.CLIENT );
            download( dl.getURL(), versionDir.resolve( id + ".jar" ), dl.getSha1() );
            
            //Create version info file
            Path versionInfoPath = versionDir.resolve( id + ".json" );
            System.out.println( "Creating version info file at \"" + versionInfoPath + "\"..." );
            Writer writer = Files.newBufferedWriter( versionInfoPath );
            try     { gson.toJson( this.version, writer ); }
            finally { writer.close(); }
            
            //Create assets/ directories
            Path assetsDir     = this.directory.resolve( MinecraftConstants.ASSETS_DIRECTORY );
            Path assetsIndices = assetsDir.resolve( MinecraftConstants.ASSETS_INDICES_DIRECTORY );
            Files.createDirectories( assetsIndices );
            Path assetsObjects = assetsDir.resolve( MinecraftConstants.ASSETS_OBJECTS_DIRECTORY );
            Files.createDirectories( assetsObjects );
            
            //Create asset index file
            Path assetIndexPath = assetsIndices.resolve( assetID + ".json" );
            System.out.println( "Creating asset index file at \"" + assetIndexPath + "\"..." );
            writer = Files.newBufferedWriter( assetIndexPath );
            try     { gson.toJson( this.assets, writer ); }
            finally { writer.close(); }
            
            //Download assets
            boolean virtual = this.assets.isVirtual();
            if( virtual ) {
                //Create assets/virtual/ directory
                Path assetsVirtual = assetsDir.resolve( MinecraftConstants.ASSETS_VIRTUAL_DIRECTORY );
                Files.createDirectories( assetsVirtual );
                
                //Download assets to the virtual directory according to their original paths
                for( Entry< String, AssetObject> entry : this.assets.getObjects().entrySet() ) {
                    AssetObject asset = entry.getValue();
                    String path = asset.getPath();
                    
                    download( new URL( BASE_ASSETS_URL + path ), assetsVirtual.resolve( entry.getKey() ) , asset.getHash() );
                }
            } else {
                //Download assets to the objects directory according to their hashes
                for( Entry< String, AssetObject> entry : this.assets.getObjects().entrySet() ) {
                    AssetObject asset = entry.getValue();
                    String path = asset.getPath();
                    
                    download( new URL( BASE_ASSETS_URL + path ), assetsObjects.resolve( path ), asset.getHash() );
                }
            }
            
            //Download libraries
            Path librariesDir = this.directory.resolve( MinecraftConstants.LIBRARIES_DIRECTORY );
            Path nativesDir = this.directory.resolve( MinecraftConstants.NATIVES_DIRECTORY );
            for( Library library : this.version.getLibraries() ) {
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
            download( dl.getURL(), this.directory.resolve( "minecraft_server." + id + ".jar" ), dl.getSha1() );
        }
    }
    
    public void uninstall() {
        //TODO
    }
    
    public void download( URL url, Path path, String sha1 ) throws IOException {
        if( Files.exists( path ) )
            return;
        System.out.println( "Downloading: " + url );
        byte[] response = HTTP.get( url ).getResponse();
        String responseSHA1 = computeSHA1( response );
        if( !sha1.equals( responseSHA1 ) )
            throw new RuntimeException( "Downloaded of \"" + url + "\" failed: SHA1 hash (" + responseSHA1 + ") doesn't match expected hash (" + sha1 + ")." );
        System.out.println( "Success. Installing to: " + path );
        Files.createDirectories( path.getParent() );
        Files.write( path, response );
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
    
    private String computeSHA1( byte[] bytes ) {
        try {
            MessageDigest md = MessageDigest.getInstance( "SHA-1" );
            md.reset();
            return String.format( "%040x", new BigInteger( 1, md.digest( bytes ) ) );
        } catch (NoSuchAlgorithmException e) { throw new Error( e ); }
    }
}
