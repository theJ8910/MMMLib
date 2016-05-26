package net.theJ89.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Nullable {
    /**
     * Same as rs.getBoolean() but returns null if the value was null in the database.
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public static Boolean getBoolean( final ResultSet rs, final int index ) throws SQLException {
        boolean value = rs.getBoolean( index );
        if( rs.wasNull() )
            return null;
        return value;
    }
    
    /**
     * Same as rs.getInt() but returns null if the value was null in the database.
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public static Integer getInt( final ResultSet rs, final int index ) throws SQLException {
        Integer value = rs.getInt( index );
        if( rs.wasNull() )
            return null;
        return value;
    }
    
    /**
     * Same as rs.getLong() but returns null if the value was null in the database.
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public static Long getLong( final ResultSet rs, final int index ) throws SQLException {
        Long value = rs.getLong( index );
        if( rs.wasNull() )
            return null;
        return value;
    }
    
    /**
     * Same as rs.getString() but returns null if the value was null in the database.
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public static String getString( final ResultSet rs, final int index ) throws SQLException {
        String value = rs.getString( index );
        if( rs.wasNull() )
            return null;
        return value;
    }
}
