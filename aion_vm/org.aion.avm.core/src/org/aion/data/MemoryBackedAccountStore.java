package org.aion.data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.util.ByteArrayWrapper;


public class MemoryBackedAccountStore implements IAccountStore {
    private BigInteger balance = BigInteger.ZERO;
    private long nonce = 0;
    private byte[] code = null;
    private final Map<ByteArrayWrapper, byte[]> storage = new HashMap<>();

    @Override
    public byte[] getCode() {
        return this.code;
    }

    @Override
    public void setCode(byte[] code) {
        this.code = code;
    }

    @Override
    public BigInteger getBalance() {
        return this.balance;
    }

    @Override
    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    @Override
    public long getNonce() {
        return this.nonce;
    }

    @Override
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    @Override
    public byte[] getData(byte[] key) {
        return this.storage.get(new ByteArrayWrapper(key));
    }

    @Override
    public void setData(byte[] key, byte[] value) {
        this.storage.put(new ByteArrayWrapper(key), value);
    }

    @Override
    public Map<ByteArrayWrapper, byte[]> getStorageEntries() {
        return this.storage;
    }
}
