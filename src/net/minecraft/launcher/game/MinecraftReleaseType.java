package net.minecraft.launcher.game;

import java.util.HashMap;
import java.util.Map;

public enum MinecraftReleaseType {
    SNAPSHOT( "snapshot", 3 ),
    RELEASE(  "release",  2 ),
    OLD_BETA( "beta",     1 ),
    OLD_ALPHA("alpha",    0 );
    
    private String name;
    private int    id;
    
    private static Map< String, MinecraftReleaseType > LOOKUP_NAME;
    private static MinecraftReleaseType[] LOOKUP_ID = new MinecraftReleaseType[4];
    static {
        LOOKUP_NAME = new HashMap< String, MinecraftReleaseType >();
        
        for( MinecraftReleaseType type : MinecraftReleaseType.values() ) {
            LOOKUP_NAME.put( type.name, type );
            LOOKUP_ID[ type.id ] = type;
        }
    }
    
    MinecraftReleaseType( final String name, final int id ) {
        this.name = name;
        this.id   = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getID() {
        return this.id;
    }
    
    public static MinecraftReleaseType get( final String name ) {
        return LOOKUP_NAME.get( name );
    }
    
    public static MinecraftReleaseType get( final int id ) {
        return LOOKUP_ID[ id ];
    }
}
