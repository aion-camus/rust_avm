package org.aion.avm.tooling;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;

public class CryptoUtilMethodFeeBenchmarkTestTargetClass {

    private static final byte[] SIGNATURE = "0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61".getBytes();
    private static final byte[] PK = "8c11e9a4772bb651660a5a5e412be38d".getBytes();

    private CryptoUtilMethodFeeBenchmarkTestTargetClass(){
        // initialize to default
    }

    public static void callBlake2b(int count, byte[] message){
        for (int i = 0; i < count; i++){
             BlockchainRuntime.blake2b(message);
        }
    }

    public static void callSha(int count, byte[] message){
        for (int i = 0; i < count; i++){
            BlockchainRuntime.sha256(message);
        }
    }

    public static void callKeccak(int count, byte[] message){
        for (int i = 0; i < count; i++){
            BlockchainRuntime.keccak256(message);
        }
    }

    public static void callEdverify(int count, byte[] message) throws IllegalArgumentException{
        for (int i = 0; i < count; i++){
            BlockchainRuntime.edVerify(message, SIGNATURE, PK);
        }
    }

    /**
     * Entry point at a transaction call.
     */
    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("callBlake2b")) {
                callBlake2b((Integer) argValues[0], (byte[]) argValues[1]);
                return new byte[0];
            } else if (methodName.equals("callSha")) {
                callSha((Integer) argValues[0], (byte[]) argValues[1]);
                return new byte[0];
            } else if (methodName.equals("callKeccak")) {
                callKeccak((Integer) argValues[0], (byte[]) argValues[1]);
                return new byte[0];
            } else if (methodName.equals("callEdverify")) {
                callEdverify((Integer) argValues[0], (byte[]) argValues[1]);
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }
}
