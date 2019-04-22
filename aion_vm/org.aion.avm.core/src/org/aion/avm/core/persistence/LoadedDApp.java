package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.List;

import org.aion.avm.core.util.DebugNameResolver;
import org.aion.avm.internal.AvmThrowable;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadowapi.avm.Blockchain;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InternedClasses;
import org.aion.avm.internal.MethodAccessException;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.internal.UncaughtException;


/**
 * Manages the organization of a DApp's root classes serialized shape as well as how to kick-off the serialization/deserialization
 * operations of the entire object graph (since both operations start at the root classes defined within the DApp).
 * Only the class statics and maybe a few specialized instances will be populated here.  The graph is limited by installing instance
 * stubs into fields pointing at objects.
 * 
 * We will store the data for all classes in a single storage key to avoid small IO operations when they are never used partially.
 * 
 * This class was originally just used to house the top-level calls related to serializing and deserializing a DApp but now it also
 * contains information relating to the DApp, in order to accomplish this.
 * Specifically, it now contains the ClassLoader, information about the class instances, and the cache of any reflection data.
 * NOTE:  It does NOT contain any information about the data currently stored within the Class objects associated with the DApp, nor
 * does it have any information about persisted aspects of the DApp (partly because it doesn't know anything about storage versioning).
 * 
 * NOTE:  Nothing here should be eagerly cached or looked up since the external caller is responsible for setting up the environment
 * such that it is fully usable.  Attempting to eagerly interact with it before then might not be safe.
 */
public class LoadedDApp {
    private static final Method SERIALIZE_SELF;
    private static final Method DESERIALIZE_SELF;
    private static final Field FIELD_READ_INDEX;
    
    static {
        try {
            Class<?> shadowObject = org.aion.avm.shadow.java.lang.Object.class;
            SERIALIZE_SELF = shadowObject.getDeclaredMethod("serializeSelf", Class.class, IObjectSerializer.class);
            DESERIALIZE_SELF = shadowObject.getDeclaredMethod("deserializeSelf", Class.class, IObjectDeserializer.class);
            FIELD_READ_INDEX = shadowObject.getDeclaredField("readIndex");
        } catch (NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            // These are statically defined so can't fail.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public final ClassLoader loader;
    private final Class<?>[] sortedClasses;
    private final String originalMainClassName;
    private final SortedFieldCache fieldCache;

    // Other caches of specific pieces of data which are lazily built.
    private final Class<?> helperClass;
    public final IRuntimeSetup runtimeSetup;
    private Class<?> blockchainRuntimeClass;
    private Class<?> mainClass;
    private Field runtimeBlockchainRuntimeField;
    private Method mainMethod;
    private long loadedBlockNum;
    private final boolean preserveDebuggability;

    /**
     * Creates the LoadedDApp to represent the classes related to DApp at address.
     * 
     * @param loader The class loader to look up shape.
     * @param classes The list of classes to populate.
     */
    public LoadedDApp(ClassLoader loader, List<Class<?>> classes, String originalMainClassName, boolean preserveDebuggability) {
        this.loader = loader;
        // Note that the storage system defines the classes as being sorted alphabetically.
        this.sortedClasses = classes.stream()
                .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
                .toArray(Class[]::new);
        this.originalMainClassName = originalMainClassName;
        this.fieldCache = new SortedFieldCache(this.loader, SERIALIZE_SELF, DESERIALIZE_SELF, FIELD_READ_INDEX);
        this.preserveDebuggability = preserveDebuggability;
        // We also know that we need the runtimeSetup, meaning we also need the helperClass.
        try {
            String helperClassName = Helper.RUNTIME_HELPER_NAME;
            this.helperClass = this.loader.loadClass(helperClassName);
            RuntimeAssertionError.assertTrue(helperClass.getClassLoader() == this.loader);
            this.runtimeSetup = (IRuntimeSetup) helperClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            // We require that this be instantiated in this way.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    /**
     * Requests that the Classes in the receiver be populated with data from the rawGraphData.
     * NOTE:  The caller is expected to manage billing - none of that is done in here.
     * 
     * @param internedClassMap The interned classes, in case class references need to be instantiated.
     * @param rawGraphData The data from which to read the graph (note that this must encompass all and only a completely serialized graph.
     * @return The nextHashCode serialized within the graph.
     */
    public int loadEntireGraph(InternedClasses internedClassMap, byte[] rawGraphData) {
        ByteBuffer inputBuffer = ByteBuffer.wrap(rawGraphData);
        List<Object> existingObjectIndex = null;
        StandardGlobalResolver resolver = new StandardGlobalResolver(internedClassMap, this.loader);
        StandardNameMapper classNameMapper = new StandardNameMapper();
        int nextHashCode = Deserializer.deserializeEntireGraphAndNextHashCode(inputBuffer, existingObjectIndex, resolver, this.fieldCache, classNameMapper, this.sortedClasses);
        return nextHashCode;
    }

    /**
     * Requests that the Classes in the receiver be walked and all referenced objects be serialized into a graph.
     * NOTE:  The caller is expected to manage billing - none of that is done in here.
     * 
     * @param nextHashCode The nextHashCode to serialize into the graph so that this can be resumed in the future.
     * @param maximumSizeInBytes The size limit on the serialized graph size (this is a parameter for testing but also to allow the caller to impose energy-based limits).
     * @return The enter serialized object graph.
     */
    public byte[] saveEntireGraph(int nextHashCode, int maximumSizeInBytes) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(maximumSizeInBytes);
        List<Object> out_instanceIndex = null;
        List<Integer> out_calleeToCallerIndexMap = null;
        StandardGlobalResolver resolver = new StandardGlobalResolver(null, this.loader);
        StandardNameMapper classNameMapper = new StandardNameMapper();
        Serializer.serializeEntireGraph(outputBuffer, out_instanceIndex, out_calleeToCallerIndexMap, resolver, this.fieldCache, classNameMapper, nextHashCode, this.sortedClasses);
        
        byte[] finalBytes = new byte[outputBuffer.position()];
        System.arraycopy(outputBuffer.array(), 0, finalBytes, 0, finalBytes.length);
        return finalBytes;
    }

    public ReentrantGraph captureStateAsCaller(int nextHashCode, int maxGraphSize) {
        StandardGlobalResolver resolver = new StandardGlobalResolver(null, this.loader);
        StandardNameMapper classNameMapper = new StandardNameMapper();
        return ReentrantGraph.captureCallerState(resolver, this.fieldCache, classNameMapper, maxGraphSize, nextHashCode, this.sortedClasses);
    }

    public ReentrantGraph captureStateAsCallee(int updatedNextHashCode, int maxGraphSize) {
        StandardGlobalResolver resolver = new StandardGlobalResolver(null, this.loader);
        StandardNameMapper classNameMapper = new StandardNameMapper();
        return ReentrantGraph.captureCalleeState(resolver, this.fieldCache, classNameMapper, maxGraphSize, updatedNextHashCode, this.sortedClasses);
    }

    public void commitReentrantChanges(InternedClasses internedClassMap, ReentrantGraph callerState, ReentrantGraph calleeState) {
        StandardGlobalResolver resolver = new StandardGlobalResolver(internedClassMap, this.loader);
        StandardNameMapper classNameMapper = new StandardNameMapper();
        callerState.commitChangesToState(resolver, this.fieldCache, classNameMapper, this.sortedClasses, calleeState);
    }

    public void revertToCallerState(InternedClasses internedClassMap, ReentrantGraph callerState) {
        StandardGlobalResolver resolver = new StandardGlobalResolver(internedClassMap, this.loader);
        StandardNameMapper classNameMapper = new StandardNameMapper();
        callerState.revertChangesToState(resolver, this.fieldCache, classNameMapper, this.sortedClasses);
    }

    /**
     * Attaches an IBlockchainRuntime instance to the Helper class (per contract) so DApp can
     * access blockchain related methods.
     *
     * Returns the previously attached IBlockchainRuntime instance if one existed, or null otherwise.
     *
     * NOTE:  The current implementation is mostly cloned from Helpers.attachBlockchainRuntime() but we will inline/cache more of this,
     * over time, and that older implementation is only used by tests (which may be ported to use this).
     *
     * @param runtime The runtime to install in the DApp.
     * @return The previously attached IBlockchainRuntime instance or null if none.
     */
    public IBlockchainRuntime attachBlockchainRuntime(IBlockchainRuntime runtime) {
        try {
            Field field = getBlochchainRuntimeField();
            IBlockchainRuntime previousBlockchainRuntime = (IBlockchainRuntime) field.get(null);
            field.set(null, runtime);
            return previousBlockchainRuntime;
        } catch (Throwable t) {
            // Errors at this point imply something wrong with the installation so fail.
            throw RuntimeAssertionError.unexpected(t);
        }
    }

    /**
     * Calls the actual entry-point, running the whatever was setup in the attached blockchain runtime as a transaction and return the result.
     * 
     * @return The data returned from the transaction (might be null).
     * @throws OutOfEnergyException The transaction failed since the permitted energy was consumed.
     * @throws Exception Something unexpected went wrong with the invocation.
     */
    public byte[] callMain() throws Throwable {
        try {
            Method method = getMainMethod();
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new MethodAccessException("main method not static");
            }

            ByteArray rawResult = (ByteArray) method.invoke(null);
            return (null != rawResult)
                    ? rawResult.getUnderlying()
                    : null;
        } catch (ClassNotFoundException | SecurityException | ExceptionInInitializerError e) {
            // should have been handled during CREATE.
            RuntimeAssertionError.unexpected(e);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new MethodAccessException(e);

        } catch (InvocationTargetException e) {
            // handle the real exception
            if (e.getTargetException() instanceof UncaughtException) {
                handleUncaughtException(e.getTargetException().getCause());
            } else {
                handleUncaughtException(e.getTargetException());
            }
        }

        return null;
    }

    /**
     * Forces all the classes defined within this DApp to be loaded and initialized (meaning each has its &lt;clinit&gt; called).
     * This is called during the create action to force the DApp initialization code to be run before it is stripped off for
     * long-term storage.
     */
    public void forceInitializeAllClasses() throws Throwable {
        for (Class<?> clazz : this.sortedClasses) {
            try {
                Class<?> initialized = Class.forName(clazz.getName(), true, this.loader);
                // These must be the same instances we started with and they must have been loaded by this loader.
                RuntimeAssertionError.assertTrue(clazz == initialized);
                RuntimeAssertionError.assertTrue(initialized.getClassLoader() == this.loader);
            } catch (ClassNotFoundException e) {
                // This error would mean that this is assembled completely incorrectly, which is a static error in our implementation.
                RuntimeAssertionError.unexpected(e);

            } catch (SecurityException e) {
                // This would mean that the shadowing is not working properly.
                RuntimeAssertionError.unexpected(e);

            } catch (ExceptionInInitializerError e) {
                // handle the real exception
                handleUncaughtException(e.getException());
            } catch (Throwable t) {
                // Some other exceptions can float out from the user clinit, not always wrapped in ExceptionInInitializerError.
                handleUncaughtException(t);
            }
        }
    }

    /**
     * The exception could be any {@link org.aion.avm.internal.AvmThrowable}, any {@link java.lang.RuntimeException},
     * or a {@link org.aion.avm.exceptionwrapper.java.lang.Throwable}.
     */
    private void handleUncaughtException(Throwable cause) throws Throwable {
        // thrown by us
        if (cause instanceof AvmThrowable) {
            throw cause;

            // thrown by runtime, but is never handled
        } else if ((cause instanceof RuntimeException) || (cause instanceof Error)) {
            throw new UncaughtException(cause);

            // thrown by users
        } else if (cause instanceof org.aion.avm.exceptionwrapper.org.aion.avm.shadow.java.lang.Throwable) {
            // Note that we will need to unwrap this since the wrapper doesn't actually communicate anything, just being
            // used to satisfy Java exception relationship requirements (the user code populates the wrapped object).
            throw new UncaughtException(((org.aion.avm.exceptionwrapper.org.aion.avm.shadow.java.lang.Throwable) cause).unwrap().toString(), cause);

        } else {
            RuntimeAssertionError.unexpected(cause);
        }
    }

    /**
     * Called before the DApp is about to be put into a cache.  This is so it can put itself into a "resumable" state.
     */
    public void cleanForCache() {
        // TODO (AKI-104): Implement - we want to wipe the statics so it no longer keeps its graph alive.
    }


    private Class<?> loadBlockchainRuntimeClass() throws ClassNotFoundException {
        Class<?> runtimeClass = this.blockchainRuntimeClass;
        if (null == runtimeClass) {
            String runtimeClassName = Blockchain.class.getName();
            runtimeClass = this.loader.loadClass(runtimeClassName);
            RuntimeAssertionError.assertTrue(runtimeClass.getClassLoader() == this.loader);
            this.blockchainRuntimeClass = runtimeClass;
        }
        return runtimeClass;
    }

    private Class<?> loadMainClass() throws ClassNotFoundException {
        Class<?> mainClass = this.mainClass;
        if (null == mainClass) {
            String mappedUserMainClass = DebugNameResolver.getUserPackageDotPrefix(this.originalMainClassName, this.preserveDebuggability);
            mainClass = this.loader.loadClass(mappedUserMainClass);
            RuntimeAssertionError.assertTrue(mainClass.getClassLoader() == this.loader);
            this.mainClass = mainClass;
        }
        return mainClass;
    }

    private Field getBlochchainRuntimeField() throws ClassNotFoundException, NoSuchFieldException, SecurityException  {
        Field runtimeBlockchainRuntimeField = this.runtimeBlockchainRuntimeField;
        if (null == runtimeBlockchainRuntimeField) {
            Class<?> runtimeClass = loadBlockchainRuntimeClass();
            runtimeBlockchainRuntimeField = runtimeClass.getField("blockchainRuntime");
            this.runtimeBlockchainRuntimeField = runtimeBlockchainRuntimeField;
        }
        return runtimeBlockchainRuntimeField;
    }

    private Method getMainMethod() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Method mainMethod = this.mainMethod;
        if (null == mainMethod) {
            Class<?> clazz = loadMainClass();
            mainMethod = clazz.getMethod("avm_main");
            this.mainMethod = mainMethod;
        }
        return mainMethod;
    }

    /**
     * Dump the transformed class files of the loaded Dapp.
     * The output class files will be put under {@param path}.
     *
     * @param path The runtime to install in the DApp.
     */
    public void dumpTransformedByteCode(String path){
        AvmClassLoader appLoader = (AvmClassLoader) loader;
        for (Class<?> clazz : this.sortedClasses){
            byte[] bytecode = appLoader.getUserClassBytecode(clazz.getName());
            String output = path + "/" + clazz.getName() + ".class";
            Helpers.writeBytesToFile(bytecode, output);
        }
    }

    public void setLoadedBlockNum(long loadedBlockNum) {
        this.loadedBlockNum = loadedBlockNum;
    }

    public long getLoadedBlockNum() {
        return loadedBlockNum;
    }
}
