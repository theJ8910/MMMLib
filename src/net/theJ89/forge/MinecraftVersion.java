package net.theJ89.forge;

public class MinecraftVersion {
    private String name;
    private String latest;
    private String recommended;
    
    public MinecraftVersion( final String name, final String latest, final String recommended ) {
        this.name        = name;
        this.latest      = latest;
        this.recommended = recommended;
    }
    
    public void setName( final String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setLatest( final String latest ) {
        this.latest = latest;
    }
    
    public String getLatest() {
        return this.latest;
    }
    
    public void setRecommended( final String recommended ) {
        this.recommended = recommended;
    }
    
    public String getRecommended() {
        return this.recommended;
    }
}
