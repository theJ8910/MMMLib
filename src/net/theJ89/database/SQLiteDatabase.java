package net.theJ89.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabase implements AutoCloseable {
    private static final String CONNECTOR_CLASS = "org.sqlite.JDBC";
    private static final String JDBC_PREFIX     = "jdbc:sqlite:";
    
    private Connection connection;
    
    /**
     * Constructor for an SQLite database.
     * @param databaseFilepath - filename of the database file to open (e.g. sample.db). Creates the file if it doesn't exist.
     * @throws ClassNotFoundException If the SQlite JDBC connector wasn't found.
     * @throws SQLException If there was a problem connecting to the database.
     */
    public SQLiteDatabase( Path databaseFilepath ) throws ClassNotFoundException, SQLException {
        this.connection = null;
        
        //Make sure we have the connector class available
        Class.forName( CONNECTOR_CLASS );
        
        //Try to connect to the database
        connection = DriverManager.getConnection( JDBC_PREFIX + databaseFilepath.toString() );
    }
    
    /**
     * Creates a statement.
     * @return A statement, means of interacting with the database.
     * @throws SQLException If the database is closed.
     */
    public Statement createStatement() throws SQLException {
        return this.connection.createStatement();
    }
    
    /**
     * Creates a prepared statement using the given string.
     * @param query - The query the statement performs, with ? to represent variables.
     * @return The prepared statement.
     * @throws SQLException If the database is closed.
     */
    public PreparedStatement createPreparedStatement( String query ) throws SQLException {
        return this.connection.prepareStatement( query );
    }
    
    /**
     * Called to begin a transaction.
     * @throws SQLException
     */
    public void beginTransaction() throws SQLException {
        this.connection.setAutoCommit( false );
    }
    
    /**
     * Called to commit the current transaction.
     * @throws SQLException
     */
    public void commitTransaction() throws SQLException {
        this.connection.commit();
        this.connection.setAutoCommit( true );
    }
    
    /**
     * Called to roll back the current transaction.
     * @throws SQLException
     */
    public void rollbackTransaction() throws SQLException {
        this.connection.rollback();
        this.connection.setAutoCommit( true );
    }
    
    /**
     * Closes the connection to the database.
     * If a SQLException is encountered it prints a stack trace.
     */
    @Override
    public void close() {
        if( this.connection == null )
            return;
        
        try {
            this.connection.close();
            
        //Statement / connection close failed.
        } catch( SQLException e ) { e.printStackTrace(); }
    }
}
