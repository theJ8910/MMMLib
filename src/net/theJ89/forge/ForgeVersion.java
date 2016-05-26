package net.theJ89.forge;

import java.time.ZonedDateTime;

public class ForgeVersion {
    private String        name;
    private ZonedDateTime time;
    
    public ForgeVersion( final String name, final ZonedDateTime time ) {
        this.name = name;
        this.time = time;
    }
    
    public void setName( final String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void getTime( final ZonedDateTime time ) {
        this.time = time;
    }
    
    public ZonedDateTime getTime() {
        return this.time;
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
}
