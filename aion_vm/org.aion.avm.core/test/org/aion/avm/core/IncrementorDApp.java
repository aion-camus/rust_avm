package org.aion.avm.core;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test DApp used in tests related to CREATE and CALL since it is small and has a very basic interface and functionality.
 * Takes 1 byte as an argument to CREATE, which will be the incrementor byte.
 * On any CALL, increments the given byte[] elements by the incrementor and returns it.
 */
public class IncrementorDApp {
    private static final byte INCREMENT_BY;
    static {
        INCREMENT_BY = BlockchainRuntime.getData()[0];
    }

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("incrementArray")) {
                return ABIEncoder.encodeOneObject(incrementArray((byte[]) argValues[0]));
            } else {
                return new byte[0];
            }
        }
    }

    public static byte[] incrementArray(byte[] array) {
        for (int i = 0; i < array.length; ++i) {
            array[i] += INCREMENT_BY;
        }
        return array;
    }
}
