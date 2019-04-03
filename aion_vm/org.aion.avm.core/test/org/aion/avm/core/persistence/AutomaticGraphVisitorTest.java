package org.aion.avm.core.persistence;

import java.lang.reflect.Field;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class AutomaticGraphVisitorTest {
    private SimpleAvm avm;
    private Class<?> primaryClass;
    private Class<?> secondaryClass;

    @Before
    public void setup() throws Exception {
        boolean preserveDebuggability = false;
        this.avm = new SimpleAvm(1_000_000L, preserveDebuggability, AutomaticGraphVisitorTargetPrimary.class, AutomaticGraphVisitorTargetSecondary.class);
        AvmClassLoader loader = avm.getClassLoader();
        
        this.primaryClass = loader.loadUserClassByOriginalName(AutomaticGraphVisitorTargetPrimary.class.getName(), preserveDebuggability);
        this.secondaryClass = loader.loadUserClassByOriginalName(AutomaticGraphVisitorTargetSecondary.class.getName(), preserveDebuggability);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void createPrimary() throws Exception {
        Object primary = this.primaryClass.getConstructor().newInstance();
        Assert.assertEquals(42, this.primaryClass.getDeclaredField("avm_value").getInt(primary));
    }

    @Test
    public void manipulatePrimaryFinalField() throws Exception {
        Object primary = this.primaryClass.getConstructor().newInstance();
        Field valueField = this.primaryClass.getDeclaredField("avm_value");
        Assert.assertEquals(42,valueField.getInt(primary));
        valueField.setInt(primary, 100);
        Assert.assertEquals(100,valueField.getInt(primary));
    }

    @Test
    public void createSecondaryDirect() throws Exception {
        Object secondary = this.secondaryClass.getConstructor(int.class).newInstance(5);
        Assert.assertEquals(5, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.secondaryClass.getMethod("avm_setValue", int.class).invoke(secondary, 6);
        Assert.assertEquals(6, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }

    @Test
    public void createSecondaryThroughPrimary() throws Exception {
        Object secondary = this.primaryClass.getMethod("avm_createSecondary", int.class, int.class).invoke(null, 5, 6);
        Assert.assertEquals(6, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.primaryClass.getMethod("avm_changeAgain", this.secondaryClass, int.class).invoke(null, secondary, 7);
        Assert.assertEquals(7, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }

    @Test
    public void createSecondarySpecialConstructor() throws Exception {
        Object secondary = this.secondaryClass.getConstructor(IDeserializer.class, IPersistenceToken.class).newInstance(null, null);
        Assert.assertEquals(0, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.secondaryClass.getMethod("avm_setValue", int.class).invoke(secondary, 1);
        Assert.assertEquals(1, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }
}
