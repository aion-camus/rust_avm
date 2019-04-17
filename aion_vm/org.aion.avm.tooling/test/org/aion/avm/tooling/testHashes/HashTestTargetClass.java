package org.aion.avm.tooling.testHashes;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class HashTestTargetClass {

    private static byte[] hashedVal = null;
    private static final int LENGTH_LIMIT = 256;

    @Callable
    public static boolean callBlake2b(byte[] input){
        if (input.length > LENGTH_LIMIT){
            return false;
        } else {
            try{
                hashedVal = Blockchain.blake2b(input);
                return true;
            } catch (Exception e){
                hashedVal = null;
                return false;
            }
        }
    }

    @Callable
    public static boolean callSha(byte[] input){
        if (input.length > LENGTH_LIMIT){
            return false;
        } else {
            try{
                hashedVal = Blockchain.sha256(input);
                return true;
            } catch (Exception e){
                hashedVal = null;
                return false;
            }
        }
    }

    @Callable
    public static boolean callKeccak(byte[] input){
        if (input.length > LENGTH_LIMIT){
            return false;
        } else {
            try{
                hashedVal = Blockchain.keccak256(input);
                return true;
            } catch (Exception e){
                hashedVal = null;
                return false;
            }
        }
    }

    @Callable
    public static byte[] getHashedVal() {
        return hashedVal;
    }

    private static HashTestTargetClass hashTestTarget;

    /**
     * Initialization code executed once at the Dapp deployment.
     */
    static {
        hashTestTarget = new HashTestTargetClass();
    }
}
