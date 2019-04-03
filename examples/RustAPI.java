package rust_api;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Address;
import org.aion.avm.api.Result;

public class RustAPI {
    /**
     * This static main() MUST be provided in the main class of a DApp.  This is where every non-deploy transaction begins.
     *
     * @return The data to return (only relevant if this called by another DApp - this will be returned to the caller).
     */
    public static byte[] main() {
        // The ABI is technically optional.  Any interpretation of the incoming data is permitted but the ABI is what we use, internally.
        return ABIDecoder.decodeAndRunWithClass(RustAPI.class, BlockchainRuntime.getData());
    }

    public static Address getAddress() {
        return BlockchainRuntime.getAddress();
    }

    public static Address getCaller() {
        return BlockchainRuntime.getCaller();
    }

    public static Address getOrigin() {
        return BlockchainRuntime.getOrigin();
    }

    public static long getEnergyLimit() {
        return BlockchainRuntime.getEnergyLimit();
    }

    public static long getEnergyPrice() {
        return BlockchainRuntime.getEnergyPrice();
    }

    public static long getValue() {
        return BlockchainRuntime.getValue().longValue();
    }

    public static byte[] getData() {
        return BlockchainRuntime.getData();
    }

    public static long getBlockTimestamp() {
        return BlockchainRuntime.getBlockTimestamp();
    }

    public static long getBlockNumber() {
        return BlockchainRuntime.getBlockNumber();
    }

    public static long getBlockEnergyLimit() {
        return BlockchainRuntime.getBlockEnergyLimit();
    }

    public static Address getBlockCoinbase() {
        return BlockchainRuntime.getBlockCoinbase();
    }

    public java.math.BigInteger getBlockDifficulty() {
        return BlockchainRuntime.getBlockDifficulty();
    }

    public static long getBalance(Address address) {
        return BlockchainRuntime.getBalance(address).longValue();
    }

    public static long getBalanceOfThisContract() {
        return BlockchainRuntime.getBalanceOfThisContract().longValue();
    }

    public static int getCodeSize(Address address) {
        return BlockchainRuntime.getCodeSize(address);
    }

    public static long getRemainingEnergy() {
        return BlockchainRuntime.getRemainingEnergy();
    }

    // TODO: need another AVM contract
    public static Result call(Address targetAddress, java.math.BigInteger value, byte[] data, long energylimit) {
        byte[] returnData = {0, 1, 2, 3};
        return new Result(true, returnData);
    }

    public static Result create(java.math.BigInteger value, byte[] data, long energyLimit) {
        byte[] returnData = {0, 1, 2, 3};
        return new Result(true, returnData);
    }

    public static void selfDestruct(Address beneficiary) {
        BlockchainRuntime.selfDestruct(beneficiary);
    }

    public static void log1() {
        byte[] data = new byte[]{0x00, 0x01, 0x02};
        BlockchainRuntime.log(data);
    }

    public static void log2() {
        byte[] topic1 = new byte[]{0x00, 0x01, 0x02};
        byte[] topic2 = new byte[]{0x05, 0x06, 0x07};
        byte[] data = new byte[]{0x0a, 0x0a, 0x0a};
        BlockchainRuntime.log(topic1, topic2, data);
    }

    public static void log3() {
        byte[] topic1 = new byte[]{0x00, 0x01, 0x02};
        byte[] topic2 = new byte[]{0x05, 0x06, 0x07};
        byte[] topic3 = new byte[]{0x0a, 0x0a};
        byte[] data = new byte[]{0x0b, 0x0b, 0x0b};
        BlockchainRuntime.log(topic1, topic2, topic3, data);
    }

    public static void log4() {
        byte[] topic1 = new byte[]{0x00, 0x01, 0x02};
        byte[] topic2 = new byte[]{0x05, 0x06, 0x07};
        byte[] topic3 = new byte[]{0x0a, 0x0a};
        byte[] topic4 = new byte[]{0x0b, 0x0b};
        byte[] data = new byte[]{0x0c, 0x0c, 0x0c};
        BlockchainRuntime.log(topic1, topic2, topic3, topic4, data);
    }

    public static byte[] blake2b() {
        byte[] data = "hello, blake2b".getBytes();
        return BlockchainRuntime.blake2b(data);
    }

    public static byte[] sha256() {
        byte[] data = "hello, sha256".getBytes();
        return BlockchainRuntime.sha256(data);
    }

    public static byte[] keccak256() {
        byte[] data = "hello, keccak256".getBytes();
        return BlockchainRuntime.keccak256(data);
    }

    public static void revert() {
        BlockchainRuntime.revert();
    }

    public static void invalid() {
        BlockchainRuntime.invalid();
    }

    public static void require(boolean condition) {
        BlockchainRuntime.require(condition);
    }

    public static void print(java.lang.String message) {
        BlockchainRuntime.print(message);
    }

    public static void println(java.lang.String message) {
        BlockchainRuntime.println(message);
    }

    public static boolean edVerify(byte[] data, byte[] signature, byte[] publicKey) {
        return BlockchainRuntime.edVerify(data, signature, publicKey);
    }
}
