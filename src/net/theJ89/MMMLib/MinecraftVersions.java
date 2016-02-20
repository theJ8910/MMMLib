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

import net.minecraft.launcher.updater.CompleteMinecraftVersion;

public class MinecraftVersions {
    private static final Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( Date.class, new DateTypeAdapter() );
        gb.registerTypeAdapterFactory( new LowerCaseEnumTypeAdapterFactory() );
        gson = gb.create();
    }
    
    private static final Map< String, CompleteMinecraftVersion > versions = new HashMap< String, CompleteMinecraftVersion >();
    
    /**
     * Return information about a version of Minecraft.
     * Loads the information if it isn't already.
     * @param version - The version of Minecraft to get information from (e.g. "1.5.2", "1.6.4", "1.7.10")
     * @return Information about the requested Minecraft version.
     * @throws IOException
     */
    public static CompleteMinecraftVersion get( String version ) throws IOException {
        CompleteMinecraftVersion v = versions.get( version );
        if( v == null )
            v = load( version );
        return v;
    }
    
    /**
     * Loads information about the requested Minecraft version.
     * @param version
     * @return
     * @throws IOException
     */
    private static CompleteMinecraftVersion load( String version ) throws IOException {
        CompleteMinecraftVersion v = gson.fromJson( Files.newBufferedReader( Paths.get( "versions", version, "info.json"   ) ), CompleteMinecraftVersion.class );
        //AssetIndex               i = gson.fromJson( Files.newBufferedReader( Paths.get( "versions", version, "assets.json" ) ), AssetIndex.class               );
        
        versions.put( version, v );
        return v;
    }
}
