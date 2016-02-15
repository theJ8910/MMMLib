package com.mojang.authlib.yggdrasil;

import java.util.UUID;

public class GameProfile {
    private final UUID id;
    private final String name;
    //private PropertyMap properties = new PropertyMap();
    private boolean legacy;
    
    public GameProfile( UUID id, String name ) {
        this.id   = id;
        this.name = name;
    }
    
    public UUID getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    /*
    public PropertyMap getPropertyMap() {
        return this.properties;
    }
    */
    
    public boolean isLegacy() {
        return this.legacy;
    }
}
