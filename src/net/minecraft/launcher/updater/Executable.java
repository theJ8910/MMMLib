package net.minecraft.launcher.updater;

public class Executable extends DownloadInfo {
    private static final String DOWNLOAD_BASE_URL = "https://launcher.mojang.com/mc/game/";
    
    public void validate( String id, DownloadType key ) {
        super.validate();
        
        //Make sure the provided URL matches what we expect it to be.
        String downloadType = key.name().toLowerCase();
        String url = DOWNLOAD_BASE_URL + id + "/" + downloadType + "/" + this.sha1 + "/" + downloadType + ( key != DownloadType.WINDOWS_SERVER ? ".jar" : ".exe" );
        if( !this.url.toString().equals( url ) )
            throw new RuntimeException( "url (" + this.url + ") doesn't match expected url (" + url + ")." );
    }
}
