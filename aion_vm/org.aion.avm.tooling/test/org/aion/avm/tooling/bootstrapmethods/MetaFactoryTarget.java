package org.aion.avm.tooling.bootstrapmethods;

import java.lang.invoke.LambdaMetafactory;
import avm.Blockchain;

/**
 * A contract that attempts to call into {@link java.lang.invoke.LambdaMetafactory#metafactory}.
 * This should be illegal.
 */
public class MetaFactoryTarget {

    public static void call() {
        try {
            LambdaMetafactory.metafactory(null, null, null, null, null, null);
        } catch (Exception e) {
            Blockchain.println(e.toString());
        }
    }

}
