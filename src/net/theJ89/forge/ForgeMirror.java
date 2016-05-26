package net.theJ89.forge;

import java.util.Objects;

public class ForgeMirror {
    private String name;
    private String imageURL;
    private String clickURL;
    private String url;
    
    public ForgeMirror( final String name, final String imageURL, final String clickURL, final String url ) {
        this.name     = name;
        this.imageURL = imageURL;
        this.clickURL = clickURL;
        this.url      = url;
    }
    
    public void setName( final String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setImageURL( final String imageURL ) {
        this.imageURL = imageURL;
    }
    
    public String getImageURL() {
        return this.imageURL;
    }
    
    public void setClickURL( final String clickURL ) {
        this.clickURL = clickURL;
    }
    
    public String getClickURL() {
        return this.clickURL;
    }
    
    public void setURL( final String url ) {
        this.url = url;
    }
    
    public String getURL() {
        return this.url;
    }
    
    @Override
    public boolean equals( Object obj ) {
        if( obj == null || !( obj instanceof ForgeMirror ) )
            return false;
        
        ForgeMirror other = (ForgeMirror)obj;
        return Objects.equals( this.name,     other.name     ) &&
               Objects.equals( this.imageURL, other.imageURL ) &&
               Objects.equals( this.clickURL, other.clickURL ) &&
               Objects.equals( this.url,      other.url      );
    }
}
