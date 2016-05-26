package net.theJ89.forge;

import java.util.List;

public class ForgeLibrary {
    private String comment;
    private String name;
    private String url;
    private List< String > checksums;
    private Boolean serverreq;
    private Boolean clientreq;
    
    public void setComment( final String comment ) {
        this.comment = comment;
    }
    
    public String getComment() {
        return this.comment;
    }
    
    public void setName( final String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setURL( final String url ) {
        this.url = url;
    }
    
    public String getURL() {
        return this.url;
    }
    
    public void setChecksums( final List< String > checksums ) {
        this.checksums = checksums;
    }
    
    public List< String > getChecksums() {
        return this.checksums;
    }
    
    public void setServerReq( final Boolean serverreq ) {
        this.serverreq = serverreq;
    }
    
    public boolean isServerReq() {
        return this.serverreq != null && this.serverreq;
    }
    
    public void setClientReq( final Boolean clientreq ) {
        this.clientreq = clientreq;
    }
    
    public boolean isClientReq() {
        return this.clientreq != null && this.clientreq;
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
}
