package net.minecraft.launcher.updater;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launcher.game.MinecraftReleaseType;

public class CompleteMinecraftVersion {
    private String                          id;
    private MinecraftReleaseType            type;
    private AssetIndexInfo                  assetIndex;
    private Map<DownloadType, DownloadInfo> downloads = new EnumMap< DownloadType, DownloadInfo >( DownloadType.class );
    private List<Library>                   libraries;
    private String                          mainClass;
    private String                          minecraftArguments;
    private int                             minimumLauncherVersion;
    private Date                            time;
    private Date                            releaseTime;
    
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
    
    public Map<DownloadType, DownloadInfo> getDownloads() {
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
    
    public Date releaseTime() {
        return this.releaseTime;
    }
}
