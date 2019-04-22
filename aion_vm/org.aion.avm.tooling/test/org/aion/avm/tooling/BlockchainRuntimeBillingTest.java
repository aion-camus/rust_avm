package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.junit.*;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class BlockchainRuntimeBillingTest {

    private static boolean debugMode = false;
    private static int base = 2;
    private static int extraCost;

    @ClassRule
    public static AvmRule avmRule = new AvmRule(debugMode);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(BlockchainRuntimeBillingTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = deployResult.getDappAddress();
        extraCost = debugMode? 180 : 100;
    }

    @Test
    public void fillArray() {
        AvmRule.ResultWrapper result = callStatic("fillArray");
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        if (debugMode) {
            System.out.println("Energy Cost: " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));
        }
    }

    @Test
    public void blake2b() {
        int[] sizes = {100, 1000, 100000, 200000, 300000, 400000, 460000};
        int blakeBaseCost = 2000;

        for (int size : sizes) {
            AvmRule.ResultWrapper result = callStatic("blake2b", size);
            Assert.assertTrue(result.getReceiptStatus().isSuccess());
            int expectedEnergyConsumption = (int)(Math.ceil((double)size /10) * base * 6) + blakeBaseCost;
            Assert.assertEquals(expectedEnergyConsumption, (long) result.getDecodedReturnData() - extraCost);
            if (debugMode) {
                System.out.println("blake2b energy cost for size " + size + ": " + expectedEnergyConsumption);
            }
        }
    }

    @Test
    public void sha256() {
        int[] sizes = {100, 1000, 100000, 200000, 300000, 485000, 600000};
        int shaBaseCost = 1500;

        for (int size : sizes) {
            AvmRule.ResultWrapper result = callStatic("sha256", size);
            Assert.assertTrue(result.getReceiptStatus().isSuccess());
            int expectedEnergyConsumption = (int) Math.ceil((double)size /10) * base + shaBaseCost;

            Assert.assertEquals(expectedEnergyConsumption, (long) result.getDecodedReturnData() - extraCost);
            if (debugMode) {
                System.out.println("sha256 energy cost for size " + size + ": " + expectedEnergyConsumption);
            }
        }
    }

    @Test
    public void keccak() {
        int[] sizes = {100, 1000, 100000, 200000, 300000};
        int keccakBaseCost = 2000;

        for (int size : sizes) {
            AvmRule.ResultWrapper result = callStatic("keccak", size);
            Assert.assertTrue(result.getReceiptStatus().isSuccess());
            int expectedEnergyConsumption = (int) Math.ceil((double)size/10) * 12 * base + keccakBaseCost;

            Assert.assertEquals(expectedEnergyConsumption, (long) result.getDecodedReturnData() - extraCost);
            if (debugMode) {
                System.out.println("keccak energy cost for size " + size + ": " + expectedEnergyConsumption);
            }
        }
    }

    @Test
    public void hashingTestMaxInputZero() {
        byte[] message = new byte[32639];

        AvmRule.ResultWrapper result = callStatic("blake2bForInput", message);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        System.out.println("blake2b energy cost (32639 byte): " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));

        result = callStatic("sha256ForInput", message);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        System.out.println("sha256 energy cost (32639 byte): " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));

        result = callStatic("keccakForInput", message);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        System.out.println("keccak energy cost (32639 byte): " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));
    }

    @Test
    public void hashingTestMaxInput() {
        byte[] message = new byte[28000];
        Arrays.fill(message, Byte.MAX_VALUE);
        AvmRule.ResultWrapper result = callStatic("blake2bForInput", message);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        System.out.println("blake2b energy cost (28000 byte): " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));

        result = callStatic("sha256ForInput", message);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        System.out.println("sha256 energy cost (28000 byte): " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));

        message = new byte[27500];
        Arrays.fill(message, Byte.MAX_VALUE);
        result = callStatic("keccakForInput", message);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        System.out.println("keccak energy cost (27500 byte): " + (2_000_000 - result.getTransactionResult().getEnergyRemaining()));
    }

    private AvmRule.ResultWrapper callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        return avmRule.call(sender, contract, value, data, 2_000_000, 1);
    }
}
