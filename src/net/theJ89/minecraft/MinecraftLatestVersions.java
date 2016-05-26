package net.theJ89.minecraft;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.launcher.game.MinecraftReleaseType;

public class MinecraftLatestVersions {
    private String snapshot;
    private String release;
    private String beta;
    private String alpha;
    
    public MinecraftLatestVersions() {
        this.snapshot = null;
        this.release  = null;
        this.beta     = null;
        this.alpha    = null;
    }
    
    public MinecraftLatestVersions( final String snapshot, final String release, final String beta, final String alpha ) {
        this.snapshot = snapshot;
        this.release  = release;
        this.beta     = beta;
        this.alpha    = alpha;
    }
    
    public void setSnapshot( final String snapshot ) {
        this.snapshot = snapshot;
    }
    
    /*
     * Returns the name of the latest snapshot.
     */
    public String getSnapshot() {
        return this.snapshot;
    }
    
    public void setRelease( final String release ) {
        this.release = release;
    }
    
    /*
     * Returns the name of the latest release.
     */
    public String getRelease() {
        return this.release;
    }
    
    public void setBeta( final String beta ) {
        this.beta = beta;
    }
    
    /*
     * Returns the name of the latest beta.
     */
    public String getBeta() {
        return this.beta;
    }
    
    public void setAlpha( final String alpha ) {
        this.alpha = alpha;
    }
    
    /*
     * Returns the name of the latest alpha.
     */
    public String getAlpha() {
        return this.alpha;
    }
    
    public void set( final MinecraftReleaseType type, final String name ) {
        switch( type ) {
        case SNAPSHOT:
            this.snapshot = name;
            break;
        case RELEASE:
            this.release  = name;
            break;
        case OLD_BETA:
            this.beta     = name;
            break;
        case OLD_ALPHA:
            this.alpha    = name;
            break;
        }
    }
    
    /**
     * Returns the name of the latest version for the given release type
     * @param type
     * @return
     */
    public String get( final MinecraftReleaseType type ) {
        switch( type ) {
        case SNAPSHOT:
            return this.snapshot;
        case RELEASE:
            return this.release;
        case OLD_BETA:
            return this.beta;
        case OLD_ALPHA:
            return this.alpha;
        default:
            return null;
        }
    }
    
    /**
     * Returns a map of release type -> latest version name.
     * @return
     */
    public Map< MinecraftReleaseType, String > getAll() {
        Map< MinecraftReleaseType, String > map = new EnumMap< MinecraftReleaseType, String >( MinecraftReleaseType.class );
        
        map.put( MinecraftReleaseType.SNAPSHOT,  this.snapshot );
        map.put( MinecraftReleaseType.RELEASE,   this.release  );
        map.put( MinecraftReleaseType.OLD_BETA,  this.beta     );
        map.put( MinecraftReleaseType.OLD_ALPHA, this.alpha    );
        
        return map;
    }
    
    @Override
    public boolean equals( Object obj ) {
        if( obj == null || !( obj instanceof MinecraftLatestVersions ) )
            return false;
        
        MinecraftLatestVersions other = (MinecraftLatestVersions)obj;
        return Objects.equals( this.snapshot, other.snapshot ) &&
               Objects.equals( this.release,  other.release  ) &&
               Objects.equals( this.beta,     other.beta     ) &&
               Objects.equals( this.alpha,    other.alpha    );
    }
}
