package org.aion.avm.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.types.ImmortalDappModule;
import org.aion.avm.core.types.TransformedDappModule;
import org.aion.avm.core.util.Helpers;


/**
 * This is just a utility class which contains the logic required to assemble a LoadedDApp instance from the code in storage
 * or construct a temporary LoadedDApp instance from transformed classes, in-memory.
 * This logic was formally in DAppExecutor/DAppCreator but moving it out made handling the cached DApp case less specialized.
 */
public class DAppLoader {
    /**
     * Called to load an immortal DApp from the code storage provided by the kernel.
     * 
     * @param immortalDappJar The contract jar.
     * @return The DApp instance, or NULL if not exist
     * @throws IOException If there was a failure decoding the code from the kernel.
     */
    public static LoadedDApp loadFromGraph(byte[] immortalDappJar, boolean preserveDebuggability) throws IOException {
        // normal account or account with no code?
        if (immortalDappJar == null || immortalDappJar.length == 0) {
            return null;
        }

        // parse the code
        ImmortalDappModule app = ImmortalDappModule.readFromJar(immortalDappJar);
        
        // We now need all the classes which will loaded within the class loader for this DApp (includes Helper and userlib classes we add).
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes, Helpers.loadDefaultHelperBytecode());
        
        // Construct the per-contract class loader.
        AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);
        
        // Load all the user-defined classes (these are required for both loading and storing state).
        // (we do this in alphabetical order since the persistence model needs consistent read/write order).
        List<Class<?>> aphabeticalContractClasses = Helpers.getAlphabeticalUserTransformedDappClasses(classLoader, app.classes.keySet());

        // We now have all the information to describe the LoadedDApp.
        return new LoadedDApp(classLoader, aphabeticalContractClasses, app.mainClass, preserveDebuggability);
    }

    /**
     * Called to create a temporary DApp from transformed classes, in-memory.
     * 
     * @param app The transformed module.
     * @return The DApp instance.
     */
    public static LoadedDApp fromTransformed(TransformedDappModule app, boolean preserveDebuggability) {
        // We now need all the classes which will loaded within the class loader for this DApp (includes Helper and userlib classes we add).
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes, Helpers.loadDefaultHelperBytecode());
        
        // Construct the per-contract class loader.
        AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);
        
        // Load all the user-defined classes (these are required for both loading and storing state).
        // (we do this in alphabetical order since the persistence model needs consistent read/write order).
        List<Class<?>> aphabeticalContractClasses = Helpers.getAlphabeticalUserTransformedDappClasses(classLoader, app.classes.keySet());

        // We now have all the information to describe the LoadedDApp.
        return new LoadedDApp(classLoader, aphabeticalContractClasses, app.mainClass, preserveDebuggability);
    }
}
