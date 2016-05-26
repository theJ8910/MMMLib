package net.theJ89.mmm;

public enum Side {
    //Note: CLIENT and SERVER should use the same IDs as SideCompat
    CLIENT( 0 ),
    SERVER( 1 );
    
    private static final Side ID_TO_SIDE[] = {
        CLIENT,
        SERVER
    };
    
    private int id;
    Side( final int id ) {
        this.id = id;
    }
    
    /**
     * Returns the ID of this Side.
     * @return
     */
    public int getID() {
        return this.id;
    }
    
    /**
     * Returns the SideCompat corresponding to this side
     * @return
     */
    public SideCompat toSideCompat() {
        switch( this ) {
        case CLIENT:
            return SideCompat.CLIENT;
        case SERVER:
            return SideCompat.SERVER;
        default:
            return null;
        }
    }
    
    /**
     * Returns the Side with the given ID.
     * @param id
     * @return
     */
    public static Side fromID( final int id ) {
        return ID_TO_SIDE[id];
    }
}
