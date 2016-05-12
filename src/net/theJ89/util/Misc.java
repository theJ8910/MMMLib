package net.theJ89.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class Misc {
    /**
     * Closes the given closeable.
     * If an exception occurs while closing, it is ignored.
     * 
     * Borrowed from Apache IOUtils
     * 
     * @param closeable - The closeable to close
     */
    public static void closeQuietly( Closeable closeable ) {
        try {
            if( closeable != null )
                closeable.close();
        } catch (IOException e) {}
    }
    
    /**
     * Reads all bytes from the given input stream into a byte array and returns it.
     * 
     * Borrowed from Apache IOUtils
     * 
     * @param in - The input stream to read from.
     */
    public static byte[] toByteArray( InputStream in ) throws IOException {
        //Read 4096 bytes at a time into the byte array output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream( 1024 );
        byte[] buf = new byte[4096];
        int n = 0;
        while( ( n = in.read( buf ) ) != -1 )
            out.write( buf, 0, n );
        return out.toByteArray();
    }
}
