package org.aion.avm.tooling;

import avm.Address;
import avm.Blockchain;


/**
 * The test class loaded by SubclassPersistenceIntegrationTest.
 * This defines an operation which should be normally fine but is illegal when loaded as a DApp since it sub-classes an API type type.
 */
public class SubclassPersistenceIntegrationTestFailApi {
    private static SubAddress address;

    public static int setup_api() {
        // We just need some kind of random number.
        address = new SubAddress((int)Blockchain.getBlockTimestamp());
        return address.number;
    }
    public static int check_api() {
        return address.number;
    }


    private static class SubAddress extends Address {
        public final int number;
        public SubAddress(int number) {
            super(new byte[0]);
            this.number = number;
        }
    }
}
