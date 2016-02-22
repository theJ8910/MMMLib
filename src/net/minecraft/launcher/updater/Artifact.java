package net.minecraft.launcher.updater;

public class Artifact extends DownloadInfo {
    private static final String DOWNLOAD_BASE_URL = "https://libraries.minecraft.net/";
    private String path;
    
    public String getPath() {
        return this.path;
    }
    
    public void validate( String path ) {
        super.validate();
        
        if( this.path == null )
            throw new RuntimeException( "path is null." );
        if( !this.path.equals( path ) )
            throw new RuntimeException( "path (" + this.path + ") is different from expected path ( " + path + " )." );
        String url = DOWNLOAD_BASE_URL + path;
        if( !this.url.toString().equals( url ) )
            throw new RuntimeException( "url (" + this.url + ") is different from expected url ( " + url + " )" );
    }
}
