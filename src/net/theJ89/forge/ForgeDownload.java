package net.theJ89.forge;

import net.theJ89.mmm.SideCompat;

public class ForgeDownload {
    private SideCompat type;
    private String     url;
    private Long       size;
    private String     md5;
    private String     sha1;
    
    public ForgeDownload() {}
    
    public ForgeDownload( final SideCompat type, final String url, final Long size, final String md5, final String sha1 ) {
        this.type = type;
        this.url  = url;
        this.size = size;
        this.md5  = md5;
        this.sha1 = sha1;
    }
    
    public void setType( final SideCompat type ) {
        this.type = type;
    }
    
    public SideCompat getType() {
        return this.type;
    }
    
    public void setURL( final String url ) {
        this.url = url;
    }
    
    public String getURL() {
        return this.url;
    }
    
    public void setSize( Long size ) {
        this.size = size;
    }
    
    public Long getSize() {
        return this.size;
    }
    
    public void setMD5( final String md5 ) {
        this.md5 = md5;
    }
    
    public String getMD5() {
        return this.md5;
    }
    
    public void setSHA1( final String sha1 ) {
        this.sha1 = sha1;
    }
    
    public String getSHA1() {
        return this.sha1;
    }
}
