package org.aion.avm.core;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * The test class loaded by HashCodeIntegrationTest.
 */
public class HashCodeIntegrationTestTarget {
    private static Object persistentObject;

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("persistNewObject")) {
                return ABIEncoder.encodeOneInteger(persistNewObject());
            } else if (methodName.equals("readPersistentHashCode")) {
                return ABIEncoder.encodeOneInteger(readPersistentHashCode());
            } else {
                return new byte[0];
            }
        }
    }
    
    public static int persistNewObject() {
        persistentObject = new Object();
        return persistentObject.hashCode();
    }
    
    public static int readPersistentHashCode() {
        return persistentObject.hashCode();
    }
}
