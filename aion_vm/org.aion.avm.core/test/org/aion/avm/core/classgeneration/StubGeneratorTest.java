package org.aion.avm.core.classgeneration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.PackageConstants;
import org.junit.Assert;
import org.junit.Test;


public class StubGeneratorTest {
    @Test
    public void testBasics() throws Exception {
        String slashName = "my/test/ClassName";
        String dotName = slashName.replaceAll("/", ".");
        String superName = TestClass.class.getName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateWrapperClass(slashName, superName);
        Class<?> clazz = Loader.loadClassAlone(dotName, bytecode);
        Constructor<?> con = clazz.getConstructor(Object.class);
        String contents = "one";
        Object foo = con.newInstance(contents);
        Assert.assertEquals(dotName, foo.getClass().getName());
        TestClass bar = (TestClass)foo;
        Assert.assertEquals(contents, bar.getContents());
    }

    /**
     * Tests that our approach for generating all the exception shadows is correct.
     * The approach here is to find the original and shadow "java.lang.Throwable", walk the original, building a parallel tree in the shadow.
     * NOTE:  This test is really just a temporary landing zone for some generation code until its real location is ready, in the core.
     */
    @Test
    public void testGenerateExceptionShadows() throws Exception {
        ClassLoader parent = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        AvmClassLoader loader = generateExceptionShadowsAndWrappers(parent);
        Class<?> aioobe = loader.loadClass(PackageConstants.kShadowDotPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getName());
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getName());
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getName());
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getName());
        
        IRuntimeSetup runtimeSetup = new Helper();
        IInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        InstrumentationHelpers.pushNewStackFrame(runtimeSetup, loader, 5L, 1, null);
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = aioobe.getConstructor(org.aion.avm.shadow.java.lang.String.class);
        org.aion.avm.shadow.java.lang.String contents = new org.aion.avm.shadow.java.lang.String("one");
        Object instance = con.newInstance(contents);
        org.aion.avm.shadow.java.lang.Throwable shadow = (org.aion.avm.shadow.java.lang.Throwable)instance;
        // Ask for the toString (our internal version) since we know what that should look like.
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.ArrayIndexOutOfBoundsException: one", shadow.toString());
        
        InstrumentationHelpers.popExistingStackFrame(runtimeSetup);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    /**
     * This is must like above except we are interested in the generated wrappers _of_ the generated shadows for the built-in types
     */
    @Test
    public void testGenerateExceptionWrappers() throws Exception {
        ClassLoader parent = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        AvmClassLoader loader = generateExceptionShadowsAndWrappers(parent);
        Class<?> aioobe = loader.loadClass(PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        // The interesting thing about the wrappers is that they are actually real Throwables.
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getName());
        Assert.assertEquals(PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getName());
        Assert.assertEquals(PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getName());
        Assert.assertEquals(PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getName());
        Assert.assertEquals("java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getName());
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = aioobe.getConstructor(Object.class);
        String contents = "one";
        Object instance = con.newInstance(contents);
        org.aion.avm.exceptionwrapper.org.aion.avm.shadow.java.lang.Throwable wrapper = (org.aion.avm.exceptionwrapper.org.aion.avm.shadow.java.lang.Throwable)instance;
        // We can just unwrap this one.
        Assert.assertEquals(wrapper.unwrap(), contents);
        // Also, make sure that it is safe to cast this to the actual Throwable.
        Throwable top = (Throwable)wrapper;
        Assert.assertNotNull(top.toString());
    }

    /**
     * We want to verify that a generated shadow with a hand-written super-class has the correct classloaders through its hierarchy.
     */
    @Test
    public void getGeneratedShadowWithHandWrittenSuper() throws Exception {
        ClassLoader handWritten = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and is partially hand-written.
        AvmClassLoader generated = generateExceptionShadowsAndWrappers(handWritten);
        Class<?> aioobe = generated.loadClass(PackageConstants.kShadowDotPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        // We want to make sure that each class loader is what we expect.
        Assert.assertNotNull(aioobe);
        Assert.assertTrue(NodeEnvironment.singleton.isClassFromSharedLoader(aioobe)); // java.lang.ArrayIndexOutOfBoundsException
        Assert.assertTrue(NodeEnvironment.singleton.isClassFromSharedLoader(aioobe.getSuperclass())); // java.lang.IndexOutOfBoundsException
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getClassLoader()); // java.lang.RuntimeException
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getSuperclass().getClassLoader()); // java.lang.Exception
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getClassLoader()); // java.lang.Throwable
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getClassLoader()); // java.lang.Object
    }

    /**
     * Make sure that the "legacy" exceptions work properly (generated, but generated differently than the other cases).
     */
    @Test
    public void testGenerateLegacyExceptionShadows() throws Exception {
        ClassLoader handWritten = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ClassNotFoundException, since it is deep and the legacy style.
        AvmClassLoader generated = generateExceptionShadowsAndWrappers(handWritten);
        Class<?> notFound = generated.loadClass(PackageConstants.kShadowDotPrefix + "java.lang.ClassNotFoundException");
        
        Assert.assertNotNull(notFound);
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.ClassNotFoundException", notFound.getName());
        Assert.assertTrue(NodeEnvironment.singleton.isClassFromSharedLoader(notFound));
        
        Class<?> reflectiveOperationException = notFound.getSuperclass();
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.ReflectiveOperationException", reflectiveOperationException.getName());
        Assert.assertTrue(NodeEnvironment.singleton.isClassFromSharedLoader(reflectiveOperationException));
        
        Class<?> exception = reflectiveOperationException.getSuperclass();
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.Exception", exception.getName());
        Assert.assertEquals(handWritten, exception.getClassLoader());
        
        Class<?> throwable = exception.getSuperclass();
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.Throwable", throwable.getName());
        Assert.assertEquals(handWritten, throwable.getClassLoader());
        
        Class<?> object = throwable.getSuperclass();
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.Object", object.getName());
        Assert.assertEquals(handWritten, object.getClassLoader());
        
        IRuntimeSetup runtimeSetup = new Helper();
        IInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        InstrumentationHelpers.pushNewStackFrame(runtimeSetup, generated, 5L, 1, null);
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = notFound.getConstructor(org.aion.avm.shadow.java.lang.String.class, org.aion.avm.shadow.java.lang.Throwable.class);
        org.aion.avm.shadow.java.lang.String contents = new org.aion.avm.shadow.java.lang.String("one");
        org.aion.avm.shadow.java.lang.Throwable cause = new org.aion.avm.shadow.java.lang.Throwable();
        
        Object instance = con.newInstance(contents, cause);
        org.aion.avm.shadow.java.lang.Throwable shadow = (org.aion.avm.shadow.java.lang.Throwable)instance;
        
        // Call our getException and make sure it is the cause.
        Method getException = notFound.getMethod(NamespaceMapper.mapMethodName("getException"));
        Object result = getException.invoke(shadow);
        Assert.assertTrue(result == cause);
        
        InstrumentationHelpers.popExistingStackFrame(runtimeSetup);
        InstrumentationHelpers.detachThread(instrumentation);
    }


    private static AvmClassLoader generateExceptionShadowsAndWrappers(ClassLoader parent) throws Exception {
        // Get the generated classes.
        Map<String, byte[]> allGenerated = CommonGenerators.generateShadowJDK();
        // This test now falls back to the sharedClassLoader, which makes it kind of redundant, but it at least proves it is working as expected.
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(Collections.emptyMap());
        
        // NOTE:  Given that we can only inject individual classes, we need to add them in the right order.
        // See ExceptionWrappingTest for an example of how this can be fully loaded into the classloader in such a way that the
        // class relationships can be lazily constructed.
        // For now, for this test case, we will just load them in a hard-coded topological order (since we will probably use a
        // different loader, eventually).
        // (note that the CommonGenerators constant happens to be in a safe order so we will use that).
        for (String name : CommonGenerators.kExceptionClassNames) {
            String shadowName = PackageConstants.kShadowDotPrefix + name;
            byte[] shadowBytes = allGenerated.get(shadowName);
            // Note that not all shadow exceptions are generated.
            Class<?> shadowClass = null;
            if (null != shadowBytes) {
                // Verify that these are being served by the shared loader (since these are the statically generated - shared loader).
                Assert.assertTrue(!CommonGenerators.kHandWrittenExceptionClassNames.contains(name));
                shadowClass = loader.loadClass(shadowName);
                Assert.assertTrue(NodeEnvironment.singleton.isClassFromSharedLoader(shadowClass));
            } else {
                // This must be hand-written.
                Assert.assertTrue(CommonGenerators.kHandWrittenExceptionClassNames.contains(name));
                shadowClass = Class.forName(shadowName);
                Assert.assertEquals(parent, shadowClass.getClassLoader());
            }
            
            String wrapperName = PackageConstants.kExceptionWrapperDotPrefix + shadowName;
            byte[] wrapperBytes = allGenerated.get(wrapperName);
            Assert.assertNotNull(wrapperBytes);
            Class<?> wrapperClass = loader.loadClass(wrapperName);
            Assert.assertTrue(NodeEnvironment.singleton.isClassFromSharedLoader(wrapperClass));
        }
        return loader;
    }

    //Note that class names here are always in the dot style:  "java.lang.Object"
    private static class Loader {
        public static Class<?> loadClassAlone(String topName, byte[] bytecode) throws ClassNotFoundException {
            ClassLoader loader = new ClassLoader() {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    Class<?> result = null;
                    if (name.equals(topName)) {
                        result = defineClass(name, bytecode, 0, bytecode.length);
                    } else {
                        result = super.loadClass(name);
                    }
                    return result;
                }
            };
            return loader.loadClass(topName);
        }
    }
}
