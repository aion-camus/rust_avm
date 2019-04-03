package org.aion.avm.jni;

import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.types.Address;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.math.BigInteger;

public class Substate implements KernelInterface {
    final private KernelInterface parent;
    private final List<Consumer<KernelInterface>> writeLog;

    public Substate(Substate parent) {
        this.parent = parent;
        this.writeLog = new ArrayList<>();
    }

    @Override
    public void createAccount(Address address) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.createAccount(address);
        };
        writeLog.add(write);
    }

    @Override
    public boolean hasAccountState(Address address) {
        return this.parent.hasAccountState(address);
    }

    @Override
    public void putCode(Address address, byte[] code) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.putCode(address, code);
        };
        writeLog.add(write);
    }

    @Override
    public byte[] getCode(Address address) {
        return this.parent.getCode(address);
    }

    @Override
    public void putStorage(Address address, byte[] key, byte[] value) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.putStorage(address, key, value);
        };
        writeLog.add(write);
    }

    @Override
    public byte[] getStorage(Address address, byte[] key) {
        return this.parent.getStorage(address, key);
    }

    @Override
    public void deleteAccount(Address address) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.deleteAccount(address);
        };
        writeLog.add(write);
    }

    @Override
    public boolean accountNonceEquals(Address address, BigInteger nonce) {
        return this.parent.accountNonceEquals(address, nonce);
    }

    @Override
    public BigInteger getBalance(Address address) {
        return this.parent.getBalance(address);
    }

    @Override
    public void adjustBalance(Address address, BigInteger delta) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.adjustBalance(address, delta);
        };
        writeLog.add(write);
    }

    @Override
    public BigInteger getNonce(Address address) {
        return this.parent.getNonce(address);
    }

    @Override
    public void incrementNonce(Address address) {
        Consumer<KernelInterface> write = (kernel) -> {
            kernel.incrementNonce(address);
        };
        writeLog.add(write);
    }

    @Override
    public boolean accountBalanceIsAtLeast(Address address, BigInteger amount) {
        return this.parent.accountBalanceIsAtLeast(address, amount);
    }
    
    @Override
    public boolean isValidEnergyLimitForNonCreate(long energyLimit) {
        return this.parent.isValidEnergyLimitForNonCreate(energyLimit);
    }

    @Override
    public boolean isValidEnergyLimitForCreate(long energyLimit) {
        return this.parent.isValidEnergyLimitForCreate(energyLimit);
    }

    @Override
    public boolean destinationAddressIsSafeForThisVM(Address address) {
        return this.parent.destinationAddressIsSafeForThisVM(address);
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
        return new Substate(this);
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
}