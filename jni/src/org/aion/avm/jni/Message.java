package org.aion.avm.jni;

import java.math.BigInteger;

import org.aion.avm.core.BillingRules;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionInterface;

/// this class includes the Message transferred between kernel and avm
/// though avm calls it Transaction, it is not absolutely right, more like a message.
public class Message implements TransactionInterface {

    public Message(Type type, Address from, Address to, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice, byte[] transactionHash) {
        this.type = type;
        this.from = from.toBytes();
        this.to = to.toBytes();
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
        this.transactionHash = transactionHash;
    }

    public enum Type {
        /**
         * The CREATE is used to deploy a new DApp.
         */
        CREATE(3),
        /**
         * The CALL is used when sending an invocation to an existing DApp.
         */
        CALL(0),
        /**
         * The GARBAGE_COLLECT is a special transaction which asks that the target DApp's storage be deterministically collected.
         * Note that this is the only transaction type which will result in a negative TransactionResult.energyUsed.
         */
        GARBAGE_COLLECT(5);

        private int value;

        Type(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }
    }

    Type type;
    
    byte[] from;

    byte[] to;

    BigInteger nonce;

    BigInteger value;

    long timestamp;

    byte[] timestampAsBytes;

    byte[] data;

    long energyLimit;

    long energyPrice;

    byte[] transactionHash;

    byte vm;

    @Override
    public byte[] getTimestamp() {
        if (this.timestampAsBytes == null) {
            this.timestampAsBytes = BigInteger.valueOf(this.timestamp).toByteArray();
        }
        return this.timestampAsBytes;
    }

    long getTimestampAsLong() {
        return timestamp;
    }

    /**
     * Returns the {@link org.aion.vm.api.interfaces.VirtualMachine} that this transaction must be
     * executed by in the case of a contract creation.
     *
     * @return The VM to use to create a new contract.
     */
    @Override
    public byte getTargetVM() {
        return this.vm;
    }

    /**
     * Returns the type of transactional logic that this transaction will cause to be executed.
     */
    public Type getType() {
        return type;
    }

    @Override
    public Address getSenderAddress() {
        return org.aion.types.Address.wrap(from);
    }

    @Override
    public Address getDestinationAddress() {
        return org.aion.types.Address.wrap(to);
    }

    @Override
    public byte[] getNonce() {
        return this.nonce.toByteArray();
    }

    long getNonceAsLong() {
        return nonce.longValue();
    }

    @Override
    public byte[] getValue() {
        return this.value.toByteArray();
    }

    BigInteger getValueAsBigInteger() {
        return value;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long getEnergyLimit() {
        return energyLimit;
    }

    @Override
    public long getEnergyPrice() {
        return energyPrice;
    }

    @Override
    public byte[] getTransactionHash() {
        return transactionHash;
    }

    @Override
    public long getTransactionCost() {
        System.out.println("AVM getTransactionCost");
        return BillingRules.getBasicTransactionCost(getData());
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean isContractCreationTransaction() {
        return this.to == null;
    }
}