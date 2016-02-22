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
    
    public void validate( String id ) {
        super.validate();
        
        if( this.id == null )
            throw new RuntimeException( "No ID." );
        if( !this.id.equals( id ) )
            throw new RuntimeException( "ID (" + this.id + ") doesn't match expected ID(" + id + ")." );
    }
}
