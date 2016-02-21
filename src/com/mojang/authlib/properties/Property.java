package com.mojang.authlib.properties;

public class Property {
    private String name;
    private String value;
    private String signature;
    
    public Property( String name, String value ) {
        this( name, value, null );
    }
    
    public Property( String name, String value, String signature ) {
        this.name      = name;
        this.value     = value;
        this.signature = signature;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setValue( String value ) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public void setSignature( String signature ) {
        this.signature = signature;
    }
    
    public String getSignature() {
        return this.signature;
    }
}
