package org.aion.parallel;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;

public class TestContract {

    static Address deployer;

    static {
        deployer = new Address(BlockchainRuntime.getCaller().unwrap());
    }

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("doTransfer")) {
                doTransfer();
                return new byte[0];
            } else if (methodName.equals("addValue")) {
                addValue();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void doTransfer() {
        BlockchainRuntime.call(deployer, BigInteger.valueOf(1000), new byte[0], 100_000L);
    }

    public static void addValue() {
    }

}
