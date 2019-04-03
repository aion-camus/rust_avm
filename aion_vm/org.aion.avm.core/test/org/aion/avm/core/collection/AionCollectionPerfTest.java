package org.aion.avm.core.collection;

import java.math.BigInteger;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionPlainMap;
import org.aion.avm.userlib.AionPlainSet;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class AionCollectionPerfTest {

    private org.aion.types.Address from = TestingKernel.PREMINED_ADDRESS;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 100_000_000_000L;
    private long energyPrice = 1;

    private byte[] buildListPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionListPerfContract.class);
    }

    private byte[] buildSetPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionSetPerfContract.class,
            AionPlainSet.class,
            AionPlainMap.class);
    }

    private byte[] buildMapPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionMapPerfContract.class,
            AionPlainSet.class,
            AionPlainMap.class);
    }

    private TransactionResult deploy(KernelInterface kernel, AvmImpl avm, byte[] testJar){


        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = TransactionContextImpl.forExternalTransaction(createTransaction, block);
        TransactionResult createResult = avm.run(kernel, new TransactionContext[] {createContext})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());

        return createResult;
    }


    private TransactionResult call(KernelInterface kernel, AvmImpl avm, org.aion.types.Address contract, org.aion.types.Address sender, byte[] args) {
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender), BigInteger.ZERO, args, energyLimit, 1l);
        TransactionContext callContext = TransactionContextImpl.forExternalTransaction(callTransaction, block);
        TransactionResult callResult = avm.run(kernel, new TransactionContext[] {callContext})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testList() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionList");
        byte[] args;
        KernelInterface kernel = new TestingKernel();
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AvmTransactionResult deployRes = (AvmTransactionResult) deploy(kernel, avm, buildListPerfJar());
        org.aion.types.Address contract = org.aion.types.Address.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callInit");
        AvmTransactionResult initResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callAppend");
        AvmTransactionResult appendResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Append        : " + appendResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(kernel, avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callInsertHead");
        AvmTransactionResult insertHeadResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Insert Head   : " + insertHeadResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callInsertMiddle");
        AvmTransactionResult insertMiddleResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Insert Middle : " + insertMiddleResult.getEnergyUsed() / AionListPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        avm.shutdown();
    }

    @Test
    public void testSet() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionSet");
        byte[] args;
        KernelInterface kernel = new TestingKernel();
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AvmTransactionResult deployRes = (AvmTransactionResult) deploy(kernel, avm, buildSetPerfJar());
        org.aion.types.Address contract = org.aion.types.Address.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callInit");
        AvmTransactionResult initResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callAdd");
        AvmTransactionResult addResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callContains");
        AvmTransactionResult containsResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemove");
        AvmTransactionResult removeReult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.getEnergyUsed() / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionPlainSet");

        args = ABIEncoder.encodeMethodArguments("callInitB");
        initResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callAddB");
        addResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callContainsB");
        containsResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemoveB");
        removeReult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.getEnergyUsed() / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }

    @Test
    public void testMap() {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionMap");
        byte[] args;
        KernelInterface kernel = new TestingKernel();
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AvmTransactionResult deployRes = (AvmTransactionResult) deploy(kernel, avm, buildMapPerfJar());
        org.aion.types.Address contract = org.aion.types.Address.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callInit");
        AvmTransactionResult initResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPut");
        AvmTransactionResult putResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGet");
        AvmTransactionResult getResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemove");
        AvmTransactionResult removeResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionPlainMap");

        args = ABIEncoder.encodeMethodArguments("callInitB");
        initResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPutB");
        putResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGetB");
        getResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemoveB");
        removeResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }

}
