package net.theJ89.util;

public class Size2D {
    int width;
    int height;
    
    public Size2D() {
        this.width  = 0;
        this.height = 0;
    }
    
    public Size2D( int width, int height ) {
        this.width  = width;
        this.height = height;
    }
    
    public void setWidth( int width ) {
        this.width = width;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setHeight( int height ) {
        this.height = height;
    }
    
    public int getHeight() {
        return this.height;
    }
}
