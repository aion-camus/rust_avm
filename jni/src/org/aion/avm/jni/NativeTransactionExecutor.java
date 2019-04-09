package org.aion.avm.jni;

import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.tooling.StandardCapabilities;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TransactionalKernel;


import java.util.Set;

public class NativeTransactionExecutor {

    /**
     * Runs the given transactions with the specified handle, and returns the transaction results
     *
     * @param handle reserved pointer for the client
     * @param txs    serialized list of transaction contexts, using the Native Codec
     * @return serialized list of transaction result, using the Native Codec
     */
    public static byte[] execute(long handle, byte[] txs) {
        try {
            // deserialize the transaction contexts
            NativeDecoder decoder = new NativeDecoder(txs);
            TransactionContext[] contexts = new TransactionContext[decoder.decodeInt()];
            for (int i = 0; i < contexts.length; i++) {
                contexts[i] = new TransactionContextHelper(decoder.decodeBytes());
                if (Constants.DEBUG)
                    System.out.println(contexts[i]);
            }

            // submit the transactions to a newly created avm for execution
            NativeKernelInterface kernel = new NativeKernelInterface(handle);
            Substate substate = new Substate(kernel);
            AvmConfiguration config = new AvmConfiguration();
            config.enableVerboseConcurrentExecutor = true;
            AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new AionCapabilities(), config);
            SimpleFuture<TransactionResult>[] futures = avm.run(substate, contexts);

            // wait for the transaction results and serialize them into bytes
            NativeEncoder encoder = new NativeEncoder();
            encoder.encodeInt(futures.length);
            for (int i = 0; i < futures.length; i++) {
                TransactionResult r = futures[i].get();
                encoder.encodeBytes(TransactionResultHelper.encodeTransactionResult(r));
                if (Constants.DEBUG) {
                    System.out.println(futures[i]);
                }
                //TODO: get VM kernel interface generated during execution; then update substates
                KernelInterface transactionKernel = r.getKernelInterface();
                for (byte[] addr: transactionKernel.getTouchedAccounts()) {
                    kernel.touchAccount(addr, i);
                }

                transactionKernel.commitTo(kernel);
                byte[] state_root = kernel.sendSignal(0);   // 0: should commit state; and return state root
                encoder.encodeBytes(state_root);
            }
            kernel.sendSignal(-1);
            // substate.commit();
            avm.shutdown();

            return encoder.toByteArray();
        } catch (Exception e) {
            // instead of propagating the exceptions to client, we dump it from the java side
            // and return NULL to indicate an error.
            e.printStackTrace();
            return null;
        }
    }
}
