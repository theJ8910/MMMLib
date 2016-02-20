package net.theJ89.MMMLib;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.mojang.authlib.UserType;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.launcher.updater.CompleteMinecraftVersion;
import net.minecraft.launcher.updater.Library;
import net.theJ89.util.OperatingSystem;
import net.theJ89.util.Platform;
import net.theJ89.util.Size;
import net.theJ89.util.Size2D;
import net.theJ89.util.Target;

public class MinecraftLauncher {
    //The Minecraft launcher uses these values by default
    private static long DEFAULT_NURSERY_SIZE  = 128 * Size.MEGABYTE;
    private static long DEFAULT_MAX_HEAP_SIZE = Platform.getBitness() == 32 ? 512 * Size.MEGABYTE : Size.GIGABYTE;
    
    //For versions of Java where this isn't set manually, 128 MB seems to be sufficient to handle an instance with ~200 mods.
    private static long DEFAULT_PERMGEN_SIZE = 128 * Size.MEGABYTE;
    
    //Where Minecraft version executables and info are stored
    private static String VERSIONS_DIRECTORY = "versions";
    
    //Directory we can find the natives in (relative to the instance directory)
    private static String NATIVES_DIRECTORY = "natives";
    
    //Directory we can find the assets in (relative to the instance directory)
    private static String ASSETS_DIRECTORY = "assets";
    
    //Virtual assets root (inside of ASSETS_DIRECTORY) for versions that need it
    private static String ASSETS_VIRTUAL_DIRECTORY = "virtual";
    
    private Instance                 instance;
    private UserData                 userdata;
    private CompleteMinecraftVersion version;
    private Path                     directory;
    private boolean                  demo;
    
    public MinecraftLauncher( Instance instance, UserData userdata ) throws IOException {
        this.instance  = instance;
        this.userdata  = userdata;
        this.version   = MinecraftVersions.get( instance.getMinecraftVersion() );
        this.directory = this.instance.getDirectory().toAbsolutePath();
        this.demo      = false;
    }
    
    //TODO: Actually make this launch a Minecraft instance
    public void launch() throws IOException {
        Target target = Platform.getTarget();
        JavaLauncher l = new JavaLauncher();
        
        //The Minecraft launcher always passes these options
        l.setUseConcMarkSweepGC( true );
        l.setUseCMSIncrementalMode( true );
        l.setUseAdaptiveSizePolicy( true );
        
        //Set nursery, init heap size, and max heap size.
        Long nurserySize = this.instance.getNurserySize();
        l.setNurserySize( nurserySize != null ? nurserySize : DEFAULT_NURSERY_SIZE );
        
        Long initialHeapSize = this.instance.getInitialHeapSize();
        l.setInitialHeapSize( initialHeapSize );
        
        Long maxHeapSize = this.instance.getMaxHeapSize();
        l.setMaxHeapSize( maxHeapSize != null ? maxHeapSize : DEFAULT_MAX_HEAP_SIZE );
        
        //Set metaspace size if the instance overrides it
        Long metaspaceSize = this.instance.getMetaspaceSize();
        if( metaspaceSize != null )
            l.setMetaspaceSize( metaspaceSize );
        //Manually set permgen if we're using something older than Java 1.8.
        else if( !Platform.getJavaVersion().atLeast( JavaVersion.JAVA_1_8 ) )
            l.setMetaspaceSize( DEFAULT_PERMGEN_SIZE );
        
        //Set class paths
        //The Minecraft instance .jar and any Java libraries it needs should be listed here.
        List<String> classPaths = new ArrayList< String >();
        classPaths.add( Paths.get( VERSIONS_DIRECTORY ).resolve( this.version.getId() ).resolve( this.version.getId() + ".jar" ).toString() );
        
        for( Library library : this.version.getLibraries() )
            if( !library.isNative() )
                classPaths.add( library.getPath( target ).toString() );
        
        l.setClassPaths( classPaths );
        
        //Set native library dir
        l.setLibraryPathsIL( NATIVES_DIRECTORY );
        
        //Set classname
        //"net.minecraft.client.Minecraft"
        l.setClassname( this.version.getMainClass() );
        
        //Add Minecraft arguments
        List<String> arguments = getArguments();
        
        //Include desired game resolution if set
        Size2D resolution = this.instance.getResolution();
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
        
        l.setArguments( arguments );
        
        //Set working directory and environment if necessary
        //2013-09-19T15:52:37+00:00
        Calendar c = Calendar.getInstance( TimeZone.getTimeZone("UTC") );
        c.set(2013, 9, 19, 15, 52, 37);
        if( Platform.getOS() == OperatingSystem.WINDOWS && this.instance.getSide() == Side.CLIENT && this.version.getReleaseTime().before( c.getTime() ) ) {
            l.setEnvironmentIL( "APPDATA", this.directory.toString() );
            l.setWorkingDirectory( this.directory.resolve( ".minecraft" ) );
        } else {
            l.setWorkingDirectory( this.directory );
        }
        
        //Run Minecraft
        l.launch();
    }
    
    private List<String> getArguments() {
        Path assetDir = this.directory.resolve( ASSETS_DIRECTORY );
        String assetID = this.version.getAssetIndex().getId();
        
        //Parse the argument string to get a list of arguments
        List<String> arguments = JavaLauncher.parseArguments( this.version.getMinecraftArguments() );
        
        //These arguments may contain placeholders (that take the form "${key}") that need to be substituted
        //with various values before being sent to Minecraft.
        //The Minecraft launcher handles this by creating a translation table and running each argument through
        //a string substitutor utilizing this table. We take the same approach below.
        Map<String,String> m = new HashMap<String, String>();
        
        //Temporary until I get this sorted out.
        //The serializers in the launcher suggest these are JSON arrays.
        m.put( "user_properties",   "[]" ); //Note: this is described as "legacy" in the launcher
        m.put( "user_property_map", "[]" );
        
        //All of these placeholders are related to user account information / authentication.
        if( this.userdata != null ) {
            String accessToken = UUIDTypeAdapter.fromUUID( this.userdata.getAccessToken() );
            String playerUUID  = UUIDTypeAdapter.fromUUID( this.userdata.getUUID() );
            m.put( "auth_access_token", accessToken );
            m.put( "auth_session",      String.format( "token:%s:%s", accessToken, playerUUID ) );
            //m.put( "auth_session",    accessToken ); //Non-Yggdrasil authentication
            m.put( "auth_player_name",  this.userdata.getUsername() );
            m.put( "auth_uuid",         playerUUID );
            m.put( "user_type",         this.userdata.getUserType().getName() );
        //If user data isn't available (e.g. no login), we'll fill these in with defaults.
        } else {
            String defaultUUID = UUIDTypeAdapter.fromUUID( new UUID( 0L, 0L ) );
            m.put( "auth_access_token", defaultUUID );
            m.put( "auth_session",      "-" );
            m.put( "auth_player_name", "Player" );
            m.put( "auth_uuid",         defaultUUID );
            m.put( "user_type",         UserType.LEGACY.getName() );
        }
        
        m.put( "profile_name", this.instance.getName() );
        m.put( "version_name", this.instance.getMinecraftVersion() );
        
        m.put( "game_directory", this.directory.toString() );
        m.put( "game_assets", assetDir.resolve( ASSETS_VIRTUAL_DIRECTORY ).resolve( assetID ).toString() ); //e.g. assets/virtual/1.7.10
        
        m.put( "assets_root", assetDir.toString() );
        m.put( "assets_index_name", assetID );
        
        m.put( "version_type", this.version.getType().getName() );
        
        //Perform the substitutions
        StrSubstitutor ss = new StrSubstitutor( m );
        for( int i = 0; i < arguments.size(); ++i )
            arguments.set( i, ss.replace( arguments.get( i ) ) );
        return arguments;
    }
    
    public void setDemo( boolean demo ) {
        this.demo = demo;
    }
    
    public boolean isDemo() {
        return this.demo;
    }
}
