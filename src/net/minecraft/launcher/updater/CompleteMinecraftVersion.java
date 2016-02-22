package net.minecraft.launcher.updater;

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
    private Date                          time;
    private Date                          releaseTime;
    
    //Note: currently not present within any version info .json:
    //private String inheritsFrom;
    //private String incompatibilityReason;
    //private List<CompatibilityRule> compatibilityRules;
    //private String jar;
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
