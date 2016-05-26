package net.theJ89.mmm;

import java.util.HashSet;
import java.util.Set;

public class Package {
    private String         name;
    private Set< Package > dependencies;
    
    public Package() {
        this.name         = "";
        this.dependencies = new HashSet< Package >();
    }
    
    public void setName( final String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Set< Package > getDependencies() {
        return this.dependencies;
    }
    
    public void install() {
        //TODO
    }
    
    public void uninstall() {
        //TODO
    }
}
