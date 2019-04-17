package org.aion.avm.tooling.shadowing.testMath;

import java.math.MathContext;
import java.math.RoundingMode;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class TestResource {

    @Callable
    public static boolean testMaxMin(){
        boolean ret = true;

        ret = ret && (StrictMath.max(1, 10) == 10);
        ret = ret && (StrictMath.min(1, 10) == 1);

        return ret;
    }

    @Callable
    public static int testMathContext() {
        return new MathContext(5).getPrecision();
    }

    @Callable
    public static void getRoundingMode(){
        MathContext mc1, mc2;

        mc1 = new MathContext(4);
        mc2 = new MathContext(5, RoundingMode.CEILING);

        Blockchain.require(mc1.getRoundingMode().equals(RoundingMode.HALF_UP));
        Blockchain.require(mc2.getRoundingMode().equals(RoundingMode.CEILING));
    }
}
