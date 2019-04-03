package org.aion.avm.core.instrument;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class ClassMeteringTest {

    private Class<?> clazz;
    // We keep this here just because a lot of cases want to use this sort of "do no instrumentation" cost builder for testing other rewrites.
    private final Function<byte[], byte[]> commonCostBuilder = (inputBytes) ->
            new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                    .addNextVisitor(new ClassMetering(null))
                    .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                    .build()
                    .runAndGetBytecode();

    @Before
    public void setup() throws Exception {
        // Clear the state of our static test class.
        TestEnergy.totalCost = 0;
        TestEnergy.totalCharges = 0;
        TestEnergy.totalArrayElements = 0;
        TestEnergy.totalArrayInstances = 0;

        // Setup and rewrite the class.
        String className = TestResource.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, this.commonCostBuilder.apply(raw));
        byte[] stubBytecode = Helpers.loadRequiredResourceAsBytes(HelperStub.CLASS_NAME + ".class");
        Map<String, byte[]> classesAndHelper = Helpers.mapIncludingHelperBytecode(classes, stubBytecode);
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classesAndHelper);
        this.clazz = loader.loadClass(className);
    }

    /**
     * Tests that we can add a instrumented callout to the beginning of each block.
     */
    @Test
    public void testWrittenBlockPrefix() throws Exception {
        // By this point, we should still have 0 charges.
        Assert.assertEquals(0, TestEnergy.totalCharges);
        Object target = clazz.getConstructor(int.class).newInstance(6);
        // We expect to see 1 charge for init - it is only 1 block.
        Assert.assertEquals(1, TestEnergy.totalCharges);
        long expectedCost = getFees(
                Opcodes.ALOAD
                , Opcodes.INVOKESPECIAL
                , Opcodes.ALOAD
                , Opcodes.ILOAD
                , Opcodes.PUTFIELD
                , Opcodes.RETURN
        );
        Assert.assertEquals(expectedCost, TestEnergy.totalCost);
        target.hashCode();
        // Now, we should see the additional charge for the hashcode block.
        Assert.assertEquals(2, TestEnergy.totalCharges);
        expectedCost += getFees(
                Opcodes.ALOAD
                , Opcodes.GETFIELD
                , Opcodes.IRETURN
        );
        Assert.assertEquals(expectedCost, TestEnergy.totalCost);
    }

    @Test
    public void testInterface() throws Exception {
        // Setup and rewrite the interface.
        String interfaceName = TestInterface.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(interfaceName.replaceAll("\\.", "/") + ".class");
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(interfaceName, this.commonCostBuilder.apply(raw));
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        Class<?> theInterface = loader.loadClass(interfaceName);
        Method method = theInterface.getMethod("one");
        Assert.assertNotNull(method);
    }

    private long getFees(int... opcodes) {
        long total = 0;
        
        BytecodeFeeScheduler fees = new BytecodeFeeScheduler();
        fees.initialize();
        for (int opcode : opcodes) {
            total += fees.getFee(opcode);
        }
        return total;
    }


    /**
     * NOTE:  This class is used for the "testWrittenBlockPrefix()" test.
     */
    public static class TestEnergy {
        public static long totalCost;
        public static int totalCharges;
        public static int totalArrayElements;
        public static int totalArrayInstances;
    }

    public static class HelperStub {
        public static String CLASS_NAME = Helpers.fulllyQualifiedNameToInternalName(HelperStub.class.getName());
        public static void chargeEnergy(long cost) {
            TestEnergy.totalCost += cost;
            TestEnergy.totalCharges += 1;
        }
    }
}
