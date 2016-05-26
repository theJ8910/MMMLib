package net.theJ89.minecraft;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.launcher.updater.DateTypeAdapter;
import com.mojang.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import com.mojang.launcher.updater.download.assets.AssetIndex;

import net.minecraft.launcher.game.MinecraftReleaseType;
import net.minecraft.launcher.updater.CompleteMinecraftVersion;
import net.minecraft.launcher.updater.PartialMinecraftVersion;
import net.theJ89.database.SQLiteDatabase;
import net.theJ89.http.HTTP;
import net.theJ89.mmm.MMM;
import net.theJ89.util.IO;

public class MinecraftVersions {
    private static final URL    VERSION_MANIFEST_URL = HTTP.stringToURL( "https://launchermeta.mojang.com/mc/game/version_manifest.json" );
    private static final String DATABASE_FILENAME    = "minecraft.db";
    
    private static final Gson gson;
    static {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter( Date.class, new DateTypeAdapter() );
        gb.registerTypeAdapterFactory( new LowerCaseEnumTypeAdapterFactory() );
        
        gson = gb.create();
    }

    private static SQLiteDatabase    db;
    private static PreparedStatement stmt_getMinecraftVersionID;
    private static PreparedStatement stmt_getMinecraftVersionNames;
    private static PreparedStatement stmt_getMinecraftVersion;
    private static PreparedStatement stmt_addMinecraftVersion;
    private static PreparedStatement stmt_updMinecraftVersion;
    private static PreparedStatement stmt_delMinecraftVersion;
    
    private static PreparedStatement stmt_getLatestVersions;
    private static PreparedStatement stmt_getLatestVersion;
    private static PreparedStatement stmt_addLatestVersion;
    private static PreparedStatement stmt_updLatestVersion;
    private static PreparedStatement stmt_delLatestVersion;
    
    private static final Map< String, CompleteMinecraftVersion > versions = new HashMap< String, CompleteMinecraftVersion >();
    private static final Map< String, AssetIndex >               assets   = new HashMap< String, AssetIndex >();
    
    
    private MinecraftVersions() {
        throw new Error();
    }
    
    public static void init() throws ClassNotFoundException, SQLException {
        db = new SQLiteDatabase( MMM.getDirectory().resolve( DATABASE_FILENAME ) );
        
        createDatabaseTables();
        createPreparedStatements();
    }
    
    public static void close() {
        IO.closeQuietly( stmt_getMinecraftVersionID );
        IO.closeQuietly( stmt_getMinecraftVersionNames );
        IO.closeQuietly( stmt_getMinecraftVersion );
        IO.closeQuietly( stmt_addMinecraftVersion );
        IO.closeQuietly( stmt_updMinecraftVersion );
        IO.closeQuietly( stmt_delMinecraftVersion );
        
        IO.closeQuietly( stmt_getLatestVersions );
        IO.closeQuietly( stmt_getLatestVersion );
        IO.closeQuietly( stmt_addLatestVersion );
        IO.closeQuietly( stmt_updLatestVersion );
        IO.closeQuietly( stmt_delLatestVersion );
        
        IO.closeQuietly( db );
    }
    
    private static void createDatabaseTables() throws SQLException {
        db.performTransaction( () -> {
            try( Statement stmt = db.createStatement() ) {
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"MinecraftVersion\"( " +
                    "    \"id\" INTEGER PRIMARY KEY AUTOINCREMENT, "    +
                    "    \"name\" TEXT NOT NULL UNIQUE, "               +
                    "    \"type\" INT NOT NULL, "                       +
                    "    \"time\" BIGINT NOT NULL, "                    +
                    "    \"releaseTime\" BIGINT NOT NULL, "             +
                    "    \"url\" TEXT NOT NULL "                        +
                    ")"
                );
                
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"LatestVersion\"( "                         +
                    "    \"type\" INT NOT NULL PRIMARY KEY, "                                +
                    "    \"mc_id\" INT NOT NULL REFERENCES \"MinecraftVersion\" ( \"id\" ) " +
                    ") WITHOUT ROWID"
                );
                
                stmt.executeUpdate(
                    "CREATE VIEW IF NOT EXISTS \"NamedLatestVersion\" AS " +
                    "    SELECT \"mv\".\"name\", \"lv\".\"type\""          +
                    "    FROM \"LatestVersion\" AS \"lv\" "                +
                    "    JOIN \"MinecraftVersion\" AS \"mv\" "             +
                    "    ON \"lv\".\"mc_id\" = \"mv\".\"id\""
                );
            }
        } );
    }
    
    private static void createPreparedStatements() throws SQLException {
        stmt_getMinecraftVersionID    = db.createPreparedStatement( "SELECT \"id\" FROM \"MinecraftVersion\" WHERE \"name\" = ? LIMIT 1" );
        stmt_getMinecraftVersionNames = db.createPreparedStatement( "SELECT \"id\", \"name\" FROM \"MinecraftVersion\"" );
        stmt_getMinecraftVersion      = db.createPreparedStatement( "SELECT \"name\", \"type\", \"time\", \"releaseTime\", \"url\" FROM \"MinecraftVersion\" WHERE \"id\" = ? LIMIT 1" );
        stmt_addMinecraftVersion      = db.createPreparedStatement( "INSERT INTO \"MinecraftVersion\" VALUES ( NULL, ?, ?, ?, ?, ? )" );
        stmt_updMinecraftVersion      = db.createPreparedStatement( "UPDATE \"MinecraftVersion\" SET \"type\" = ?, \"time\" = ?, \"releaseTime\" = ?, \"url\" = ? WHERE \"id\" = ?" );
        stmt_delMinecraftVersion      = db.createPreparedStatement( "DELETE FROM \"MinecraftVersion\" WHERE \"id\" = ?" );
        
        stmt_getLatestVersions        = db.createPreparedStatement( "SELECT \"name\", \"type\" FROM \"NamedLatestVersion\"" );
        stmt_getLatestVersion         = db.createPreparedStatement( "SELECT \"name\" FROM \"NamedLatestVersion\" WHERE \"type\" = ? LIMIT 1" );
        stmt_addLatestVersion         = db.createPreparedStatement( "INSERT INTO \"LatestVersion\" VALUES ( ?, ? )" );
        stmt_updLatestVersion         = db.createPreparedStatement( "UPDATE \"LatestVersion\" SET \"mc_id\" = ? WHERE \"type\" = ?" );
        stmt_delLatestVersion         = db.createPreparedStatement( "DELETE FROM \"LatestVersion\" WHERE \"type\" = ?" );
    }
    
    public static void update() throws IOException, SQLException {
        //Fetch Minecraft versions manifest from Mojang
        MinecraftVersionManifest mvm = HTTP.get( VERSION_MANIFEST_URL ).getObject( MinecraftVersionManifest.class );
        
        db.performTransaction( () -> {
            //Build map of version name -> version id for all old versions
            Map< String, Integer > old_ids = new HashMap< String, Integer >();
            try( ResultSet rs = stmt_getMinecraftVersionNames.executeQuery() ) {
                while( rs.next() )
                    old_ids.put( rs.getString( 2 ), rs.getInt( 1 ) );
            }
            
            //Build map of version name -> version for all new versions 
            Map< String, PartialMinecraftVersion > new_versions = new HashMap< String, PartialMinecraftVersion >();
            for( PartialMinecraftVersion mv : mvm.getVersions() )
                new_versions.put( mv.getID(), mv );
            
            //Make a set of both new and old version names
            Set< String > all_versions = new HashSet< String >( old_ids.keySet() );
            all_versions.addAll( new_versions.keySet() );
            
            //Add, update, and remove Minecraft versions depending on which maps they appear in
            for( String name : all_versions ) {
                Integer old_id = old_ids.get( name );
                PartialMinecraftVersion new_mv = new_versions.get( name );
                
                if( old_id != null ) {
                    if( new_mv != null ) {
                        //Update the Minecraft version if it has changed
                        PartialMinecraftVersion old_mv = getVersion( old_id );
                        if( !Objects.equals( old_mv, new_mv ) ) {
                            stmt_updMinecraftVersion.setInt( 1, new_mv.getType().getID() );
                            stmt_updMinecraftVersion.setLong( 2, new_mv.getTime().getTime() / 1000 );
                            stmt_updMinecraftVersion.setLong( 3, new_mv.getReleaseTime().getTime() / 1000 );
                            stmt_updMinecraftVersion.setString( 4, new_mv.getUrl() );
                            stmt_updMinecraftVersion.setInt( 5, old_id );
                            if( stmt_updMinecraftVersion.executeUpdate() == 0 )
                                throw new RuntimeException( "Failed to update Minecraft version." );
                            System.out.println( String.format( "Minecraft %s was updated.", name ) );
                            //TODO: Refresh this version and determine if anything has changed
                        }
                    //This version is no longer listed; delete it
                    } else {
                        stmt_delMinecraftVersion.setInt( 1, old_id );
                        if( stmt_delMinecraftVersion.executeUpdate() == 0 )
                            throw new RuntimeException( "Failed to delete Minecraft version." );
                        System.out.println( String.format( "Minecraft %s was removed.", name ) );
                    }
                //Add new versions
                } else if( new_mv != null ) {
                    stmt_addMinecraftVersion.setString( 1, name );
                    stmt_addMinecraftVersion.setInt( 2, new_mv.getType().getID() );
                    stmt_addMinecraftVersion.setLong( 3, new_mv.getTime().getTime() / 1000 );
                    stmt_addMinecraftVersion.setLong( 4, new_mv.getReleaseTime().getTime() / 1000 );
                    stmt_addMinecraftVersion.setString( 5, new_mv.getUrl() );
                    if( stmt_addMinecraftVersion.executeUpdate() == 0 )
                        throw new RuntimeException( "Failed to add new Minecraft version." );
                    System.out.println( String.format( "Minecraft %s was added.", name ) );
                }
            }
            
            //Get last known latest versions
            MinecraftLatestVersions old_mlv = getLatestVersions();
            MinecraftLatestVersions new_mlv = mvm.getLatestVersions();
            
            //Update latest versions if necessary
            if( Objects.equals( old_mlv, new_mlv ) )
                return;
            for( MinecraftReleaseType type : MinecraftReleaseType.values() ) {
                String oldName = old_mlv.get( type );
                String newName = new_mlv.get( type );
                
                //Find the ID of the Minecraft new Minecraft version with this name
                if( oldName != null ) {
                    if( newName != null ) {
                        //Update a latest version
                        if( !Objects.equals( oldName, newName ) ) {
                            stmt_updLatestVersion.setInt( 1, getVersionID( newName ) );
                            stmt_updLatestVersion.setInt( 2, type.getID() );
                            if( stmt_updLatestVersion.executeUpdate() == 0 )
                                throw new RuntimeException( "Couldn't update latest version." );
                        }
                    //Remove a latest version
                    } else {
                        stmt_delLatestVersion.setInt( 1, type.getID() ); 
                        if( stmt_delLatestVersion.executeUpdate() == 0 )
                            throw new RuntimeException( "Couldn't delete latest version." );
                    }
                //Add a latest version
                } else if( newName != null ) {
                    stmt_addLatestVersion.setInt( 1, type.getID() );
                    stmt_addLatestVersion.setInt( 2, getVersionID( newName ) );
                    if( stmt_addLatestVersion.executeUpdate() == 0 )
                        throw new RuntimeException( "Couldn't add latest version." );
                }
            }
        } );
    }
    
    /**
     * Returns the latest versions for all release types.
     * @return
     * @throws SQLException
     */
    public static MinecraftLatestVersions getLatestVersions() throws SQLException {
        MinecraftLatestVersions mlv = new MinecraftLatestVersions();
        try( ResultSet rs = stmt_getLatestVersions.executeQuery() ) {
            while( rs.next() )
                mlv.set( MinecraftReleaseType.get( rs.getInt( 2 ) ), rs.getString( 1 ) );
        }
        return mlv;
    }
    
    /**
     * Returns the latest version for the given release type, or null if the latest version is unknown.
     * @param type
     * @return
     * @throws SQLException
     */
    public static String getLatestVersion( final MinecraftReleaseType type ) throws SQLException {
        stmt_getLatestVersion.setInt( 1, type.getID() );
        try( ResultSet rs = stmt_getLatestVersion.executeQuery() ) {
            if( !rs.next() )
                return null;
            return rs.getString( 1 );
        }
    }
    
    /**
     * Returns basic details about a Minecraft version with the given database id.
     * @param id
     * @return
     * @throws SQLException
     */
    private static PartialMinecraftVersion getVersion( final int id ) throws SQLException {
        stmt_getMinecraftVersion.setInt( 1, id );
        try( ResultSet rs = stmt_getMinecraftVersion.executeQuery() ) {
            if( !rs.next() )
                throw new RuntimeException( "Couldn't get Minecraft version information." );
            return new PartialMinecraftVersion(
                rs.getString( 1 ),
                MinecraftReleaseType.get( rs.getInt( 2 ) ),
                new Date( rs.getLong( 3 ) * 1000 ),
                new Date( rs.getLong( 4 ) * 1000 ),
                rs.getString( 5 )
            );
        }
    }
    
    /**
     * Returns the database ID for the version with the given name
     * @param name
     * @return
     * @throws SQLException 
     */
    private static int getVersionID( final String name ) throws SQLException {
        stmt_getMinecraftVersionID.setString( 1, name );
        try( ResultSet rs = stmt_getMinecraftVersionID.executeQuery() ) {
            if( !rs.next() )
                throw new RuntimeException( "Unrecognized Minecraft version." );
            return rs.getInt( 1 );
        }
    }

    /**
     * Return information about a version of Minecraft.
     * Loads the information if it isn't already.
     * @param name - The version of Minecraft to get information from (e.g. "1.5.2", "1.6.4", "1.7.10")
     * @return Information about the requested Minecraft version.
     * @throws IOException
     */
    public static CompleteMinecraftVersion get( final String name ) throws IOException {
        CompleteMinecraftVersion v = versions.get( name );
        if( v == null )
            v = load( name );
        return v;
    }

    /**
     * Returns the asset index with the given name.
     * @param name - The name of the asset index to get.
     * @return
     */
    public static AssetIndex getAssetIndex( final String name ) throws IOException {
        AssetIndex i = assets.get( name );
        if( i == null )
            i = loadAssetIndex( name );
        return i;
    }
    
    /**
     * TEMP
     */
    public static CompleteMinecraftVersion load( final Path versionsDir, final String name ) throws IOException {
        CompleteMinecraftVersion v = null;
        try( Reader r = IO.newBufferedU8FileReader( getVersionInfo( versionsDir, name ) ) ) {
            v = gson.fromJson( r, CompleteMinecraftVersion.class );
        }
        
        String inheritsFrom = v.getInheritsFrom();
        if( inheritsFrom != null ) {
            CompleteMinecraftVersion parent = load( versionsDir, inheritsFrom );
            v.inherit( parent );
        }
        return v;
    }
    
    public static Path getVersionJar( final Path versionsDir, final String name ) {
        return versionsDir.resolve( name ).resolve( name + ".jar" );
    }
    
    public static Path getVersionInfo( final Path versionsDir, final String name ) {
        return versionsDir.resolve( name ).resolve( name + ".json" );
    }

    /**
     * Loads information about the Minecraft version with the given name.
     * @param name - The name of the version to load
     * @return
     * @throws IOException
     */
    private static CompleteMinecraftVersion load( final String name ) throws IOException {
        CompleteMinecraftVersion v = null;
        try( Reader r = IO.newBufferedU8FileReader( Paths.get( MinecraftConstants.VERSIONS_DIRECTORY, name, name + ".json" ) ) ) {
            v = gson.fromJson( r, CompleteMinecraftVersion.class );
        }
        v.validate( name );
    
        versions.put( name, v );
        return v;
    }

    /**
     * Loads an asset index with the given name.
     * Note that the asset index name is independent from the Minecraft version; several versions of Minecraft may share the same asset index.
     * For example, Minecraft versions "1.8" through "1.8.9" use the "1.8" asset index.
     * @param name - The name of the asset index to load.
     * @return
     * @throws IOException
     */
    private static AssetIndex loadAssetIndex( final String name ) throws IOException {
        AssetIndex i = null;
        try( Reader r = IO.newBufferedU8FileReader( Paths.get( MinecraftConstants.ASSETS_DIRECTORY, MinecraftConstants.ASSETS_INDICES_DIRECTORY, name + ".json" ) ) ) {
            i = gson.fromJson( r, AssetIndex.class );
        }
        i.validate();

        assets.put( name, i );
        return i;
    }
}
