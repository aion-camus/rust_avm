package org.aion.avm.core;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test DApp used in tests related to CREATE and CALL tests.
 * CREATE accepts a byte[] which is the code+args to pass to another create.
 * On CALL, creates a new DApp with its CREATE args and then invokes CALL on that, using the argument it was given.
 */
public class SpawnerDApp {
    private static final byte[] CODE_AND_ARGS;
    static {
        CODE_AND_ARGS = Blockchain.getData();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain  .getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("spawnAndCall")) {
                return ABIEncoder.encodeOneByteArray(spawnAndCall(decoder.decodeOneByteArray()));
            } else if (methodName.equals("spawnOnly")) {
                return ABIEncoder.encodeOneAddress(spawnOnly(decoder.decodeOneBoolean()));
            } else {
                return new byte[0];
            }
        }
    }

    public static byte[] spawnAndCall(byte[] array) {
        byte[] contractAddress = Blockchain.create(BigInteger.ZERO, CODE_AND_ARGS, 10_000_000L).getReturnData();
        return Blockchain.call(new Address(contractAddress), BigInteger.ZERO, array, 10_000_000L).getReturnData();
    }

    public static Address spawnOnly(boolean shouldFail) {
        byte[] contractAddress = Blockchain.create(BigInteger.ZERO, CODE_AND_ARGS, 10_000_000L).getReturnData();
        if (shouldFail) {
            Blockchain.invalid();
        }
        return new Address(contractAddress);
    }
}
