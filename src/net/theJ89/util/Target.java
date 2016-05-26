package net.theJ89.util;

//A target encapsulates an operating system (e.g. Windows), OS version (e.g. XP), and architecture (e.g. 32-bit, 64-bit)
public class Target {
    private OperatingSystem os;
    private String          version;
    private String          arch;
    
    public Target( OperatingSystem os, String version, String arch) {
        this.os      = os;
        this.version = version;
        this.arch    = arch;
    }
    
    public OperatingSystem getOS() {
        return this.os;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String getArch() {
        return this.arch;
    }
    
    @Override
    public String toString() {
        return String.format( "%s %s (%s)", this.os.getName(), this.version, this.arch );
    }
}
