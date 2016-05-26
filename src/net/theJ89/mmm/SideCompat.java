package net.theJ89.mmm;

public enum SideCompat {
    //Note: CLIENT and SERVER should use the same IDs as Side
    
    /**
     * Compatible with client only
     */
    CLIENT( 0 ),
    /**
     * Compatible with server only
     */
    SERVER( 1 ),
    /**
     * Compatible with both client and server
     */
    UNIVERSAL( 2 );
    
    private static final SideCompat ID_TO_SIDECOMPAT[] = {
        CLIENT,
        SERVER,
        UNIVERSAL
    };
    
    private int id;
    
    SideCompat( final int id ) {
        this.id = id;
    }
    
    /**
     * Returns the ID of this SideCompat.
     * @return
     */
    public int getID() {
        return this.id;
    }
    
    /**
     * Returns true if the given Side is compatible with this particular SideCompat. Returns false otherwise.
     * e.g. Side.CLIENT is compatible with SideCompat.UNIVERSAL, but not SideCompat.SERVER.
     * @param side
     * @return
     */
    public boolean isCompatible( Side side ) {
        if( this == UNIVERSAL ) return true;
        else                    return this.toSide() == side;
    }
    
    /**
     * Returns the Side corresponding to this SideCompat.
     * UNIVERSAL is neither CLIENT nor SERVER, therefore UNIVERSAL.toSide() will return null.
     * @return
     */
    public Side toSide() {
        switch( this ) {
        case CLIENT:
            return Side.CLIENT;
        case SERVER:
            return Side.SERVER;
        case UNIVERSAL:
        default:
            return null;
        }
    }
    
    /**
     * Returns the SideCompat with the given ID.
     * @param id
     * @return
     */
    public static SideCompat fromID( final int id ) {
        return ID_TO_SIDECOMPAT[id];
    }
}
