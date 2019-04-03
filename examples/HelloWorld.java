package examples;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld {
    public static byte[] main() {
        // The ABI is technically optional.  Any interpretation of the incoming data is permitted but the ABI is what we use, internally.
        return ABIDecoder.decodeAndRunWithClass(HelloWorld.class, BlockchainRuntime.getData());
    }

    public static void sayHello() {
        BlockchainRuntime.println("Hello World!");
    }
}
