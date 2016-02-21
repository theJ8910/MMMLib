package com.mojang.authlib.yggdrasil;

import java.util.UUID;

import com.mojang.authlib.properties.PropertyMap;

public class GameProfile {
    private UUID        id;
    private String      name;
    private PropertyMap properties;
    private boolean     legacy;
    
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
    
    public PropertyMap getProperties() {
        return this.properties;
    }
    
    public boolean isLegacy() {
        return this.legacy;
    }
}
