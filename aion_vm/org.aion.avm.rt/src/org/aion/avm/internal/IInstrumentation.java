package org.aion.avm.internal;


/**
 * The interface required to support the Helper injected class which provides the global callout points from within
 * the instrumented code.
 * NOTE:  This is currently a work-in-progress for issue-308, directly mirroring the refactored implementation from
 * within Helper.
 */
public interface IInstrumentation {
    // The instrumentation instance associated with the given thread and also installed into the Helper of the currently-running DApp.
    public static final ThreadLocal<IInstrumentation> attachedThreadInstrumentation = new ThreadLocal<>();

    void enterNewFrame(ClassLoader contractLoader, long energyLeft, int nextHashCode, InternedClasses classWrappers);
    void exitCurrentFrame();

    <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input);
    org.aion.avm.shadow.java.lang.String wrapAsString(String input);
    org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t);
    Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg);
    void chargeEnergy(long cost) throws OutOfEnergyException;
    long energyLeft();

    /**
     * Used to get the next hash code and then increment it.
     * @return The next hash code, prior to the increment.
     */
    int getNextHashCodeAndIncrement();
    void setAbortState();
    public void clearAbortState();
    
    int getCurStackSize();
    int getCurStackDepth();
    void enterMethod(int frameSize);
    void exitMethod(int frameSize);
    void enterCatchBlock(int depth, int size);
    
    // Used to read/write hashcode value around internal calls (since we only update the next hash code if the callee succeeded).
    /**
     * Allows read-only access to the next hash code (this will NOT increment it).
     * 
     * @return The next hash code.
     */
    int peekNextHashCode();

    /**
     * Sets the next hash code to the given value.  This is used to update the hash code in a caller frame if a callee succeeds.
     * @param nextHashCode The hash code to use for the next object allocated.
     */
    void forceNextHashCode(int nextHashCode);
    
    void bootstrapOnly();

    /**
     * @return id the class has been loaded by the classloader associated to stackFrame
     */
    boolean isLoadedByCurrentClassLoader(Class userClass);
}
