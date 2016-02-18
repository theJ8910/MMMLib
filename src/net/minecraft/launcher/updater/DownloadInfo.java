package net.minecraft.launcher.updater;

import java.net.URL;

public class DownloadInfo {
    private URL    url;
    private String sha1;
    private int    size;
    
    public URL getURL() {
        return this.url;
    }
    
    public String getSha1() {
        return this.sha1;
    }
    
    public int getSize() {
        return this.size;
    }
}
