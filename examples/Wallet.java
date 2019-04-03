package examples;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

/// Java implementation of Multi-Sig wallet
public class Wallet {
    public static byte[] main() {
        // The ABI is technically optional.  Any interpretation of the incoming data is permitted but the ABI is what we use, internally.
        return ABIDecoder.decodeAndRunWithClass(Wallet.class, BlockchainRuntime.getData());
    }

    // TODO:
}