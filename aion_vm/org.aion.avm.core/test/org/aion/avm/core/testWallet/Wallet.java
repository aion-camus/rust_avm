package org.aion.avm.core.testWallet;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * In the original, Wallet "inherited" from multisig, multiowned, and daylimit but the Solidity concept of "inheritance" probably makes more
 * sense as strict composition so we will just depend on someone creating this object with a pre-configured instances and implementing the
 * interfaces.
 */
public class Wallet {
    // Note that this key is really just a subset of uses of "Operation".
    private static AionMap<BytesKey, Transaction> transactions;

    public static void init(){

    }

    // The contract "Constructor".  Note that this should only be called once, when initially deployed (in Ethereum world, the arguments are
    // just pass as part of the deployment payload, after the code).
    public static void init(Address[] requestedOwners, int votesRequiredPerOperation, long daylimit) {
        // This is the contract entry-point so "construct" the contract fragments from which we are derived.
        Address sender = Blockchain.getCaller();
        long nowInSeconds = Blockchain.getBlockTimestamp();
        long nowInDays = nowInSeconds / (24 * 60 * 60);
        Multiowned.init(sender, requestedOwners, votesRequiredPerOperation);
        Daylimit.init(daylimit, nowInDays);
        
        Wallet.transactions = new AionMap<>();
    }

    /**
     * Exists because the ABIEncoder can't encode Address[] but can encode Address.
     */
    public static void initWrapper(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        init(new Address[] {extra1, extra2}, requiredVotes, dailyLimit);
    }

    /**
     * The generic start symbol which processes the input using the ABI to calls out to other helpers.
     *
     * @return The output of running the invoke (empty byte array for void methods).
     */
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("revoke")) {
                revoke(decoder.decodeOneByteArray());
                return new byte[0];
            } else if (methodName.equals("initWrapper")) {
                initWrapper(decoder.decodeOneAddress(), decoder.decodeOneAddress(), decoder.decodeOneInteger(), decoder.decodeOneLong());
                return new byte[0];
            } else if (methodName.equals("payable")) {
                payable(decoder.decodeOneAddress(), (decoder.decodeOneLong()));
                return new byte[0];
            } else if (methodName.equals("addOwner")) {
                return ABIEncoder.encodeOneBoolean(addOwner(decoder.decodeOneAddress()));
            } else if (methodName.equals("execute")) {
                return ABIEncoder.encodeOneByteArray(execute(decoder.decodeOneAddress(), decoder.decodeOneLong(), decoder.decodeOneByteArray()));
            } else if (methodName.equals("confirm")) {
                return ABIEncoder.encodeOneBoolean(confirm(decoder.decodeOneByteArray()));
            } else if (methodName.equals("changeRequirement")) {
                return ABIEncoder.encodeOneBoolean(changeRequirement(decoder.decodeOneInteger()));
            } else if (methodName.equals("getOwner")) {
                return ABIEncoder.encodeOneAddress(getOwner(decoder.decodeOneInteger()));
            } else if (methodName.equals("changeOwner")) {
                return ABIEncoder.encodeOneBoolean(changeOwner(decoder.decodeOneAddress(), decoder.decodeOneAddress()));
            } else if (methodName.equals("removeOwner")) {
                return ABIEncoder.encodeOneBoolean(removeOwner(decoder.decodeOneAddress()));
            } else {
                return new byte[0];
            }
        }
    }

    // EXTERNAL - composed
    public static void revoke(byte[] transactionBytes) {
        Multiowned.revoke(transactionBytes);
    }

    // EXTERNAL - composed
    public static boolean addOwner(Address owner) {
        boolean result = false;
        try {
            Multiowned.addOwner(owner);
            result = true;
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    // EXTERNAL - composed
    public static boolean removeOwner(Address owner) {
        boolean result = false;
        try {
            Multiowned.removeOwner(owner);
            result = true;
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    // EXTERNAL - composed
    public static boolean changeRequirement(int newRequired) {
        boolean result = false;
        try {
            Multiowned.changeRequirement(newRequired);
            result = true;
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    // EXTERNAL - composed
    public static Address getOwner(int ownerIndex) {
        return Multiowned.getOwner(ownerIndex);
    }

    // EXTERNAL - composed
    public static void setDailyLimit(long value) {
        Daylimit.setDailyLimit(value);
    }

    // EXTERNAL - composed
    public static void resetSpentToday() {
        Daylimit.resetSpentToday();
    }

    // EXTERNAL
    public static void kill(Address to) {
        // (modifier)
        Multiowned.onlyManyOwners(Blockchain.getCaller(), Operation.fromMessage());

        Blockchain.selfDestruct(to);
    }

    // gets called when no other function matches
    public static void payable(Address from, long value) {
        if (value > 0) {
            EventLogger.deposit(from, value);
        }
    }

    // EXTERNAL
    // Outside-visible transact entry point. Executes transaction immediately if below daily spend limit.
    // If not, goes into multisig process. We provide a hash on return to allow the sender to provide
    // shortcuts for the other confirmations (allowing them to avoid replicating the _to, _value
    // and _data arguments). They still get the option of using them if they want, anyways.
    public static byte[] execute(Address to, long value, byte[] data) {
        // (modifier)
        Multiowned.onlyOwner(Blockchain.getCaller());
        
        byte[] result = null;
        // first, take the opportunity to check that we're under the daily limit.
        if (Daylimit.underLimit(value)) {
            EventLogger.singleTransact(Blockchain.getCaller(), value, to, data);
            // yes - just execute the call.
            byte[] response = Blockchain.call(to, BigInteger.ZERO, data, value).getReturnData();
            if (null == response) {
                throw new RequireFailedException();
            }
            // Returns nothing special.
            result = null;
        } else {
            // determine our operation hash.
            result = Operation.rawOperationForCurrentMessageAndBlock();
            BytesKey transactionKey = BytesKey.from(result);
            if (!internalConfirm(result) && (null == Wallet.transactions.get(transactionKey))) {
                Transaction transaction = new Transaction();
                transaction.to = to;
                transaction.value = value;
                transaction.data = data;
                Wallet.transactions.put(transactionKey, transaction);
                EventLogger.confirmationNeeded(Operation.fromHashedBytes(result), Blockchain.getCaller(), value, to, data);
            }
        }
        return result;
    }

    public static boolean confirm(byte[] h) {
        return internalConfirm(h);
    }

    public static boolean changeOwner(Address from, Address to) {
        boolean result = false;
        try {
            Multiowned.changeOwner(from, to);
            result = true;
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    // confirm a transaction through just the hash. we use the previous transactions map, m_txs, in order
    // to determine the body of the transaction from the hash provided.
    private static boolean internalConfirm(byte[] h) {
        boolean result = false;
        try {
            // (modifier)
            Operation operationToConfirm = Operation.fromRawBytes(h);
            Multiowned.onlyManyOwners(Blockchain.getCaller(), operationToConfirm);
            
            BytesKey key = BytesKey.from(h);
            if (null != Wallet.transactions.get(key).to) {
                Transaction transaction = Wallet.transactions.get(key);
                byte[] response = Blockchain.call(transaction.to, BigInteger.ZERO, transaction.data, transaction.value).getReturnData();
                if (null == response) {
                    throw new RequireFailedException();
                }
                EventLogger.multiTransact(Blockchain.getCaller(), operationToConfirm, transaction.value, transaction.to, transaction.data);
                Wallet.transactions.remove(BytesKey.from(h));
                result = true;
            }
        } catch (RequireFailedException e) {
            // In this case, the shape of the original contract seemed to act like false should be returned.
            result = false;
        }
        return result;
    }


    // Transaction structure to remember details of transaction lest it need be saved for a later call.
    // (this is public just for easy referencing in the Deployer's loader logic).
    private static class Transaction {
        public Address to;
        public long value;
        public byte[] data;
    }
}
