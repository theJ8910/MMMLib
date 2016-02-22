package net.theJ89.MMMLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.launcher.updater.DateTypeAdapter;
import com.mojang.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import com.mojang.launcher.updater.download.assets.AssetIndex;

import net.minecraft.launcher.updater.CompleteMinecraftVersion;

public class MinecraftVersions {
    private static final Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( Date.class, new DateTypeAdapter() );
        gb.registerTypeAdapterFactory( new LowerCaseEnumTypeAdapterFactory() );
        gson = gb.create();
    }
    
    //Directories (relative to MMM's root) that version and asset info .json files are stored.
    private static final String VERSIONS_DIRECTORY = "versions";
    private static final String ASSETS_DIRECTORY   = "assets";
    
    private static final Map< String, CompleteMinecraftVersion > versions = new HashMap< String, CompleteMinecraftVersion >();
    private static final Map< String, AssetIndex >               assets   = new HashMap< String, AssetIndex >();
    
    /**
     * Return information about a version of Minecraft.
     * Loads the information if it isn't already.
     * @param id - The version of Minecraft to get information from (e.g. "1.5.2", "1.6.4", "1.7.10")
     * @return Information about the requested Minecraft version.
     * @throws IOException
     */
    public static CompleteMinecraftVersion get( String id ) throws IOException {
        CompleteMinecraftVersion v = versions.get( id );
        if( v == null )
            v = load( id );
        return v;
    }
    
    /**
     * Returns the given asset index.
     * @return
     */
    public static AssetIndex getAssetIndex( String id ) throws IOException {
        AssetIndex i = assets.get( id );
        if( i == null )
            i = loadAssetIndex( id );
        return i;
    }
    
    /**
     * Loads information about the requested Minecraft version.
     * @param version
     * @return
     * @throws IOException
     */
    private static CompleteMinecraftVersion load( String id ) throws IOException {
        CompleteMinecraftVersion v = gson.fromJson( Files.newBufferedReader( Paths.get( VERSIONS_DIRECTORY, id + ".json"   ) ), CompleteMinecraftVersion.class );
        v.validate( id );
        
        versions.put( id, v );
        return v;
    }
    
    /**
     * Loads an asset index with the given ID.
     * Note that the asset index ID is independent from Minecraft version; several versions of Minecraft may share the same asset index.
     * For example, Minecraft versions "1.8" through "1.8.9" use the "1.8" asset index.
     * @param id - The id of the asset index to load.
     * @return
     * @throws IOException
     */
    private static AssetIndex loadAssetIndex( String id ) throws IOException {
        AssetIndex i = gson.fromJson( Files.newBufferedReader( Paths.get( ASSETS_DIRECTORY, id + ".json" ) ), AssetIndex.class );
        i.validate();
        
        assets.put( id, i );
        return i;
    }
}
