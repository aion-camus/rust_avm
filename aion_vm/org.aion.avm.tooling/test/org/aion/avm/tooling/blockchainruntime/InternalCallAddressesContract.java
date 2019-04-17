package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import avm.Address;
import avm.Blockchain;
import avm.Result;

/**
 * A contract that tracks the following addresses in internal calls: origin, caller, contract
 */
public class InternalCallAddressesContract {

    /**
     * Triggers a chain of internal transactions such that this contract will call into contract
     * otherContracts[0], who will call into otherContracts[1], and so on until the final contract
     * is called.
     *
     * Each of the called contracts will report their address, caller and origin.
     *
     * The returned array for N calls deep will look like this (assume external transaction has
     * depth zero):
     *
     *   index 0: origin for contract at depth 0
     *   index 1: caller for contract at depth 0
     *   index 2: address for contract at depth 0
     *   ...
     *   index (N * 3): origin for contract at depth N
     *   index (N * 3) + 1: caller for contract at depth N
     *   index (N * 3) + 2: address for contract at depth N
     *
     *
     *   ASSUMPTION: all of the other contracts are instance of InternalCallAddressTrackerContract!
     */
    @Callable
    public static Address[] runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(Address[] otherContracts) {
        return recurseAndTrackAddressesByRecursingFirst(otherContracts, 0);
    }

    /**
     * Triggers a chain of internal transactions such that this contract will call into contract
     * otherContracts[0], who will call into otherContracts[1], and so on until the final contract
     * is called.
     *
     * Each of the called contracts will report their address, caller and origin.
     *
     * The returned array for N calls deep will look like this (assume external transaction has
     * depth zero):
     *
     *   index 0: origin for contract at depth 0
     *   index 1: caller for contract at depth 0
     *   index 2: address for contract at depth 0
     *   ...
     *   index (N * 3): origin for contract at depth N
     *   index (N * 3) + 1: caller for contract at depth N
     *   index (N * 3) + 2: address for contract at depth N
     *
     *
     *   ASSUMPTION: all of the other contracts are instance of InternalCallAddressTrackerContract!
     */
    @Callable
    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(Address[] otherContracts) {
        return recurseAndTrackAddressesByRecursingLast(otherContracts, 0);
    }

    public static Address[] recurseAndTrackAddressesByRecursingLast(Address[] otherContracts, int currentDepth) {
        return recurseAndTrackAddresses(otherContracts, currentDepth, false);
    }

    public static Address[] recurseAndTrackAddressesByRecursingFirst(Address[] otherContracts, int currentDepth) {
        return recurseAndTrackAddresses(otherContracts, currentDepth, true);
    }

    @Callable
    public static Address[] recurseAndTrackAddresses(Address[] otherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < otherContracts.length) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            byte[] methodNameBytes = ABIEncoder.encodeOneString("recurseAndTrackAddresses");
            byte[] argBytes1 = ABIEncoder.encodeOneAddressArray(otherContracts);
            byte[] argBytes2 = ABIEncoder.encodeOneInteger(currentDepth + 1);
            byte[] argBytes3 = ABIEncoder.encodeOneBoolean(recurseFirst);
            byte[] data = concatenateArrays(methodNameBytes, argBytes1, argBytes2, argBytes3);


            Result result = Blockchain.call(otherContracts[currentDepth], BigInteger.ZERO, data, Blockchain.getRemainingEnergy());

            // This way we actually know if something went wrong...
            if (!result.isSuccess()) {
                Blockchain.revert();
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            ABIDecoder decoder = new ABIDecoder(result.getReturnData());
            Address[] reportForOtherContracts = decoder.decodeOneAddressArray();
            return joinArrays(reportForThisContract, reportForOtherContracts);
        } else {
            return getAddresses();
        }
    }

    /**
     * Returns an array of length 3 of the following addresses in this order:
     *
     *   [ origin, caller, address ]
     */
    private static Address[] getAddresses() {
        Address[] addresses = new Address[3];
        addresses[0] = Blockchain.getOrigin();
        addresses[1] = Blockchain.getCaller();
        addresses[2] = Blockchain.getAddress();
        return addresses;
    }

    /**
     * Returns a concatenation of array1 and array2 in this order.
     */
    private static Address[] joinArrays(Address[] array1, Address[] array2) {
        Address[] array = new Address[array1.length + array2.length];

        for (int i = 0; i < array1.length; i++) {
            array[i] = array1[i];
        }

        for (int i = 0; i < array2.length; i++) {
            array[array1.length + i] = array2[i];
        }

        return array;
    }

    private static byte[] concatenateArrays(byte[]... arrays) {
        int length = 0;
        for(byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int writtenSoFar = 0;
        for(byte[] array : arrays) {
            System.arraycopy(array, 0, result, writtenSoFar, array.length);
            writtenSoFar += array.length;
        }
        return result;
    }
}
