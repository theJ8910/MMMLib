package net.minecraft.launcher.updater;

import java.util.Date;

import net.minecraft.launcher.game.MinecraftReleaseType;

public class PartialMinecraftVersion {
    private String               id;          //Version identifier (e.g. 1.7.10,)
    private MinecraftReleaseType type;        //Type of release (alpha, beta, release, or snapshot)
    private Date                 time;        //Last time this version was updated
    private Date                 releaseTime; //When this version was first released
    private String               url;         //URL to download extended information about this version
    
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
}
