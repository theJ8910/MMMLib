package net.theJ89.util;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaLauncher {
    private static long    MAX_JAVA32_MEMORY = 4 * Size.GIGABYTE;
    private static Pattern ARGUMENT_REGEX    = Pattern.compile( "(?:\"([^\"]*)\")|(?:[^ ]+)" );
    
    //Java executable options
    private boolean             wantConsole;
    
    //JVM options
    private boolean             useConcMarkSweepGC;
    private boolean             useCMSIncrementalMode;
    private boolean             useAdaptiveSizePolicy;
    private Long                initialHeapSize;
    private Long                maxHeapSize;
    private Long                nurserySize;
    private Long                metaspaceSize;
    private List<String>        classPaths;
    private List<String>        libraryPaths;
    private List<String>        otherJVMArguments;
    
    //Program options
    private String              classname;
    private String              jarname;
    private List<String>        arguments;
    
    //Process options
    private Path                workingDirectory;
    private Map<String, String> environment;
    private boolean             inheritIO;
    
    public JavaLauncher() {
        this.wantConsole           = false;
        
        this.useConcMarkSweepGC    = false;
        this.useCMSIncrementalMode = false;
        this.useAdaptiveSizePolicy = false;
        
        this.initialHeapSize       = null;
        this.maxHeapSize           = null;
        this.nurserySize           = null;
        this.metaspaceSize         = null;
        this.classPaths            = null;
        this.libraryPaths          = null;
        this.otherJVMArguments     = null;
        
        this.classname             = null;
        this.arguments             = null;
        
        this.workingDirectory      = null;
        this.environment           = null;
        this.inheritIO             = true;
    }
    
    /**
     * Parses a string of command line arguments and returns them as a list.
     * The arguments are expected
     * @param arguments
     */
    public static List<String> parseArguments( final String argumentString ) {
        Matcher m = ARGUMENT_REGEX.matcher( argumentString );
        List<String> arguments = new ArrayList<String>();
        while( m.find() ) {
            String innerMatch = m.group(1);
            arguments.add( innerMatch == null ? m.group() : innerMatch );
        }
        return arguments;
    }
    
    /**
     * Sets whether or not java should be started with a console. Defaults to false.
     * @param wantConsole - If true, launch java with a console. If false, don't.
     */
    public void setWantConsole( final boolean wantConsole ) {
        this.wantConsole = wantConsole;
    }
    
    public boolean getWantConsole() {
        return this.wantConsole;
    }
    
    public void setUseConcMarkSweepGC( final boolean useConcMarkSweepGC ) {
        this.useConcMarkSweepGC = useConcMarkSweepGC;
    }
    
    public boolean getUseConcMarkSweepGC() {
        return this.useConcMarkSweepGC;
    }
    
    public void setUseCMSIncrementalMode( final boolean useCMSIncrementalMode ) {
        this.useCMSIncrementalMode = useCMSIncrementalMode;
    }
    
    public boolean getUseCMSIncrementalMode() {
        return this.useCMSIncrementalMode;
    }
    
    public void setUseAdaptiveSizePolicy( final boolean useAdaptiveSizePolicy ) {
        this.useAdaptiveSizePolicy = useAdaptiveSizePolicy;
    }
    
    public boolean getUseAdaptiveSizePolicy() {
        return this.useAdaptiveSizePolicy;
    }
    
    /**
     * Inline version of {@link #setClassPaths(List)}.
     * @param libraryPaths - Zero-to-many class paths. If none provided, sets class paths to null.
     */
    public void setClassPathsIL( final String... classPaths ) {
        if( classPaths.length == 0 )
            this.classPaths = null;
        
        ArrayList<String> list = new ArrayList<String>();
        for( String classPath : classPaths )
            list.add( classPath );
        this.classPaths = list;
    }
    
    /**
     * Set class paths. Java classes (.class) are loaded from here.
     * A classpath should be a path to a .jar file directory.
     * If the given string is null, no class paths are passed to the JVM.
     * @param classPaths
     */
    public void setClassPaths( final List<String> classPaths ) {
        this.classPaths = classPaths;
    }
    
    public List<String> getClassPaths() {
        return this.classPaths;
    }
    
    /**
     * Inline version of {@link #setLibraryPaths(List)}.
     * @param libraryPaths - Zero-to-many library directories. If none provided, sets library paths to null.
     */
    public void setLibraryPathsIL( final String... libraryPaths ) {
        if( libraryPaths.length == 0 )
            this.libraryPaths = null;
        
        ArrayList<String> list = new ArrayList<String>();
        for( String libraryPath : libraryPaths )
            list.add( libraryPath );
        this.libraryPaths = list;
    }
    
    /**
     * Sets native library directories.
     * This tells Java where in the system it can find native libraries (i.e. *.dll, *.so, etc).
     * If the given string is null, no library paths are passed to the JVM.
     * @param libraryPaths
     */
    public void setLibraryPaths( final List<String> libraryPaths ) {
        this.libraryPaths = libraryPaths;
    }
    
    public List<String> getLibraryPaths() {
        return this.libraryPaths;
    }
    
    /**
     * Sets the initial heap size in bytes.
     * The initial heap size determines how much memory the JVM can allocate before needing to resize the heap.
     * The given size must be greater than 1 megabyte and a multiple of 1024 bytes.
     * For 32-bit JVMs, the given size may not exceed 4 gigabytes.
     * If null is given, the default will be used.
     * @param initialHeapSize - The initial heap size to use, or null.
     */
    public void setInitialHeapSize( final Long initialHeapSize ) {
        if( initialHeapSize == null ) {
            this.initialHeapSize = null;
            return;
        }
        
        if( Platform.getBitness() == 32 && initialHeapSize > MAX_JAVA32_MEMORY )
            throw new IllegalArgumentException( "Given size cannot exceed 4 GiB on a 32-bit JVM." );
            
        if( initialHeapSize < Size.MEGABYTE )
            throw new IllegalArgumentException( "Given size must be at least 1 MiB." );
        
        if( initialHeapSize % Size.KILOBYTE != 0 )
            throw new IllegalArgumentException( "Given size must be a multiple of 1024." );
        
        this.initialHeapSize = initialHeapSize;
    }
    
    public Long getInitialHeapSize() {
        return this.initialHeapSize;
    }
    
    /**
     * Sets the max heap size in bytes.
     * The max heap size determines the total amount of memory the JVM is allowed to allocate; this often needs to be increased.
     * The given size must be greater than 1 megabyte and a multiple of 1024 bytes.
     * For 32-bit JVMs, the given size may not exceed 4 gigabytes.
     * If null is given, the default will be used.
     * @param maxHeapSize - The max heap size to use, or null.
     */
    public void setMaxHeapSize( final Long maxHeapSize ) {
        if( maxHeapSize == null ) {
            this.maxHeapSize = null;
            return;
        }
        
        if( Platform.getBitness() == 32 && maxHeapSize > MAX_JAVA32_MEMORY )
            throw new IllegalArgumentException( "Given size cannot exceed 4 GiB on a 32-bit JVM." );
        
        if( maxHeapSize < Size.MEGABYTE )
            throw new IllegalArgumentException( "Given size must be at least 1 MiB." );
        
        if( maxHeapSize % Size.KILOBYTE != 0 )
            throw new IllegalArgumentException( "Given size must be a multiple of 1024." );
        
        this.maxHeapSize = maxHeapSize;
    }
    
    public Long getMaxHeapSize() {
        return this.maxHeapSize;
    }
    
    /**
     * Size of the nursery (young generation) in bytes.
     * For 32-bit JVMs, this must not exceed 4 gigabytes.
     * This should typically be between 1/4 to 1/2 of the max heap size.
     * If null is given, the default will be used.
     * @param nurserySize - The size of the nursery to use, or null.
     */
    public void setNurserySize( final Long nurserySize ) {
        if( nurserySize == null ) {
            this.nurserySize = null;
            return;
        }
        
        if( Platform.getBitness() == 32 && nurserySize > MAX_JAVA32_MEMORY )
            throw new IllegalArgumentException( "Given size cannot exceed 4 GiB on a 32-bit JVM." );
        
        this.nurserySize = nurserySize;
    }
    
    public Long getNurserySize() {
        return this.nurserySize;
    }
    
    /**
     * Sets the metaspace / permgen size in bytes.
     * For 32-bit JVMs, this must not exceed 4 gigabytes.
     * This determines how much memory the JVM can allocate for class metadata.
     * If null is given, the default will be used.
     * In Java 1.8+, by default this is unlimited.
     * In earlier versions of Java, this defaults to 32-64 MB.
     * @param metaspaceSize - The metaspace / permgen size or null.
     */
    public void setMetaspaceSize( final Long metaspaceSize ) {
        if( metaspaceSize == null ) {
            this.metaspaceSize = null;
            return;
        }
        
        if( Platform.getBitness() == 32 && metaspaceSize > MAX_JAVA32_MEMORY )
            throw new IllegalArgumentException( "Given size cannot exceed 4GiB on a 32-bit JVM." );
        
        this.metaspaceSize = metaspaceSize;
    }
    
    public Long getMetaspaceSize() {
        return this.metaspaceSize;
    }
    
    /**
     * Inline version of {@link #setOtherJVMArguments(List)}.
     * @param arguments - Zero-to-many arguments. If none provided, sets other JVM arguments to null.
     */
    public void setOtherJVMArgumentsIL( final String... otherJVMArguments ) {
        if( otherJVMArguments.length == 0 )
            this.otherJVMArguments = null;
        
        ArrayList<String> list = new ArrayList<String>();
        for( String otherJVMArgument : otherJVMArguments )
            list.add( otherJVMArgument );
        this.otherJVMArguments = list;
    }
    
    /**
     * Use this to provide additional JVM arguments that this class may not provide.
     * @param otherJVMArguments
     */
    public void setOtherJVMArguments( final List<String> otherJVMArguments ) {
        this.otherJVMArguments = otherJVMArguments;
    }
    
    public List<String> getOtherJVMArguments() {
        return this.otherJVMArguments;
    }
    
    /**
     * Sets the name of the class we want Java to run.
     * This must name a class with a "public static void main( String[] args ) { ... }" method.
     * @param classname
     */
    public void setClassName( final String classname) {
        this.classname = classname;
    }
    
    public String getClassName() {
        return this.classname;
    }
    
    /**
     * Set the name of the jar we want Java to run.
     * Note: A classname or jarname (not both) must be set or Java will fail to launch.
     * @param jarname
     */
    public void setJarName( final String jarname ) {
        this.jarname = jarname;
    }
    
    public String getJarName() {
        return this.jarname;
    }
    
    /**
     * Inline version of {@link #setArguments(List)}.
     * @param arguments - Zero-to-many arguments. If none provided, sets arguments to null.
     */
    public void setArgumentsIL( final String... arguments ) {
        if( arguments.length == 0 )
            this.arguments = null;
        
        ArrayList<String> list = new ArrayList<String>();
        for( String argument : arguments )
            list.add( argument );
        this.arguments = list;
    }
    
    /**
     * Use this to set the arguments that will be provided to the main() method of the java class you're running.
     * @param arguments
     */
    public void setArguments( final List<String> arguments ) {
        this.arguments = arguments;
    }
    
    public List<String> getArguments() {
        return this.arguments;
    }
    
    /**
     * Sets the working directory the launched executable should use.
     * If directory is null, the default value is used (this process's working directory).
     * @param directory - The directory to use, or null.
     */
    public void setWorkingDirectory( final Path directory ) {
        this.workingDirectory = directory;
    }
    
    /**
     * Returns the working directory the launched application will use, or null if the default value will be used.
     * @return - The directory to use, or null.
     */
    public Path getWorkingDirectory() {
        return this.workingDirectory;
    }
    
    /**
     * Inline version of {@link #setEnvironment(List)}.
     * @param environment - Zero-to-many arguments in pairs of two (i.e. setEnvironmentIL( K, V, K, V, ...)). If none provided, sets environment to null.
     * @throws InvalidParameterException - If arguments are not provided in pairs.
     */
    public void setEnvironmentIL( final String... environment ) {
        if( environment.length == 0 ) {
            this.environment = null;
            return;
        }
        
        if( (environment.length & 1) != 0 )
            throw new InvalidParameterException( "Wrong number of arguments provided. Strings must be provided in pairs of two (i.e. setEnvironmentIL( K, V, K, V, ... ))" );
        
        HashMap< String, String > map = new HashMap< String, String >();
        for( int i = 0; i < environment.length; i += 2 )
            map.put( environment[i], environment[i+1] );
        this.environment = map;
    }
    
    /**
     * When launch() is called, a copy of this process's environment will be provided to the new java process.
     * This function sets a map of environment variables to add / replace.
     * @param environment
     */
    public void setEnvironment( final Map<String,String> environment ) {
        this.environment = environment;
    }
    
    public Map<String,String> getEnvironment() {
        return this.environment;
    }
    
    /**
     * Sets whether or not the new java process should use this process's stdout, stdin, and stderr streams.
     * By default, new java processes spawned with JavaLauncher inherit I/O.
     * @param inheritIO - true to inherit I/O, false otherwise.
     */
    public void setInheritIO( final boolean inheritIO ) {
        this.inheritIO = inheritIO;
    }
    
    public boolean getInheritIO() {
        return this.inheritIO;
    }
    
    /**
     * Launches the executable.
     * @throws IOException 
     */
    public Process launch() throws IOException {
        List<String> commandLine = new ArrayList< String >();
        
        //Java executable
        commandLine.add( Platform.getJavaExecutable( this.wantConsole ).toString() );
        
        //Performance-related switches
        if( this.useConcMarkSweepGC )
            commandLine.add( "-XX:+UseConcMarkSweepGC" );
        if( this.useCMSIncrementalMode )
            commandLine.add( "-XX:+CMSIncrementalMode" );
        if( this.useAdaptiveSizePolicy )
            commandLine.add( "-XX:+UseAdaptiveSizePolicy" );
        
        //Set starting and max memory
        if( this.initialHeapSize != null )
            commandLine.add( "-Xms" + Size.toString( this.initialHeapSize, Size.COMPACT_BINARY_SUFFIXES ) );
        if( this.maxHeapSize != null )
            commandLine.add( "-Xmx" + Size.toString( this.maxHeapSize, Size.COMPACT_BINARY_SUFFIXES ) );
        if( this.nurserySize != null )
            commandLine.add( "-Xmn" + Size.toString( this.nurserySize, Size.COMPACT_BINARY_SUFFIXES ) );
        
        //Set metaspace / permgen size
        if( this.metaspaceSize != null ) {
            if( Platform.getJavaVersion().atLeast( JavaVersion.JAVA_1_8 ) )
                commandLine.add( "-XX:MaxMetaspaceSize=" + Size.toString( this.metaspaceSize, Size.COMPACT_BINARY_SUFFIXES ) );
            else
                commandLine.add( "-XX:MaxPermSize=" + Size.toString( this.metaspaceSize, Size.COMPACT_BINARY_SUFFIXES ) );
        }
        
        //Construct JVM class path from list of class paths we may have been given
        if( this.classPaths != null ) {
            commandLine.add( "-cp" );
            commandLine.add( String.join( ";", this.classPaths ) );
        }
        
        //If we had to set the operating system to Windows 10 manually,
        //do the same thing for the instance of Java we're launching.
        if( Platform.getWinTenHack() ) {
            commandLine.add( "-Dos.name=Windows 10" );
            commandLine.add( "-Dos.version=10.0" );
        }
        
        //Construct JVM library path from list of library paths we may have been given
        if( this.libraryPaths != null )
            commandLine.add( "\"-Djava.library.path=" + String.join( ";", this.libraryPaths ) + "\"" );
        
        //Add any other JVM arguments we may have been given
        if( this.otherJVMArguments != null )
            commandLine.addAll( otherJVMArguments );
        
        //Set the name of the class to be launched.
        if( this.classname != null ) {
            if( this.jarname != null ) {
                throw new RuntimeException( "You cannot set both a classname and a jarname." );
            } else {
                commandLine.add( this.classname );
            }
        } else if( this.jarname != null ) {
            commandLine.add( "-jar" );
            commandLine.add( this.jarname );
        } else {
            throw new RuntimeException( "You must set a classname or a jarname." );
        }
        
        
        //Add program arguments
        if( this.arguments != null )
            commandLine.addAll( this.arguments );
        
        //Make a process builder and provide it our command line
        ProcessBuilder pb = new ProcessBuilder();
        pb.command( commandLine );
        
        //Set working directory
        if( this.workingDirectory != null )
            pb.directory( workingDirectory.toFile() );
        
        //Set up environment
        if( this.environment != null ) {
            Map<String,String> environment = pb.environment();
            environment.putAll( this.environment );
        }
        
        //Finally, launch Java.
        if( this.inheritIO )
            pb.inheritIO();
        return pb.start();
    }
}
