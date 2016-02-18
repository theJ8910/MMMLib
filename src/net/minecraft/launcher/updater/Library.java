package net.minecraft.launcher.updater;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.mojang.launcher.versions.CompatibilityRule;
import com.mojang.launcher.versions.CompatibilityRule.Action;
import com.mojang.launcher.versions.ExtractRules;

import net.theJ89.util.OperatingSystem;
import net.theJ89.util.Platform;
import net.theJ89.util.Target;

public class Library {
    //Note: The Minecraft version info files offer downloads for libraries,
    //but for whatever reason the Minecraft Launcher doesn't even look at it.
    //I implement it here because it offers useful information about the
    //libraries (namely the SHA-1 hash and size) that are useful to know ahead of time.
    public static class LibraryDownloads {
        //A Java library will typically have "artifact" set but not "classifiers".
        //Native libraries will typically have "classifiers" set, but not natives.
        private Artifact                artifact;
        private Map< String, Artifact > classifiers;
        
        public Artifact getArtifact() {
            return this.artifact;
        }
        
        public Map< String, Artifact > getClassifiers() {
            return this.classifiers;
        }
    }
    
    private String                       name;
    private List<CompatibilityRule>      rules;
    private Map<OperatingSystem, String> natives;
    private ExtractRules                 extract;
    private LibraryDownloads             downloads;
    
    /**
     * Returns the name of the library.
     * This will be a string consisting of three parts separated by colons, following this convention:
     * <pre>base-package:top-level-package:version</pre>
     * For example:
     * <pre>org.lwjgl.lwjgl:lwjgl-platform:2.9.1</pre>
     * @return The library name.
     */
    public String getName() {
        return this.name;
    }
    
    public List<CompatibilityRule> getRules() {
        return this.rules;
    }
    
    public Map<OperatingSystem, String> getNatives() {
        return this.natives;
    }
    
    public ExtractRules getExtract() {
        return this.extract;
    }
    
    public LibraryDownloads getDownloads() {
        return this.downloads;
    }
    
    /**
     * Calls {@link #isCompatible(OperatingSystem, String, String)} with the current system as its target.
     * @return true if this system is compatible with this library, false otherwise.
     */
    public boolean isCompatible() {
        return this.isCompatible( Platform.getTarget() );
    }
    
    /**
     * Returns whether or not the library is compatible with the given target (OS, OS version, and architecture).
     * Generally, Java libraries are compatible with all targets.
     * Natives may only be compatible with certain targets.
     * @param target - The target.
     * @return true if compatible, false otherwise.
     */
    public boolean isCompatible( Target target ) {
        //No special rules means "yes"
        if( this.rules == null )
            return true;
        
        //Compatibile rules stack; the last compatible rule (in the order they're defined) determines whether or not this target is compatible or not.
        Action action = Action.ALLOW;
        for( CompatibilityRule rule : this.rules )
            if( rule.appliesTo( target ) )
                action = rule.getAction();
        
        return action == Action.ALLOW;
    }
    
    /**
     * Calls {@link #getPath(OperatingSystem)} with the current operating system.
     * @return The library path or null.
     */
    public Path getPath() {
        return getPath( Platform.getTarget() );
    }
    
    /**
     * Returns a relative path for this library and the given OS.
     * If this is a native library, a classifier taken from the "natives" map will be attached.
     * If the library does not contain a natives entry for the given operating system,
     * or compatibility rules indicate there is no library available, this function returns null.
     * @param name
     * @param os - The operating system for the desired natives.
     * @return The library path or null.
     */
    public Path getPath( Target target ) {
        //There is no path if this incompatible
        if( !this.isCompatible( target ) )
            return null;
        
        if( this.isNative() ) {
            //Native libraries use a classifier from the natives map.
            String classifier = this.natives.get( target.getOS() );
            
            if( classifier == null )
                return null;
            
            //Classifiers can sometimes contain ${arch}.
            //Substitute this with the target architecture.
            Map< String, String > m = new HashMap< String, String >();
            m.put( "arch", target.getArch() );
            StrSubstitutor ss = new StrSubstitutor( m );
            
            return getPathFromName( this.name, ss.replace( classifier ) );
        }
        
        return getPathFromName( this.name );
    }
    
    
    /**
     * Constructs a path for the Java library with the given name.
     * The path is constructed from the library name by replacing the dots in the first part with slashes,
     * then joining them together like so (where {0],{1}, and {2} are the first, second, and third parts of the name respectively):
     * <pre>{0}/{1}/{2}/{1}-{2}.jar</pre>
     * Example:
     * <pre>
     * org.lwjgl.lwjgl:lwjgl_util:2.9.1
     * becomes:
     * org/lwjgl/lwjgl/lwjgl_util/2.9.1/lwjgl_util-2.9.1.jar
     * </pre>
     * @return The library path.
     */
    public static Path getPathFromName( String name ) {
        String[] parts = name.split( ":" );
        return Paths.get( parts[0].replace( '.', '/' ), parts[1], parts[2], parts[1] + "-" + parts[2] + ".jar" );
    }
    
    /**
     * Constructs a path for the native library with the given name and given classifier.
     * The process for generating a path is generally the same as {#getPathFromName(String)},
     * but a classifier corresponding to the given operating system is attached:
     * <pre>{0}/{1}/{2}/{1}-{2}-{classifier}.jar</pre>
     * Example:
     * <pre>
     * org.lwjgl.lwjgl:lwjgl-platform:2.9.1
     * becomes:
     * org/lwjgl/lwjgl/lwjgl-platfrom/2.9.1/lwjgl-platfrom-2.9.1-natives-linux.jar
     * </pre>
     * @param name
     * @param classifier
     * @return The library path.
     */
    public static Path getPathFromName( String name, String classifier ) {
        String[] parts = name.split( ":" );
        return Paths.get( parts[0].replace( '.', '/' ), parts[1], parts[2], parts[1] + "-" + parts[2] + "-" + classifier + ".jar" );
    }
    
    /**
     * Returns true if this is a native library.
     * Returns false if this is a Java library.
     * @return
     */
    public boolean isNative() {
        return this.downloads.classifiers != null;
    }
}
