package net.theJ89.MMMLib;

public class Proxy {
    private static Proxy proxy;
    
    private String host;
    private String port;
    private String username;
    private String password;
    
    public Proxy( String host, String port ) {
        this( host, port, null, null );
    }
    
    public Proxy( String host, String port, String username, String password ) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        this.password = password;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public String getPort() {
        return this.port;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Sets the global proxy
     * @param proxy
     */
    static public void set( Proxy proxy ) {
        Proxy.proxy = proxy;
    }
    
    /**
     * Gets the global proxy
     * @return
     */
    static public Proxy get() {
        return Proxy.proxy;
    }
}
