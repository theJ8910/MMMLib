package net.theJ89.forge;

import java.time.OffsetDateTime;
import java.util.List;

public class ForgeVersionInfo {
    private String               id;
    private OffsetDateTime       time;
    private OffsetDateTime       releaseTime;
    private String               type;
    private String               minecraftArguments;
    private String               mainClass;
    private float                minimumLauncherVersion;
    private String               inheritsFrom;
    private String               jar;
    private List< ForgeLibrary > libraries;
    
    public void setID( final String id ) {
        this.id = id;
    }
    
    public String getID() {
        return this.id;
    }
    
    public void setTime( final OffsetDateTime time ) {
        this.time = time;
    }
    
    public OffsetDateTime getTime() {
        return this.time;
    }
    
    public void setReleaseTime( final OffsetDateTime releaseTime ) {
        this.releaseTime = releaseTime;
    }
    
    public OffsetDateTime getReleaseTime() {
        return this.releaseTime;
    }
    
    public void setType( final String type ) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setMinecraftArguments( final String minecraftArguments ) {
        this.minecraftArguments = minecraftArguments;
    }
    
    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }
    
    public void setMainClass( final String mainClass ) {
        this.mainClass = mainClass;
    }
    
    public String getMainClass() {
        return this.mainClass;
    }
    
    public void setMinimumLauncherVersion( final float minimumLauncherVersion ) {
        this.minimumLauncherVersion = minimumLauncherVersion;
    }
    
    public float getMinimumLauncherVersion() {
        return this.minimumLauncherVersion;
    }
    
    public void setInheritsFrom( final String inheritsFrom ) {
        this.inheritsFrom = inheritsFrom;
    }
    
    public String getInheritsFrom() {
        return this.inheritsFrom;
    }
    
    public void setJar( final String jar ) {
        this.jar = jar;
    }
    
    public String getJar() {
        return this.jar;
    }
    
    public void setLibraries( List< ForgeLibrary > libraries ) {
        this.libraries = libraries;
    }
    
    public List< ForgeLibrary > getLibraries() {
        return this.libraries;
    }
    
    /**
     * Finds and returns the library name for this version of Minecraft Forge. 
     * Returns null if it can't be found.
     * @return
     */
    public String getForgeLibraryName() {
        for( ForgeLibrary fl : this.libraries ) {
            String name = fl.getName();
            if( name.startsWith( "net.minecraftforge:forge:" ) )
                return name;
        }
        return null;
    }
}
