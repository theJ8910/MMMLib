package net.minecraft.launcher.updater;

public class AssetIndexInfo extends DownloadInfo {
    private long totalSize;
    private String id;
    
    //Unused in version info JSON
    //private boolean known;
    
    public long getTotalSize() {
        return this.totalSize;
    }
    
    public String getId() {
        return this.id;
    }
}
