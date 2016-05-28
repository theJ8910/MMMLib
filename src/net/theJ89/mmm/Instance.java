package net.theJ89.mmm;

import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.launcher.updater.CompleteMinecraftVersion;
import net.theJ89.minecraft.MinecraftConstants;
import net.theJ89.minecraft.MinecraftLauncher;
import net.theJ89.minecraft.MinecraftVersions;
import net.theJ89.util.Size2D;

public class Instance {
    //Basic instance properties
    private String  name;
    private String  minecraftVersion;
    private Side    side;
    private Path    directory;
    
    //Per-instance JVM arguments
    private Long      initialHeapSize;
    private Long      maxHeapSize;
    private Long      nurserySize;
    private Long      metaspaceSize;
    
    //Game arguments
    private Size2D  resolution;
    
    public Instance( String name, String minecraftVersion, Side side, Path directory ) {
        this.name             = name;
        this.minecraftVersion = minecraftVersion;
        this.side             = side;
        this.directory        = directory;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setMinecraftVersion( String minecraftVersion ) {
        this.minecraftVersion = minecraftVersion;
    }
    
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }
    
    public void setSide( Side side ) {
        this.side = side;
    }
    
    public Side getSide() {
        return this.side;
    }
    
    public void setDirectory( Path path ) {
        this.directory = path;
    }
    
    public Path getDirectory() {
        return this.directory;
    }
    
    public void setInitialHeapSize( Long initialHeapSize ) {
        this.initialHeapSize = initialHeapSize;
    }
    
    public Long getInitialHeapSize() {
        return this.initialHeapSize;
    }
    
    public void setMaxHeapSize( Long maxHeapSize ) {
        this.maxHeapSize = maxHeapSize;
    }
    
    public Long getMaxHeapSize() {
        return this.maxHeapSize;
    }
    
    public void setNurserySize( Long nurserySize ) {
        this.nurserySize = nurserySize;
    }
    
    public Long getNurserySize() {
        return this.nurserySize;
    }
    
    public void setMetaspaceSize( Long metaspaceSize ) {
        this.metaspaceSize = metaspaceSize;
    }
    
    public Long getMetaspaceSize() {
        return this.metaspaceSize;
    }
    
    public Size2D getResolution() {
        return this.resolution;
    }
    
    public void launch( final String version, final UserData userdata ) throws IOException {
        MinecraftLauncher l = new MinecraftLauncher();
        Path directory = this.directory;
        
        l.setSide( this.side );
        l.setGameDirectory( directory );
        
        l.setNurserySize( this.nurserySize );
        l.setInitialHeapSize( this.initialHeapSize );
        l.setMaxHeapSize( this.maxHeapSize );
        l.setMetaspaceSize( this.metaspaceSize );
        
        //Launching the server's a lot simpler than launching the client.
        if( this.side == Side.CLIENT ) {
            CompleteMinecraftVersion cmv = MinecraftVersions.load( directory.resolve( MinecraftConstants.VERSIONS_DIRECTORY ), version );
            l.setVersion( cmv );
            l.setUserData( userdata );
            
            l.setVersionsDirectory( directory.resolve( MinecraftConstants.VERSIONS_DIRECTORY ) );
            l.setLibrariesDirectory( directory.resolve( MinecraftConstants.LIBRARIES_DIRECTORY ) );
            l.setNativesDirectory( directory.resolve( MinecraftConstants.NATIVES_DIRECTORY ) );
            l.setAssetsDirectory( directory.resolve( MinecraftConstants.ASSETS_DIRECTORY ) );
    
            l.setProfileName( this.name );
            
            l.setResolution( this.resolution );
        } else if( this.side == Side.SERVER ) {
            l.setServerJar( directory.resolve( "minecraft_server." + version + ".jar" ).toString() );
        }
        
        Process p = l.launch();
        try                            { p.waitFor();         }
        catch (InterruptedException e) { e.printStackTrace(); }
    }
}
