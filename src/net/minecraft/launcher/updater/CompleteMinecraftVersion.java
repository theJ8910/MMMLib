package net.minecraft.launcher.updater;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.launcher.game.MinecraftReleaseType;

public class CompleteMinecraftVersion {
    private String                        id;
    private MinecraftReleaseType          type;
    private String                        assets;
    private AssetIndexInfo                assetIndex;
    private Map<DownloadType, Executable> downloads = new EnumMap< DownloadType, Executable >( DownloadType.class );
    private List<Library>                 libraries;
    private String                        mainClass;
    private String                        minecraftArguments;
    private int                           minimumLauncherVersion;
    private String                        inheritsFrom;
    private String                        jar;
    private Date                          time;
    private Date                          releaseTime;
    
    
    //Note: currently not present within any version info .json:
    //private String incompatibilityReason;
    //private List<CompatibilityRule> compatibilityRules;
    //private CompleteMinecraftVersion savableVersion;
    
    public String getId() {
        return this.id;
    }
    
    public MinecraftReleaseType getType() {
        return this.type;
    }
    
    public AssetIndexInfo getAssetIndex() {
        return this.assetIndex;
    }
    
    public Map<DownloadType, Executable> getDownloads() {
        return this.downloads;
    }
    
    public List<Library> getLibraries() {
        return this.libraries;
    }
    
    public String getMainClass() {
        return this.mainClass;
    }
    
    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }
    
    public int getMinimumLauncherVersion() {
        return this.minimumLauncherVersion;
    }
    
    public String getInheritsFrom() {
        return this.inheritsFrom;
    }
    
    public String getJar() {
        return this.jar;
    }
    
    /**
     * Returns the name of the version whose .jar file should be used to launch this version.
     * If a jar override hasn't been set, returns the name of this version.
     * @return
     */
    public String resolveJar() {
        if( this.jar == null )
            return this.id;
        return this.jar;
    }
    
    public Date getTime() {
        return this.time;
    }
    
    public Date getReleaseTime() {
        return this.releaseTime;
    }
    
    public String getAssets() {
        return this.assets;
    }
    
    public boolean hasClient() {
        return this.downloads.containsKey( DownloadType.CLIENT );
    }
    
    public boolean hasServer() {
        return this.downloads.containsKey( DownloadType.SERVER );
    }
    
    public boolean hasWindowsServer() {
        return this.downloads.containsKey( DownloadType.WINDOWS_SERVER );
    }
    
    public void inherit( CompleteMinecraftVersion parent ) {
        if( this.type == null )
            this.type = parent.type;
        if( this.assets == null )
            this.assets = parent.assets;
        if( this.assetIndex == null )
            this.assetIndex = parent.assetIndex;
        
        if( this.downloads == null ) {
            this.downloads = parent.downloads;
        } else if( parent.downloads != null ) {
            Map< DownloadType, Executable > newDownloads = new EnumMap< DownloadType, Executable >( parent.downloads ); 
            newDownloads.putAll( this.downloads );
            this.downloads = newDownloads;
        }
        
        if( this.libraries == null ) {
            this.libraries = parent.libraries;
        } else if( parent.libraries != null ) {
            List<Library> newLibraries = new ArrayList< Library >( parent.libraries );
            newLibraries.addAll( this.libraries );
            this.libraries = newLibraries;
        }
        
        if( this.mainClass == null )
            this.mainClass = parent.mainClass;
        
        if( this.minecraftArguments == null )
            this.minecraftArguments = parent.minecraftArguments;
        
        if( this.inheritsFrom == null )
            this.inheritsFrom = parent.id;
        if( this.jar == null )
            this.jar = parent.jar;
        
        if( this.time == null )
            this.time = parent.time;
        if( this.releaseTime == null )
            this.releaseTime = parent.releaseTime;
    }
    
    /**
     * Checks this object to ensure it conforms to its schema.
     * Recursively calls validate() on each of its members as well.
     * Throws a RuntimeException if the object violates its schema.
     * Typically this should be called on an object after using Gson to deserialize it.
     * @param expectedID - The ID we expect this CompleteMinecraftVersion to have.
     */
    public void validate( String expectedID ) {
        if( this.id == null )
            throw new RuntimeException( "ID is null." );
        if( !this.id.equals( expectedID ) )
            throw new RuntimeException( "ID is " + this.id + ", not " + expectedID + "!"  );
        if( this.type == null )
            throw new RuntimeException( "Type is null." );
        
        if( this.assetIndex == null )
            throw new RuntimeException( "Asset index information is null." );
        if( this.assets == null )
            throw new RuntimeException( "Assets id is null." );
        this.assetIndex.validate( this.assets );
        
        if( this.downloads == null )
            throw new RuntimeException( "Downloads is null." );
        if( !this.hasClient() )
            throw new RuntimeException( "No client download could be found." );
        for( Entry<DownloadType,Executable> download : this.downloads.entrySet() )
            download.getValue().validate( this.id, download.getKey() );
        
        if( this.libraries == null )
            throw new RuntimeException( "Libraries is null." );
        for( Library library : this.libraries )
            library.validate();
        
        if( this.mainClass == null )
            throw new RuntimeException( "Main class is null." );
        if( this.minecraftArguments == null )
            throw new RuntimeException( "Minecraft arguments is null." );
        
        if( this.time == null )
            throw new RuntimeException( "Time is null." );
        if( this.releaseTime == null )
            throw new RuntimeException( "Release time is null." );
    }
}
