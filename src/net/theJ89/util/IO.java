package net.theJ89.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipFile;

public class IO {
    /**
     * Closes the given closeable.
     * Does nothing if the given closeable is null.
     * @param closeable - The closeable to close.
     * @throws Exception If an exception occurs when while closing.
     */
    public static void close( final AutoCloseable closeable ) throws Exception {
        if( closeable != null )
            closeable.close();
    }
    
    /**
     * Closes the given closeable.
     * If an exception occurs while closing, it is printed to the standard error stream.
     * Does nothing if the given closeable is null.
     * 
     * @param closeable - The closeable to close.
     */
    public static void closeNoisily( final AutoCloseable closeable ) {
        try {
            if( closeable != null )
                closeable.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    /**
     * Closes the given closeable.
     * If an exception occurs while closing, it is ignored.
     * Does nothing if the given closeable is null.
     * 
     * Borrowed from Apache IOUtils
     * 
     * @param closeable - The closeable to close.
     */
    public static void closeQuietly( final AutoCloseable closeable ) {
        try {
            if( closeable != null )
                closeable.close();
        } catch (Exception e) {}
    }
    
    /**
     * Returns a new buffered InputStream to the file at the given path.
     * @param path
     * @return
     * @throws IOException
     */
    public static InputStream newBufferedFileInputStream( final Path path ) throws IOException {
        return new BufferedInputStream( Files.newInputStream( path ) );
    }
    
    /**
     * Returns a new buffered JarInputStream ot the file at the given path.
     * @param path
     * @return
     * @throws IOException
     */
    public static JarInputStream newBufferedJarFileInputStream( final Path path ) throws IOException {
        return new JarInputStream( new BufferedInputStream( Files.newInputStream( path) ) );
    }

    /**
     * Returns a new buffered OutputStream to the file at the given path. 
     * @param path
     * @return
     * @throws IOException
     */
    public static OutputStream newBufferedFileOutputStream( final Path path ) throws IOException {
        return new BufferedOutputStream( Files.newOutputStream( path ) );
    }
    
    /**
     * Returns an ew buffered JarOutputStream to the file at the given path.
     * @param path
     * @return
     * @throws IOException
     */
    public static JarOutputStream newBufferedJarFileOutputStream( final Path path ) throws IOException {
        return new JarOutputStream( new BufferedOutputStream( Files.newOutputStream( path ) ) );
    }
    
    /**
     * Returns a new BufferedReader that reads default charset characters from the given input stream.
     * @param in
     * @return
     */
    public static BufferedReader newBufferedISReader( final InputStream in ) {
        return new BufferedReader( new InputStreamReader( in ) );
    }

    /**
     * Returns a new BufferedReader that reads UTF-8 characters from the given input stream.
     * @param in
     * @return
     */
    public static BufferedReader newBufferedU8ISReader( final InputStream in ) {
        return new BufferedReader( new InputStreamReader( in, StandardCharsets.UTF_8 ) );
    }
    
    /**
     * Returns a new BufferedReader that reads UTF-8 characters from the file at the given path.
     * @param path
     * @return
     * @throws IOException
     */
    public static BufferedReader newBufferedU8FileReader( final Path path ) throws IOException {
        return Files.newBufferedReader( path, StandardCharsets.UTF_8 );
    }
    
    /**
     * Returns a new BufferedWriter that writes default charset characters to the given output stream.
     * @param in
     * @return
     */
    public static BufferedWriter newBufferedOSWriter( final OutputStream in ) {
        return new BufferedWriter( new OutputStreamWriter( in ) );
    }
    
    /**
     * Returns a new BufferedWriter that writes UTF-8 characters to the given output stream.
     * @param out
     * @return
     * @throws IOException
     */
    public static BufferedWriter newBufferedU8OSWriter( final OutputStream out ) throws IOException {
        return new BufferedWriter( new OutputStreamWriter( out, StandardCharsets.UTF_8 ) );
    }
    
    /**
     * Returns a new BufferedWriter that writes UTF-8 characters to the file at the given path.
     * @param path
     * @return
     * @throws IOException
     */
    public static BufferedWriter newBufferedU8FileWriter( final Path path ) throws IOException {
        return Files.newBufferedWriter( path, StandardCharsets.UTF_8 );
    }
    
    /**
     * Reads all bytes from the given input stream into a byte array and returns it.
     * 
     * Borrowed from Apache IOUtils
     * 
     * @param in - The input stream to read from.
     */
    public static byte[] toByteArray( final InputStream in ) throws IOException {
        //Read 4096 bytes at a time into the byte array output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream( 1024 );
        byte[] buf = new byte[4096];
        int c = 0;
        while( ( c = in.read( buf ) ) != -1 )
            out.write( buf, 0, c );
        return out.toByteArray();
    }
    
    /**
     * Copies everything in the given input stream to the given output stream.
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copy( final InputStream in, final OutputStream out ) throws IOException {
        byte[] buf = new byte[4096];
        int c;
        while( ( c = in.read( buf ) ) != -1 )
            out.write( buf, 0, c );
    }
    
    /**
     * Same as {@link #copy(InputStream, OutputStream)}, but if hash is not null, computes an MD5 hash of the data as it passes from in to out.
     * After the data has been fully read, the computed hash is compared to the given hash.
     * A RuntimeException is generated if the hashes do not match.
     * @param in
     * @param out
     * @param hash
     * @throws IOException
     */
    public static void copy_MD5( final InputStream in, final OutputStream out, final String hash ) throws IOException {
        if( hash == null ) {
            copy( in, out );
            return;
        }
        
        byte[] buf = new byte[4096];
        int c;
        MessageDigest md5 = Misc.newSHA1();
        while( ( c = in.read( buf ) ) != -1 ) {
            out.write( buf, 0, c );
            md5.update( buf, 0, c );
        }
        String computedHash = String.format( "%032x", new BigInteger( 1, md5.digest() ) );
        if( !computedHash.equals( hash ) )
            throw new RuntimeException( "SHA-1 hash (" + computedHash + ") doesn't match expected hash (" + hash + ")." );
    }
    
    /**
     * Same as {@link #copy(InputStream, OutputStream)}, but if hash is not null, computes a SHA-1 hash of the data as it passes from in to out.
     * After the data has been fully read, the computed hash is compared to the given hash.
     * A RuntimeException is generated if the hashes do not match.
     * @param in
     * @param out
     * @param hash
     * @throws IOException
     */
    public static void copy_SHA1( final InputStream in, final OutputStream out, final String hash ) throws IOException {
        if( hash == null ) {
            copy( in, out );
            return;
        }
        
        byte[] buf = new byte[4096];
        int c;
        MessageDigest sha1 = Misc.newSHA1();
        while( ( c = in.read( buf ) ) != -1 ) {
            out.write( buf, 0, c );
            sha1.update( buf, 0, c );
        }
        String computedHash = String.format( "%040x", new BigInteger( 1, sha1.digest() ) );
        if( !computedHash.equals( hash ) )
            throw new RuntimeException( "SHA-1 hash (" + computedHash + ") doesn't match expected hash (" + hash + ")." );
    }
    
    /**
     * Same as {@link #copy(InputStream, OutputStream)}, but if md5Hash and/or sha1Hash is not null, computes the MD5 and/or SHA1 hash of the data as it passes from in to out.
     * After the data has been fully read, the computed hashes are compared to the given hashes.
     * A RuntimeException is generated if either hash doesn't match.
     * @throws IOException 
     */
    public static void copy_MD5_SHA1( final InputStream in, final OutputStream out, final String md5Hash, final String sha1Hash ) throws IOException {
        if( md5Hash == null ) {
            copy_SHA1( in, out, sha1Hash );
            return;
        }
        if( sha1Hash == null ) {
            copy_MD5( in, out, md5Hash );
            return;
        }
        
        byte[] buf = new byte[4096];
        int c;
        MessageDigest md5  = Misc.newMD5();
        MessageDigest sha1 = Misc.newSHA1();
        while( ( c = in.read( buf ) ) != -1 ) {
            out.write( buf, 0, c );
            md5.update( buf, 0, c );
            sha1.update( buf, 0, c );
        }
        
        //Check MD5 hash
        String computedHash = String.format( "%032x", new BigInteger( 1, md5.digest() ) );
        if( !computedHash.equals( md5Hash ) )
            throw new RuntimeException( "MD5 hash (" + computedHash + ") doesn't match expected hash (" + md5Hash + ")." );
        
        //Check SHA-1 hash
        computedHash = String.format( "%040x", new BigInteger( 1, sha1.digest() ) );
        if( !computedHash.equals( sha1Hash ) )
            throw new RuntimeException( "SHA-1 hash (" + computedHash + ") doesn't match expected hash (" + sha1Hash + ")." );
    }
    
    /**
     * Reads len bytes from the given RandomAccessFile at the given position
     * and returns a byte array containing the bytes we read.
     * @param raf
     * @param pos
     * @param len
     * @throws IOException - If the end of file is encounted before len bytes are read
     */
    public static byte[] read( final RandomAccessFile raf, final long pos, int len ) throws IOException {
        raf.seek( pos );
        
        byte[] buf = new byte[len];
        int offset = 0;
        int c;
        while( len > 0 ) {
            c = raf.read( buf, offset, len );
            if( c == -1 )
                throw new IOException( "End of file reached." );
            offset += c;
            len    -= c;
        }
        return buf;
    }

    /**
     * Reads len bytes from the given RandomAccessFile at the given position
     * and writes them to the given output stream.
     * 
     * @param raf
     * @param out
     * @param pos
     * @param len
     * @throws IOException
     */
    public static void copy( final RandomAccessFile raf, final OutputStream out, final long pos, int len ) throws IOException {
        raf.seek( pos );
        
        byte buf[] = new byte[4096];
        int c;
        while( len > 0 ) {
            c = raf.read( buf );
            if( c == -1 )
                throw new IOException( "End of file reached." );
            out.write( buf, 0, c );
            len -= c;
        }
    }
    
    /**
     * Returns a ZipFile or JarFile, depending on the extension of the given file.
     * @param path
     * @return
     * @throws IOException
     */
    public static ZipFile getZipFile( Path path ) throws IOException {
        String extension = Misc.getExtension( path.toString() );
        File f = path.toFile();
        switch( extension ) {
        case "zip":
            return new ZipFile( f );
        case "jar":
            return new JarFile( f );
        default:
            throw new RuntimeException( "Unrecognized extension." );
        }
    }
}
