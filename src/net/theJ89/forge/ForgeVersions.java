package net.theJ89.forge;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import net.theJ89.database.Nullable;
import net.theJ89.database.SQLiteDatabase;
import net.theJ89.http.HTTP;
import net.theJ89.mmm.MMM;
import net.theJ89.mmm.Side;
import net.theJ89.mmm.SideCompat;
import net.theJ89.util.IO;

public class ForgeVersions {
    private static final String DATABASE_FILENAME = "forge.db";
    private static final URL    MIRROR_LIST_URL   = HTTP.stringToURL( "http://files.minecraftforge.net/mirror-brand.list" );
    
    //Local SQLite database containing information on forge versions
    private static SQLiteDatabase db;
    
    private static PreparedStatement stmt_getMinecraftVersion;
    private static PreparedStatement stmt_addMinecraftVersion;
    private static PreparedStatement stmt_getForgeVersion;
    private static PreparedStatement stmt_getForgeVersionId;
    private static PreparedStatement stmt_addForgeVersion;
    private static PreparedStatement stmt_extForgeDownload;
    private static PreparedStatement stmt_getForgeDownload;
    private static PreparedStatement stmt_addForgeDownload;
    
    private static PreparedStatement stmt_getLatestVersion;
    private static PreparedStatement stmt_addLatestVersion;
    private static PreparedStatement stmt_updLatestVersion;
    private static PreparedStatement stmt_delLatestVersion;
    
    private static PreparedStatement stmt_getRecommendedVersion;
    private static PreparedStatement stmt_addRecommendedVersion;
    private static PreparedStatement stmt_updRecommendedVersion;
    private static PreparedStatement stmt_delRecommendedVersion;
    
    private static PreparedStatement stmt_getMirrors;
    private static PreparedStatement stmt_addMirror;
    private static PreparedStatement stmt_updMirror;
    private static PreparedStatement stmt_delMirror;
    
    private ForgeVersions() {
        throw new Error();
    }
    
    public static void init() throws ClassNotFoundException, SQLException {
        db = new SQLiteDatabase( MMM.getDirectory().resolve( DATABASE_FILENAME ) );
        
        createDatabaseTables();
        createPreparedStatements();
    }
    
    private static void createDatabaseTables() throws SQLException {
        db.performTransaction( () -> {
            try( Statement stmt = db.createStatement() ) {
                //Drop existing tables
                /*
                stmt.executeUpdate( "DROP TABLE IF EXISTS \"MinecraftVersion\""    );
                stmt.executeUpdate( "DROP TABLE IF EXISTS \"ForgeVersion\""        );
                stmt.executeUpdate( "DROP TABLE IF EXISTS \"LatestVersion\""       );
                stmt.executeUpdate( "DROP TABLE IF EXISTS \"RecommendedVersion\""  );
                stmt.executeUpdate( "DROP TABLE IF EXISTS \"ForgeDownload\""       );
                stmt.executeUpdate( "DROP VIEW  IF EXISTS \"ForgeDownloadVersion\"" );
                */
                
                //Create tables
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"MinecraftVersion\"( "       +
                    "    \"id\" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "    \"name\" TEXT NOT NULL UNIQUE "                      +
                    ")"
                );
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"ForgeVersion\"( "                          +
                    "    \"id\"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "             +
                    "    \"mc_id\" INT NOT NULL REFERENCES \"MinecraftVersion\"( \"id\" ), " +
                    "    \"name\"  TEXT NOT NULL UNIQUE, "                                   +
                    "    \"time\"  BIGINT NOT NULL "                                         +
                    ")"
                );
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"LatestVersion\"( "                            +
                    "    \"mc_id\" INT PRIMARY KEY REFERENCES \"MinecraftVersion\"( \"id\" ), " +
                    "    \"forge_id\" INT NOT NULL REFERENCES \"ForgeVersion\"( \"id\" ) "      +
                    ") WITHOUT ROWID"
                );
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"RecommendedVersion\"( "                       +
                    "    \"mc_id\" INT PRIMARY KEY REFERENCES \"MinecraftVersion\"( \"id\" ), " +
                    "    \"forge_id\" INT NOT NULL REFERENCES \"ForgeVersion\"( \"id\" ) "      +
                    ") WITHOUT ROWID"
                );
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"ForgeDownload\"( "                        +
                    "    \"forge_id\" INT NOT NULL REFERENCES \"ForgeVersion\"( \"id\" ), " +
                    "    \"type\" INT NOT NULL, "                                           +
                    "    \"url\" TEXT NOT NULL, "                                           +
                    "    \"size\" BIGINT, "                                                 +
                    "    \"md5\" TEXT, "                                                    +
                    "    \"sha1\" TEXT, "                                                   +
                    "    PRIMARY KEY ( \"forge_id\", \"type\" ) "                           +
                    ") WITHOUT ROWID"
                );
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"Mirror\"( " +
                    "    \"name\" TEXT PRIMARY KEY, "         +
                    "    \"imageURL\" TEXT NOT NULL, "        +
                    "    \"clickURL\" TEXT NOT NULL, "        +
                    "    \"url\" TEXT NOT NULL "              +
                    ") WITHOUT ROWID"
                );
                stmt.executeUpdate(
                    "CREATE VIEW IF NOT EXISTS \"ForgeDownloadVersion\" AS "                                                         +
                    "    SELECT \"fv\".\"name\", \"fd\".\"type\", \"fd\".\"url\", \"fd\".\"size\", \"fd\".\"md5\", \"fd\".\"sha1\" " +
                    "    FROM \"ForgeVersion\" AS \"fv\" "                                                                           +
                    "    JOIN \"ForgeDownload\" AS \"fd\" "                                                                          +
                    "    ON \"fv\".\"id\" = \"fd\".\"forge_id\""
                );
            }
        });
    }
    
    private static void createPreparedStatements() throws SQLException {
        stmt_getMinecraftVersion   = db.createPreparedStatement( "SELECT \"id\" FROM \"MinecraftVersion\" WHERE \"name\" = ?" );
        stmt_addMinecraftVersion   = db.createPreparedStatement( "INSERT INTO \"MinecraftVersion\" VALUES( NULL, ? )" );
        stmt_getForgeVersion       = db.createPreparedStatement( "SELECT * FROM \"ForgeVersion\" WHERE \"name\" = ?" );
        stmt_getForgeVersionId     = db.createPreparedStatement( "SELECT \"id\" FROM \"ForgeVersion\" WHERE \"name\" = ?" );
        stmt_addForgeVersion       = db.createPreparedStatement( "INSERT INTO \"ForgeVersion\" VALUES( NULL, ?, ?, ? )" );
        stmt_extForgeDownload      = db.createPreparedStatement( "SELECT EXISTS( SELECT \"type\" FROM \"ForgeDownload\" WHERE \"forge_id\" = ? AND \"type\" = ? LIMIT 1 ) AS x" );
        stmt_getForgeDownload      = db.createPreparedStatement( "SELECT \"type\", \"url\", \"size\", \"md5\", \"sha1\" FROM ForgeDownloadVersion WHERE \"name\" = ? AND \"type\" IN ( ?, " + SideCompat.UNIVERSAL.getID() + " )" );
        stmt_addForgeDownload      = db.createPreparedStatement( "INSERT INTO \"ForgeDownload\" VALUES( ?, ?, ?, ?, ?, ? )" );
        
        stmt_getRecommendedVersion = db.createPreparedStatement( "SELECT \"fv\".* FROM \"ForgeVersion\" AS \"fv\" JOIN \"LatestVersion\" AS \"lv\" ON \"fv\".\"id\" = \"lv\".\"forge_id\" WHERE \"lv\".\"mc_id\" = ?" );
        stmt_addLatestVersion      = db.createPreparedStatement( "INSERT INTO \"LatestVersion\" VALUES( ?, ? )" );
        stmt_updLatestVersion      = db.createPreparedStatement( "UPDATE \"LatestVersion\" SET \"forge_id\" = ? WHERE \"mc_id\" = ?" );
        stmt_delLatestVersion      = db.createPreparedStatement( "DELETE FROM \"LatestVersion\" WHERE \"mc_id\" = ?" );
        
        stmt_getRecommendedVersion = db.createPreparedStatement( "SELECT \"fv\".* FROM \"ForgeVersion\" AS \"fv\" JOIN \"RecommendedVersion\" AS \"rv\" ON \"fv\".\"id\" = \"rv\".\"forge_id\" WHERE \"rv\".\"mc_id\" = ?" );
        stmt_addRecommendedVersion = db.createPreparedStatement( "INSERT INTO \"RecommendedVersion\" VALUES( ?, ? )" );
        stmt_updRecommendedVersion = db.createPreparedStatement( "UPDATE \"RecommendedVersion\" SET \"forge_id\" = ? WHERE \"mc_id\" = ?" );
        stmt_delRecommendedVersion = db.createPreparedStatement( "DELETE FROM \"RecommendedVersion\" WHERE \"mc_id\" = ?" );
        
        stmt_getMirrors            = db.createPreparedStatement( "SELECT * FROM \"Mirror\"" );
        stmt_addMirror             = db.createPreparedStatement( "INSERT INTO \"Mirror\" VALUES( ?, ?, ?, ? )" );
        stmt_updMirror             = db.createPreparedStatement( "UPDATE \"Mirror\" SET \"imageURL\" = ?, \"clickURL\" = ?, \"url\" = ? WHERE \"name\" = ?" );
        stmt_delMirror             = db.createPreparedStatement( "DELETE FROM \"Mirror\" WHERE \"name\" = ?" );
    }
    
    public static void close() {
        //Close prepared statements
        IO.closeQuietly( stmt_addMinecraftVersion   );
        IO.closeQuietly( stmt_getMinecraftVersion   );
        IO.closeQuietly( stmt_addForgeVersion       );
        IO.closeQuietly( stmt_getForgeVersion       );
        IO.closeQuietly( stmt_getForgeVersionId     );
        IO.closeQuietly( stmt_extForgeDownload      );
        IO.closeQuietly( stmt_getForgeDownload      );
        IO.closeQuietly( stmt_addForgeDownload      );
        
        IO.closeQuietly( stmt_getLatestVersion      );
        IO.closeQuietly( stmt_addLatestVersion      );
        IO.closeQuietly( stmt_updLatestVersion      );
        IO.closeQuietly( stmt_delLatestVersion      );
        
        IO.closeQuietly( stmt_getRecommendedVersion );
        IO.closeQuietly( stmt_addRecommendedVersion );
        IO.closeQuietly( stmt_updRecommendedVersion );
        IO.closeQuietly( stmt_delRecommendedVersion );
        
        IO.closeQuietly( stmt_getMirrors );
        IO.closeQuietly( stmt_addMirror );
        IO.closeQuietly( stmt_updMirror );
        IO.closeQuietly( stmt_delMirror );
        
        IO.closeQuietly( db );
    }
    
    /**
     * Updates the local Forge Versions database by scraping the Forge Files website.
     * @throws SQLException
     */
    public static void update() throws SQLException {
        db.performTransaction( () -> {
            try                     {
                ForgeIndex.parse( new ForgeVersionsUpdater() );
                
                updateMirrors();
            }
            catch( SQLException e ) { throw e; }
            catch( Exception e )    { throw new RuntimeException( e ); }
        } );
    }
    
    /**
     * Updates the local database with mirrors
     * @throws IOException
     * @throws SQLException 
     */
    private static void updateMirrors() throws IOException, SQLException {
        //Get local map of mirrors from local database.
        Map< String, ForgeMirror > local_mirrors = new HashMap< String, ForgeMirror >();
        for( ForgeMirror mirror : getMirrors() ) {
            local_mirrors.put( mirror.getName(), mirror );
        }
        
        //Get remote map of mirrors.
        //The file at MIRROR_LIST_URL contains one mirror per line.
        String[] lines = HTTP.get( MIRROR_LIST_URL ).getText().split( "\n" );
        Map< String, ForgeMirror > remote_mirrors = new HashMap< String, ForgeMirror >();
        for( String line : lines ) {
            if( Objects.equals( line, "" ) )
                continue;
            //Each line in the file consists of four values separated by ! (an exclamation mark)
            String[] parts = line.split( "!" );
            if( parts.length < 4 ) {
                System.err.println( "Skipping mirror: not enough parts." );
                continue;
            }
            remote_mirrors.put( parts[0], new ForgeMirror( parts[0], parts[1], parts[2], parts[3] ) );
        }
        
        //Compare differences between local and remote
        Set< String > all_mirrors = new HashSet< String >( local_mirrors.keySet() );
        all_mirrors.addAll( remote_mirrors.keySet() );
        for( String name : all_mirrors ) {
            ForgeMirror local  = local_mirrors.get(  name );
            ForgeMirror remote = remote_mirrors.get( name );
            if( local != null ) {
                if( remote != null ) {
                    if( !Objects.equals( local, remote ) ) {
                        stmt_updMirror.setString( 1, remote.getImageURL() );
                        stmt_updMirror.setString( 2, remote.getClickURL() );
                        stmt_updMirror.setString( 3, remote.getURL()      );
                        stmt_updMirror.setString( 4, name                 );
                        if( stmt_updMirror.executeUpdate() != 1 )
                            throw new RuntimeException( "Updating mirror failed." );
                    }
                } else {
                    stmt_delMirror.setString( 1, name );
                    if( stmt_delMirror.executeUpdate() != 1 )
                        throw new RuntimeException( "Deleting mirror failed." );
                }
            } else if( remote != null ) {
                stmt_addMirror.setString( 1, name                 );
                stmt_addMirror.setString( 2, remote.getImageURL() );
                stmt_addMirror.setString( 3, remote.getClickURL() );
                stmt_addMirror.setString( 4, remote.getURL()      );
                if( stmt_addMirror.executeUpdate() != 1 )
                    throw new RuntimeException( "Adding mirror failed." );
            }
        }
    }
    
    /**
     * Returns a list of Forge mirrors loaded from the local database
     * @return
     * @throws SQLException
     */
    public static List< ForgeMirror > getMirrors() throws SQLException {
        List< ForgeMirror > list = new ArrayList< ForgeMirror >();
        try( ResultSet rs = stmt_getMirrors.executeQuery() ) {
            while( rs.next() )
                list.add( new ForgeMirror(
                    rs.getString( 1 ),
                    rs.getString( 2 ),
                    rs.getString( 3 ),
                    rs.getString( 4 )
                ) );
        }
        return list;
    }
    
    /**
     * Returns a random Forge mirror.
     * @return
     * @throws SQLException
     */
    public static ForgeMirror getRandomMirror() throws SQLException {
        List< ForgeMirror > mirrors = ForgeVersions.getMirrors();
        int count = mirrors.size();
        return count > 0 ? mirrors.get( new Random().nextInt( count ) ) : null;
    }
    
    public static ForgeDownload getDownload( String version, Side side ) throws SQLException {
        stmt_getForgeDownload.setString( 1, version );
        stmt_getForgeDownload.setInt( 2, side.getID() );
        try( ResultSet rs = stmt_getForgeDownload.executeQuery() ) {
            if( !rs.next() )
                return null;
            
            return new ForgeDownload(
                SideCompat.fromID( rs.getInt( 1 ) ),
                rs.getString( 2 ),
                Nullable.getLong( rs, 3 ),
                Nullable.getString( rs, 4 ),
                Nullable.getString( rs, 5 )
            );
        }
    }
    
    public static class ForgeVersionsUpdater implements ForgeIndexContentHandler {
        private Long    minecraft_id  = null;
        private Long    forge_id      = null;
        private boolean skip_download = false;
        
        public ForgeVersionsUpdater() {}
        
        @Override
        public void startMinecraft( final MinecraftVersion mv ) throws Exception {
            String name = mv.getName();
            System.out.println( String.format( "Parsing Forge versions for Minecraft %s...", name ) );
            
            //Find or insert this Minecraft version and get its ID
            stmt_getMinecraftVersion.setString( 1, name );
            try( ResultSet rs = stmt_getMinecraftVersion.executeQuery() ) {
                if( rs.next() ) {
                    minecraft_id = rs.getLong( 1 );
                } else {
                    stmt_addMinecraftVersion.setString( 1, name );
                    stmt_addMinecraftVersion.executeUpdate();
                    try( ResultSet rs2 = stmt_addMinecraftVersion.getGeneratedKeys(); ) {
                        if( !rs2.next() )
                            throw new RuntimeException( "Couldn't retrieve auto-generated ID for inserted Minecraft version." );
                        minecraft_id = rs2.getLong( 1 );
                    }
                }
            }
        }
        
        @Override
        public void endMinecraft( final MinecraftVersion mv ) throws Exception {
            //Update latest and recommended versions
            setPromotedVersion( mv.getLatest(), stmt_addLatestVersion, stmt_updLatestVersion, stmt_delLatestVersion );
            setPromotedVersion( mv.getRecommended(), stmt_addRecommendedVersion, stmt_updRecommendedVersion, stmt_delRecommendedVersion );
        }
        
        @Override
        public void forge( final ForgeVersion fv ) throws Exception {
            String name = fv.getName();
            stmt_getForgeVersionId.setString( 1, name );
            try( ResultSet rs = stmt_getForgeVersionId.executeQuery() ) {
                if( rs.next() ) {
                    forge_id = rs.getLong( 1 );
                } else {
                    stmt_addForgeVersion.setLong( 1, minecraft_id );
                    stmt_addForgeVersion.setString( 2, name );
                    stmt_addForgeVersion.setLong( 3, fv.getTime().toEpochSecond() );
                    stmt_addForgeVersion.executeUpdate();
                    try( ResultSet rs2 = stmt_addForgeVersion.getGeneratedKeys() ) {
                        if( !rs2.next() )
                            throw new RuntimeException( "Couldn't retrieve auto-generated ID for inserted Forge version." );
                        forge_id = rs2.getLong( 1 );
                    }
                }
            }
        }
        
        @Override
        public boolean startDownload( final ForgeDownload fd ) throws Exception {
            stmt_extForgeDownload.setLong( 1, forge_id );
            stmt_extForgeDownload.setInt( 2, fd.getType().getID() );
            try( ResultSet rs = stmt_extForgeDownload.executeQuery() ) {
                if( !rs.next() )
                    throw new RuntimeException( "Could not determine if download exists or not." );
                this.skip_download = rs.getBoolean( 1 );
                return !this.skip_download;
            }
        }
        
        @Override
        public void endDownload( final ForgeDownload fd ) throws Exception {
            if( this.skip_download )
                return;
            
            stmt_addForgeDownload.setLong( 1, forge_id );
            stmt_addForgeDownload.setInt( 2, fd.getType().getID() );
            stmt_addForgeDownload.setString( 3, fd.getURL() );
            
            Long size = fd.getSize();
            if( size == null ) { stmt_addForgeDownload.setNull( 4, Types.BIGINT ); }
            else               { stmt_addForgeDownload.setLong( 4, size );         }
            
            String md5 = fd.getMD5();
            if( md5 == null ) { stmt_addForgeDownload.setNull( 5, Types.VARCHAR ); }
            else              { stmt_addForgeDownload.setString( 5, md5 );         }
            
            String sha1 = fd.getSHA1();
            if( sha1 == null ) { stmt_addForgeDownload.setNull( 6, Types.VARCHAR ); }
            else               { stmt_addForgeDownload.setString( 6, sha1 );        }
            
            stmt_addForgeDownload.executeUpdate();
        }
        
        /**
         * Add, update, or remove a promoted (latest / recommended) version
         * @param versionName - The name of the forge version, or null to remove
         * @param add - PreparedStatement that adds a row to the desired table
         * @param upd - PreparedStatement that updates a row in the desired table
         * @param del - PreparedStatement that removes a row from the desired table
         * 
         * @throws SQLException
         */
        private void setPromotedVersion( final String versionName, final PreparedStatement add, final PreparedStatement upd, final PreparedStatement del ) throws SQLException {
            if( versionName == null ) {
                del.setLong( 1, minecraft_id );
                del.executeUpdate();
            } else {
                //Find the forge version ID from its name
                long id;
                stmt_getForgeVersionId.setString( 1, versionName );
                try( ResultSet rs = stmt_getForgeVersionId.executeQuery() ) {
                    if( !rs.next() )
                        throw new RuntimeException( "No forge version with given name." );
                    id = rs.getLong( 1 );
                }
                
                //Update or insert the promoted forge version for the current version of Minecraft
                upd.setLong( 1, id );
                upd.setLong( 2, minecraft_id );
                int count = upd.executeUpdate();
                if( count == 0 ) {
                    add.setLong( 1, minecraft_id );
                    add.setLong( 2, id );
                    count = add.executeUpdate();
                    if( count == 0 )
                        throw new RuntimeException( "Promoted version could not be updated." );
                }
            }
        }
    }
}
