package net.minecraft.launcher.updater;

import java.net.URL;

public class DownloadInfo {
    protected URL    url;
    protected String sha1;
    protected int    size;
    
    public URL getURL() {
        return this.url;
    }
    
    public String getSha1() {
        return this.sha1;
    }
    
    public int getSize() {
        return this.size;
    }

    public void validate() {
        if( this.url == null )
            throw new RuntimeException( "url is null." );
        
        if( this.sha1 == null )
            throw new RuntimeException( "sha1 is null." );
    }
}
