package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.properties.PropertyMap;

public class User {
    private int         id;
    private PropertyMap properties;
    
    public int getId() {
        return this.id;
    }
    
    public PropertyMap getProperties() {
        return this.properties;
    }
}
