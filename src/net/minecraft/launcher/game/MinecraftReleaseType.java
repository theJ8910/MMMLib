package net.minecraft.launcher.game;

import java.util.HashMap;
import java.util.Map;

public enum MinecraftReleaseType {
    SNAPSHOT( "snapshot" ),
    RELEASE(  "release"  ),
    OLD_BETA( "beta"     ),
    OLD_ALPHA("alpha"    );
    
    private String name;
    
    private static Map< String, MinecraftReleaseType > LOOKUP;
    static {
        LOOKUP = new HashMap< String, MinecraftReleaseType >();
        
        for( MinecraftReleaseType type : MinecraftReleaseType.values() )
            LOOKUP.put( type.name, type );
    }
    
    MinecraftReleaseType( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public static MinecraftReleaseType get( String name ) {
        return LOOKUP.get( name );
    }
}
