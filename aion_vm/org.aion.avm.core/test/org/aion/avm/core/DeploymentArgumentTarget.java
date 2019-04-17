package org.aion.avm.core;

import java.math.BigInteger;

import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


public class DeploymentArgumentTarget {
    private static String arg0;
    private static Address[] arg1;
    private static int arg2;
    private static double arg3;
    private static byte[] smallJar;

    static {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        arg0 = decoder.decodeOneString();
        arg1 = decoder.decodeOneAddressArray();
        arg2 = decoder.decodeOneInteger();
        arg3 = decoder.decodeOneDouble();
        smallJar = decoder.decodeOneByteArray();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("correctDeployment")) {
                correctDeployment();
                return new byte[0];
            } else if (methodName.equals("incorrectDeployment")) {
                incorrectDeployment();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void correctDeployment() {

        byte[] arg0Bytes = ABIEncoder.encodeOneString(arg0);
        byte[] arg1Bytes = ABIEncoder.encodeOneAddressArray(arg1);
        byte[] arg2Bytes = ABIEncoder.encodeOneInteger(arg2);
        byte[] arg3Bytes = ABIEncoder.encodeOneDouble(arg3);
        byte[] smallJarBytes = ABIEncoder.encodeOneByteArray(smallJar);
        byte[] deploymentArgs = concatenateArrays(arg0Bytes, arg1Bytes, arg2Bytes, arg3Bytes, smallJarBytes);

        byte[] codeAndArguments = encodeCodeAndArguments(deploymentArgs);
        Result createResult = Blockchain.create(BigInteger.ZERO, codeAndArguments, Blockchain.getEnergyLimit());
        Blockchain.require(createResult.isSuccess());
    }

    public static void incorrectDeployment() {
        // For this failed attempt, we will omit the final argument, which should cause a deployment failure.

        byte[] arg0Bytes = ABIEncoder.encodeOneString(arg0);
        byte[] arg1Bytes = ABIEncoder.encodeOneAddressArray(arg1);
        byte[] arg2Bytes = ABIEncoder.encodeOneInteger(arg2);
        byte[] arg3Bytes = ABIEncoder.encodeOneDouble(arg3);
        byte[] deploymentArgs = concatenateArrays(arg0Bytes, arg1Bytes, arg2Bytes, arg3Bytes);

        byte[] codeAndArguments = encodeCodeAndArguments(deploymentArgs);
        Result createResult = Blockchain.create(BigInteger.ZERO, codeAndArguments, Blockchain.getEnergyLimit());
        // We still want to pass (to ensure this isn't a different failure) so require that the sub-deployment failed.
        Blockchain.require(!createResult.isSuccess());
    }


    // Note that we currently don't have a way to encode the CodeAndArguments from inside a contract.
    // TODO:  This capability should be added to the new user-space ABI.
    private static byte[] encodeCodeAndArguments(byte[] deploymentArgs) {
        byte[] codeAndArguments = new byte[Integer.BYTES + smallJar.length + Integer.BYTES + deploymentArgs.length];
        AionBuffer codeAndArgumentsBuffer = AionBuffer.wrap(codeAndArguments);
        codeAndArgumentsBuffer.putInt(smallJar.length);
        codeAndArgumentsBuffer.put(smallJar);
        codeAndArgumentsBuffer.putInt(deploymentArgs.length);
        codeAndArgumentsBuffer.put(deploymentArgs);
        return codeAndArguments;
    }

    private static byte[] concatenateArrays(byte[]... arrays) {
        int length = 0;
        for(byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int writtenSoFar = 0;
        for(byte[] array : arrays) {
            System.arraycopy(array, 0, result, writtenSoFar, array.length);
            writtenSoFar += array.length;
        }
        return result;
    }
}
