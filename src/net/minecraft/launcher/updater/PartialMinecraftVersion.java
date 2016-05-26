package net.minecraft.launcher.updater;

import java.util.Date;
import java.util.Objects;

import net.minecraft.launcher.game.MinecraftReleaseType;

public class PartialMinecraftVersion {
    private String               id;          //Version identifier (e.g. 1.7.10,)
    private MinecraftReleaseType type;        //Type of release (alpha, beta, release, or snapshot)
    private Date                 time;        //Last time this version was updated
    private Date                 releaseTime; //When this version was first released
    private String               url;         //URL to download extended information about this version
    
    public PartialMinecraftVersion() {}
    
    public PartialMinecraftVersion( final String id, final MinecraftReleaseType type, final Date time, final Date releaseTime, final String url ) {
        this.id          = id;
        this.type        = type;
        this.time        = time;
        this.releaseTime = releaseTime;
        this.url         = url;
    }
    
    public String getID() {
        return this.id;
    }
    
    public MinecraftReleaseType getType() {
        return this.type;
    }
    
    public Date getTime() {
        return this.time;
    }
    
    public Date getReleaseTime() {
        return this.releaseTime;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    @Override
    public boolean equals(Object obj) {
        if( obj == null || !(obj instanceof PartialMinecraftVersion ) )
            return false;
        PartialMinecraftVersion other = (PartialMinecraftVersion)obj;
        
        return Objects.equals( this.id,          other.id          ) &&
               Objects.equals( this.type,        other.type        ) &&
               Objects.equals( this.time,        other.time        ) &&
               Objects.equals( this.releaseTime, other.releaseTime ) &&
               Objects.equals( this.url,         other.url         );
    }
}
