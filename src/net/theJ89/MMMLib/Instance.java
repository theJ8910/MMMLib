package net.theJ89.MMMLib;

import java.nio.file.Path;

import net.theJ89.util.Size2D;

public class Instance {
    //Basic instance properties
    private String  name;
    private String  minecraftVersion;
    private Side    side;
    private Path    directory;
    
    //Per-instance JVM arguments
    private Long    initialHeapSize;
    private Long    maxHeapSize;
    private Long    nurserySize;
    private Long    metaspaceSize;
    
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
}
