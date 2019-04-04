package org.aion.avm.jni;

import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.types.Address;
import org.aion.avm.core.NodeEnvironment;
import java.math.BigInteger;

import java.util.Set;

/**
 * JNI binding of the kernel interface.
 */
public class NativeKernelInterface implements KernelInterface {

    private static final String libraryName = "avmjni";

    // load the native library
    static {
        System.loadLibrary(libraryName);
    }

    // store the pointer of a native KernelInterface object.
    private long handle;

    public NativeKernelInterface(long handle) {
        this.handle = handle;
    }

    public void touchAccount(byte[] addr, int index_of_substate) {
        touchAccount(handle, addr, index_of_substate);
    }

    public byte[] sendSignal(int sig_num) {
        return sendSignal(handle, sig_num);
    }

    @Override
    public void createAccount(Address address) {
        createAccount(handle, address.toBytes());
    }

    @Override
    public boolean hasAccountState(Address address) {
        return hasAccountState(handle, address.toBytes());
    }

    @Override
    public void putCode(Address address, byte[] code) {
        putCode(handle, address.toBytes(), code);
    }

    @Override
    public byte[] getCode(Address address) {
        return getCode(handle, address.toBytes());
    }

    @Override
    public void putStorage(Address address, byte[] key, byte[] value) {
        putStorage(handle, address.toBytes(), key, value);
    }

    @Override
    public byte[] getStorage(Address address, byte[] key) {
        return getStorage(handle, address.toBytes(), key);
    }

    @Override
    public void deleteAccount(Address address) {
        deleteAccount(handle, address.toBytes());
    }

    @Override
    public boolean accountNonceEquals(Address address, BigInteger nonce) {
        return nonce.compareTo(this.getNonce(address)) == 0;
    }

    @Override
    public BigInteger getBalance(Address address) {
        byte[] balance = getBalance(handle, address.toBytes());
        return new BigInteger(1, balance);
    }

    @Override
    public void adjustBalance(Address address, BigInteger delta) {
        System.out.println(String.format("avm adjust balance: %d", delta.longValue()));
        if (delta.signum() > 0) {
            increaseBalance(handle, address.toBytes(), delta.toByteArray());
        } else if (delta.signum() < 0) {
            decreaseBalance(handle, address.toBytes(), delta.negate().toByteArray());
        }
    }

    @Override
    public BigInteger getNonce(Address address) {
        return BigInteger.valueOf(getNonce(handle, address.toBytes()));
    }

    @Override
    public void incrementNonce(Address address) {
        incrementNonce(handle, address.toBytes());
    }

    @Override
    public boolean accountBalanceIsAtLeast(Address address, BigInteger amount) {
        return getBalance(address).compareTo(amount) >= 0;
    }
    
    @Override
    public boolean isValidEnergyLimitForNonCreate(long energyLimit) {
      return energyLimit > 0;
    }

    @Override
    public boolean isValidEnergyLimitForCreate(long energyLimit) {
      return energyLimit > 0;
    }

    @Override
    public boolean destinationAddressIsSafeForThisVM(Address address) {
        byte[] code = getCode(address);
        return (code == null) || (code.length == 0) || (address.toBytes()[0] == 0x0f);
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {
        throw new AssertionError("No equivalent concept in the Avm.");
    }

    @Override
    public void payMiningFee(Address address, BigInteger fee) {
        // This method may have special logic in the kernel. Here it is just adjustBalance.
        adjustBalance(address, fee);
    }

    @Override
    public void refundAccount(Address address, BigInteger amount) {
        // This method may have special logic in the kernel. Here it is just adjustBalance.
        adjustBalance(address, amount);
    }

    @Override
    public void deductEnergyCost(Address address, BigInteger cost) {
        // This method may have special logic in the kernel. Here it is just adjustBalance.
        adjustBalance(address, cost);
    }

    @Override
    public void removeStorage(Address address, byte[] key) {
        throw new AssertionError("This class does not implement this method.");
    }

    @Override
    public KernelInterface makeChildKernelInterface() {
        return new NativeKernelInterface(handle);
    }

    @Override
    public byte[] getObjectGraph(Address a) {
        return new byte[0x00];
    }

    @Override
    public void putObjectGraph(Address a, byte[] data) {

    }

    @Override
    public Set<byte[]> getTouchedAccounts() {
        throw new AssertionError("This class does not implement this method.");
    }

    @Override
    public void commitTo(KernelInterface target) { }

    @Override
    public void commit() { }

    public static native void createAccount(long handle, byte[] address);

    public static native boolean hasAccountState(long handle, byte[] address);

    public static native void putCode(long handle, byte[] address, byte[] code);

    public static native byte[] getCode(long handle, byte[] address);

    public static native void putStorage(long handle, byte[] address, byte[] key, byte[] value);

    public static native byte[] getStorage(long handle, byte[] address, byte[] key);

    public static native void deleteAccount(long handle, byte[] address);

    public static native byte[] getBalance(long handle, byte[] address);

    public static native void increaseBalance(long handle, byte[] address, byte[] delta);

    public static native void decreaseBalance(long handle, byte[] address, byte[] delta);

    public static native long getNonce(long handle, byte[] address);

    public static native void incrementNonce(long handle, byte[] address);

    public static native void touchAccount(long handle, byte[] address, int idx);

    public static native byte[] sendSignal(long handle, int sig);
}
