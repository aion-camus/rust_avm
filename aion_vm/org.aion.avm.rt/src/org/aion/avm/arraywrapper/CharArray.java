package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class CharArray extends Array {

    private char[] underlying;

    /**
     * Static CharArray factory
     *
     * After instrumentation, NEWARRAY bytecode (with char as type) will be replaced by a INVOKESTATIC to
     * this method.
     *
     * @param size Size of the char array
     *
     * @return New empty char array wrapper
     */
    public static CharArray initArray(int size){
        chargeEnergy(size * ArrayElement.CHAR.getEnergy());
        return new CharArray(size);
    }

    @Override
    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public char get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, char val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    @Override
    public IObject avm_clone() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.CharArray_avm_clone + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length());
        lazyLoad();
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Internal Helper
    //========================================================

    public CharArray(int c) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.CharArray_avm_constructor);
        this.underlying = new char[c];
    }

    public CharArray(char[] underlying) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.CharArray_avm_constructor_1);
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public char[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    @Override
    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (char[]) u;
    }

    @Override
    public java.lang.Object getUnderlyingAsObject(){
        lazyLoad();
        return underlying;
    }

    @Override
    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

    public CharArray(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(CharArray.class, deserializer);

        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new char[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readChar();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(CharArray.class, serializer);

        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeChar(this.underlying[i]);
        }
    }
}
