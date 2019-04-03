package org.aion.avm.jni;

import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionSideEffects;
import org.aion.vm.api.interfaces.TransactionInterface;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.types.Address;
import org.aion.kernel.Transaction;
import org.aion.kernel.SideEffects;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a transaction context for execution.
 */
public class TransactionContextHelper implements TransactionContext {

    private final Message avmMessage;
    private final byte type;
    private final byte[] address;
    private final byte[] caller;
    private final byte[] origin;
    private final long nonce;
    private final byte[] value;
    private final byte[] data;
    private final long energyLimit;
    private final long energyPrice;
    private byte[] transactionHash;
    private final int basicCost;
    private final long transactionTimestamp;
    private final long blockTimestamp;
    private final long blockNumber;
    private final long blockEnergyLimit;
    private final byte[] blockCoinbase;
    private final byte[] blockPreviousHash;
    private final byte[] blockDifficulty;
    private final int internalCallDepth;
    private final TransactionSideEffects sideEffects;


    public TransactionContextHelper(byte[] bytes) {
        NativeDecoder dec = new NativeDecoder(bytes);

        type = dec.decodeByte();
        address = dec.decodeBytes();
        caller = dec.decodeBytes();
        origin = dec.decodeBytes();
        nonce = dec.decodeLong();
        value = dec.decodeBytes();
        data = dec.decodeBytes();
        energyLimit = dec.decodeLong();
        energyPrice = dec.decodeLong();
        transactionHash = dec.decodeBytes();
        basicCost = dec.decodeInt();
        transactionTimestamp = dec.decodeLong();
        blockTimestamp = dec.decodeLong();
        blockNumber = dec.decodeLong();
        blockEnergyLimit = dec.decodeLong();
        blockCoinbase = dec.decodeBytes();
        blockPreviousHash = dec.decodeBytes();
        blockDifficulty = dec.decodeBytes();
        internalCallDepth = dec.decodeInt();
        sideEffects = new SideEffects();

        //TODO: transaction context will be finally removed.
        avmMessage = new Message(
            toAvmType(type),
            Address.wrap(caller),
            Address.wrap(address),
            BigInteger.valueOf(nonce),
            new BigInteger(1, value),
            data,
            energyLimit,
            energyPrice,
            transactionHash);
    }

    public byte[] getOrigin() {
        return origin;
    }

    @Override
    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public long getTransactionTimestamp() {
        return transactionTimestamp;
    }

    @Override
    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    @Override
    public long getBlockNumber() {
        return blockNumber;
    }

    @Override
    public long getBlockEnergyLimit() {
        return blockEnergyLimit;
    }

    public byte[] getBlockCoinbase() {
        return blockCoinbase;
    }

    @Override
    public long getBlockDifficulty() {
        return new BigInteger(1, blockDifficulty).longValue();
    }

    public int getInternalCallDepth() {
        return internalCallDepth;
    }

    @Override
    public String toString() {
        return "TransactionContextHelper{" +
                "type=" + type +
                ", address=" + Arrays.toString(address) +
                ", caller=" + Arrays.toString(caller) +
                ", origin=" + Arrays.toString(origin) +
                ", nonce=" + nonce +
                ", value=" + Arrays.toString(value) +
                ", data=" + Arrays.toString(data) +
                ", energyLimit=" + energyLimit +
                ", energyPrice=" + energyPrice +
                ", transactionHash=" + Arrays.toString(transactionHash) +
                ", basicCost=" + basicCost +
                ", transactionTimestamp=" + transactionTimestamp +
                ", blockTimestamp=" + blockTimestamp +
                ", blockNumber=" + blockNumber +
                ", blockEnergyLimit=" + blockEnergyLimit +
                ", blockCoinbase=" + Arrays.toString(blockCoinbase) +
                ", blockPreviousHash=" + Arrays.toString(blockPreviousHash) +
                ", blockDifficulty=" + Arrays.toString(blockDifficulty) +
                ", internalCallDepth=" + internalCallDepth +
                '}';
    }

    @Override
    public byte[] toBytes() {
        throw RuntimeAssertionError.unimplemented("No equivalent concept exists in the Avm for this.");
    }

    @Override
    public void setDestinationAddress(Address address) {
        //throw new AssertionError("No equivalent concept exists in the Avm for this.");
        avmMessage.to = address.toBytes();

    }

    @Override
    public void setTransactionHash(byte[] hash) {
        avmMessage.transactionHash = hash;
    }

    @Override
    public int getFlags() {
        throw RuntimeAssertionError.unimplemented("No equivalent concept exists in the Avm for this.");
    }

    @Override
    public int getTransactionStackDepth() {
        return internalCallDepth;
    }

    @Override
    public int getTransactionKind() {
        return type;
    }

    @Override
    public long getTransactionEnergyPrice() {
        return energyPrice;
    }

    @Override
    public long getTransactionEnergy() {
        throw RuntimeAssertionError.unimplemented("No equivalent concept exists in the Avm for this.");
    }

    @Override
    public byte[] getHashOfOriginTransaction() {
        throw RuntimeAssertionError.unimplemented("No equivalent concept exists in the Avm for this.");
    }

    @Override
    public byte[] getTransactionData() {
        return data;
    }

    @Override
    public BigInteger getTransferValue() {
        return new BigInteger(1, value);
    }

    @Override
    public Address getMinerAddress() {
        return new Address(blockCoinbase);
    }

    @Override
    public Address getOriginAddress() {
        return new Address(origin);
    }

    @Override
    public Address getSenderAddress() {
        return new Address(caller);
    }

    @Override
    public Address getDestinationAddress() {
        return new Address(address);
    }

    @Override
    public TransactionSideEffects getSideEffects() {
        return sideEffects;
    }

    @Override
    public TransactionInterface getTransaction() {
        System.out.println("Rust JNI: getTransaction");
        return avmMessage;
    }

    private Message.Type toAvmType(byte type) {
        Message.Type avmType;
        switch (type) {
            case 0x00:
                avmType = Message.Type.CREATE;
                break;
            case 0x05:
                avmType = Message.Type.GARBAGE_COLLECT;
                break;
            default:
                avmType = Message.Type.CALL;
        }

        return avmType;
    }
}
