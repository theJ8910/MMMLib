package com.mojang.authlib;

import java.util.HashMap;
import java.util.Map;

public enum UserType {
    LEGACY("legacy"),
    MOJANG("mojang");
    
    private static final Map< String, UserType > BY_NAME;
    static {
        BY_NAME = new HashMap< String, UserType >();
        for( UserType value : UserType.values() )
            BY_NAME.put( value.name, value );
    }
    
    private final String name;
    
    UserType( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public static UserType byName( String name ) {
        return BY_NAME.get( name );
    }
}
