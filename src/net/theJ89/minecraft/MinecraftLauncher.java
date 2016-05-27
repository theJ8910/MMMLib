package net.theJ89.minecraft;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.UserType;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.launcher.updater.CompleteMinecraftVersion;
import net.minecraft.launcher.updater.Library;
import net.theJ89.mmm.Proxy;
import net.theJ89.mmm.Side;
import net.theJ89.mmm.UserData;
import net.theJ89.util.JavaLauncher;
import net.theJ89.util.JavaVersion;
import net.theJ89.util.Platform;
import net.theJ89.util.Size;
import net.theJ89.util.Size2D;
import net.theJ89.util.StrSubstitutor;

public class MinecraftLauncher {
    //The Minecraft launcher uses these values by default
    private static long DEFAULT_NURSERY_SIZE  = 128 * Size.MEGABYTE;
    private static long DEFAULT_MAX_HEAP_SIZE = Platform.getBitness() == 32 ? 512 * Size.MEGABYTE : Size.GIGABYTE;
    
    //For versions of Java where this isn't set manually, 128 MB seems to be sufficient to handle an instance with ~200 mods.
    private static long DEFAULT_PERMGEN_SIZE = 128 * Size.MEGABYTE;
    
    private CompleteMinecraftVersion version;
    private Side                     side;
    private UserData                 userdata;
    
    private Path                     gameDirectory;
    private Path                     versionsDirectory;
    private Path                     librariesDirectory;
    private Path                     nativesDirectory;
    private Path                     assetsDirectory;
    
    private String                   profileName;
    
    private Long                     nurserySize;
    private Long                     initialHeapSize;
    private Long                     maxHeapSize;
    private Long                     metaspaceSize;
    
    private Size2D                   resolution;
    
    private String                   serverJar;
    
    private boolean                  demo;
    
    public MinecraftLauncher() {
    }
    
    public void setVersion( final CompleteMinecraftVersion version ) {
        this.version = version;
    }
    
    public CompleteMinecraftVersion getVersion() {
        return this.version;
    }
    
    public void setSide( final Side side ) {
        this.side = side;
    }
    
    public Side getSide() {
        return this.side;
    }
    
    public void setUserData( final UserData userdata ) {
        this.userdata = userdata;
    }
    
    public UserData getUserData() {
        return this.userdata;
    }
    
    public void setGameDirectory( final Path gameDirectory ) {
        this.gameDirectory = gameDirectory;
    }
    
    public Path getGameDirectory() {
        return this.gameDirectory;
    }
    
    public void setVersionsDirectory( final Path versionsDirectory ) {
        this.versionsDirectory = versionsDirectory;
    }
    
    public Path getVersionsDirectory() {
        return this.versionsDirectory;
    }
    
    public void setLibrariesDirectory( final Path librariesDirectory ) {
        this.librariesDirectory = librariesDirectory;
    }
    
    public Path getLibrariesDirectory() {
        return this.librariesDirectory;
    }
    
    public void setNativesDirectory( final Path nativesDirectory ) {
        this.nativesDirectory = nativesDirectory;
    }
    
    public Path getNativesDirectory() {
        return this.nativesDirectory;
    }
    
    public void setAssetsDirectory( final Path assetsDirectory ) {
        this.assetsDirectory = assetsDirectory;
    }
    
    public Path getAssetsDirectory() {
        return this.assetsDirectory;
    }
    
    public void setProfileName( final String profileName ) {
        this.profileName = profileName;
    }
    
    public String getProfileName() {
        return this.profileName;
    }
    
    public void setNurserySize( final Long nurserySize ) {
        this.nurserySize = nurserySize;
    }
    
    public Long getNurserySize() {
        return this.nurserySize;
    }
    
    public void setInitialHeapSize( final Long initialHeapSize ) {
        this.initialHeapSize = initialHeapSize;
    }
    
    public Long getInitialHeapSize() {
        return this.initialHeapSize;
    }
    
    public void setMaxHeapSize( final Long maxHeapSize ) {
        this.maxHeapSize = maxHeapSize;
    }
    
    public Long getMaxHeapSize() {
        return this.maxHeapSize;
    }
    
    public void setMetaspaceSize( final Long metaspaceSize ) {
        this.metaspaceSize = metaspaceSize;
    }
    
    public Long getMetaspaceSize() {
        return this.metaspaceSize;
    }
    
    public void setResolution( final Size2D resolution ) {
        this.resolution = resolution;
    }
    
    public Size2D getResolution() {
        return this.resolution;
    }
    
    public void setServerJar( final String serverJar ) {
        this.serverJar = serverJar;
    }
    
    public String getServerJar() {
        return this.serverJar;
    }
    
    public void launch() throws IOException {
        JavaLauncher l = new JavaLauncher();
        
        Side                     side    = this.side;
        CompleteMinecraftVersion version = this.version;
        
        Path gameDirectory      = this.gameDirectory;
        Path versionsDirectory  = this.versionsDirectory;
        Path librariesDirectory = this.librariesDirectory;
        Path nativesDirectory   = this.nativesDirectory;
        
        //The Minecraft launcher always passes these options
        l.setUseConcMarkSweepGC( true );
        l.setUseCMSIncrementalMode( true );
        l.setUseAdaptiveSizePolicy( true );
        
        //Set nursery, init heap size, and max heap size.
        Long nurserySize = this.nurserySize;
        l.setNurserySize( nurserySize != null ? nurserySize : DEFAULT_NURSERY_SIZE );
        
        Long initialHeapSize = this.initialHeapSize;
        l.setInitialHeapSize( initialHeapSize );
        
        Long maxHeapSize = this.maxHeapSize;
        l.setMaxHeapSize( maxHeapSize != null ? maxHeapSize : DEFAULT_MAX_HEAP_SIZE );
        
        //Set metaspace size if the instance overrides it
        Long metaspaceSize = this.metaspaceSize;
        if( metaspaceSize != null )
            l.setMetaspaceSize( metaspaceSize );
        //Manually set permgen if we're using something older than Java 1.8.
        else if( !Platform.getJavaVersion().atLeast( JavaVersion.JAVA_1_8 ) )
            l.setMetaspaceSize( DEFAULT_PERMGEN_SIZE );
        
        if( side == Side.CLIENT ) {
            //Set class paths
            //The Minecraft .jar and any Java libraries it needs should be listed here.
            List<String> classPaths = new ArrayList< String >();
            classPaths.add( MinecraftVersions.getVersionJar( versionsDirectory, version.resolveJar() ).toString() );
            
            for( Library library : version.getLibraries() )
                if( !library.isNative() )
                    classPaths.add( librariesDirectory.resolve( Library.getPathFromName( library.getName() ) ).toString() );
            
            l.setClassPaths( classPaths );
            
            //Set native library dir
            l.setLibraryPathsIL( nativesDirectory.toString() );
            
            //Set classname
            l.setClassName( version.getMainClass() );
                    
            //Build list of Minecraft arguments
            l.setArguments( getArguments() );
        } else {
            l.setJarName( this.serverJar );
            l.setArgumentsIL( "nogui" );
        }
        
        //Set working directory
        l.setWorkingDirectory( gameDirectory );
        
        //Run Minecraft
        l.launch();
    }
    
    private List<String> getArguments() {
        CompleteMinecraftVersion version         = this.version;
        UserData                 userdata        = this.userdata;
        String                   profileName     = this.profileName;
        
        String                   assetName       = version.getAssets();
        
        Path                     gameDirectory   = this.gameDirectory;
        Path                     assetsDirectory = this.assetsDirectory;
        Size2D                   resolution      = this.resolution;
        
        //Parse the argument string to get a list of arguments
        List<String> arguments = JavaLauncher.parseArguments( version.getMinecraftArguments() );
        
        //These arguments may contain placeholders (that take the form "${key}") that need to be substituted
        //with various values before being sent to Minecraft.
        //The Minecraft launcher handles this by creating a translation table and running each argument through
        //a string substitutor utilizing this table. We take the same approach below.
        Map<String,String> m = new HashMap<String, String>();
        
        //All of these placeholders are related to user account information / authentication.
        if( userdata != null ) {
            //Create both legacy and modern user property JSON.
            //User properties were represented differently in older versions of Minecraft.
            //We need to support both forms to be compatible with older versions.
            PropertyMap properties = userdata.getProperties();
            Gson gson;
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter( PropertyMap.class, new PropertyMap.LegacySerializer() );
            gson = gb.create();
            m.put( "user_properties",   gson.toJson( properties ) );
            
            gb = new GsonBuilder();
            gb.registerTypeAdapter( PropertyMap.class, new PropertyMap.Serializer() );
            gson = gb.create();
            m.put( "user_property_map", gson.toJson( properties ) );
                
            String accessToken = UUIDTypeAdapter.fromUUID( userdata.getAccessToken() );
            String playerUUID  = UUIDTypeAdapter.fromUUID( userdata.getUUID() );
            m.put( "auth_access_token", accessToken );
            m.put( "auth_session",      String.format( "token:%s:%s", accessToken, playerUUID ) );
            //m.put( "auth_session",    accessToken ); //Non-Yggdrasil authentication
            m.put( "auth_player_name",  userdata.getUsername() );
            m.put( "auth_uuid",         playerUUID );
            m.put( "user_type",         userdata.getUserType().getName() );
        //If user data isn't available (e.g. no login), we'll fill these in with defaults.
        } else {
            String defaultUUID = UUIDTypeAdapter.fromUUID( new UUID( 0L, 0L ) );
            m.put( "user_properties",   "{}" );
            m.put( "user_property_map", "[]" );
            m.put( "auth_access_token", defaultUUID );
            m.put( "auth_session",      "-" );
            m.put( "auth_player_name", "Player" );
            m.put( "auth_uuid",         defaultUUID );
            m.put( "user_type",         UserType.LEGACY.getName() );
        }
        
        m.put( "profile_name", profileName );
        m.put( "version_name", version.getId() );
        
        m.put( "game_directory", gameDirectory.toString() );
        m.put( "game_assets", assetsDirectory.resolve( MinecraftConstants.ASSETS_VIRTUAL_DIRECTORY ).resolve( assetName ).toString() ); //e.g. assets/virtual/1.7.10
        
        m.put( "assets_root", assetsDirectory.toString() );
        m.put( "assets_index_name", assetName );
        
        m.put( "version_type", version.getType().getName() );
        
        //Perform the substitutions
        StrSubstitutor ss = new StrSubstitutor( m );
        for( int i = 0; i < arguments.size(); ++i )
            arguments.set( i, ss.replace( arguments.get( i ) ) );
        
        //Include desired game resolution if set
        if( resolution != null ) {
            arguments.add( "--width" );
            arguments.add( Integer.toString( resolution.getWidth() ) );
            arguments.add( "--height" );
            arguments.add( Integer.toString( resolution.getHeight() ) );
        }
        
        //Pass proxy settings to Minecraft if any are set
        Proxy proxy = Proxy.get();
        if( proxy != null ) {
            arguments.add( "--proxyHost" );
            arguments.add( proxy.getHost() );
            arguments.add( "--proxyPort" );
            arguments.add( proxy.getPort() );
            
            //Pass username and password if the proxy requires it
            String proxyUsername = proxy.getUsername();
            if( proxyUsername != null ) {
                arguments.add( "--proxyUser" );
                arguments.add( proxyUsername );
                arguments.add( "--proxyPass" );
                arguments.add( proxy.getPassword() );
            }
        }
        
        //If demo mode is set, pass "--demo"
        if( this.demo )
            arguments.add( "--demo" );
        
        return arguments;
    }
    
    public void setDemo( boolean demo ) {
        this.demo = demo;
    }
    
    public boolean isDemo() {
        return this.demo;
    }
}
