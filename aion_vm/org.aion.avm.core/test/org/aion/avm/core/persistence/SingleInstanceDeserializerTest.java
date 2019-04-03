package org.aion.avm.core.persistence;

import java.util.Arrays;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.InstrumentationHelpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SingleInstanceDeserializerTest {
    private static final SingleInstanceDeserializer.IAutomatic NULL_AUTOMATIC = new SingleInstanceDeserializer.IAutomatic() {
        @Override
        public void partialAutomaticDeserializeInstance(SerializedRepresentationCodec.Decoder decoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        }
        @Override
        public org.aion.avm.shadow.java.lang.Object decodeStub(INode node) {
            // We just check null, or not.
            return (null == node)
                    ? null
                    : new ObjectArray(1);
        }};

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
    }

    @Test
    public void deserializeByteArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x1,
                0x2,
                0x3,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        ByteArray bytes = new ByteArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new byte[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeShortArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x1,
                0x0, 0x2,
                0x0, 0x3,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        ShortArray bytes = new ShortArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new short[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeCharArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x1,
                0x0, 0x2,
                0x0, 0x3,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        CharArray bytes = new CharArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new char[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeIntArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x0, 0x0, 0x1,
                0x0, 0x0, 0x0, 0x2,
                0x0, 0x0, 0x0, 0x3,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        IntArray bytes = new IntArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new int[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeFloatArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x3f, (byte)0x80, 0x0, 0x0,
                0x40, 0x0, 0x0, 0x0,
                0x40, 0x40, 0x0, 0x0,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        FloatArray bytes = new FloatArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new float[] {1.0f,2.0f,3.0f}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeLongArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x3,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        LongArray bytes = new LongArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new long[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeDoubleArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x3f, (byte)0xf0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x40, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x40, 0x8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        DoubleArray bytes = new DoubleArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new double[] {1.0,2.0,3.0}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeShadowString() {
        // This test needs to instantiate a shadow object, meaning it needs a stack frame.
        CommonInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        Helper runtimeSetup = new Helper();
        InstrumentationHelpers.pushNewStackFrame(runtimeSetup, SingleInstanceDeserializerTest.class.getClassLoader(), 1_000_000L, 1, null);
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x4, //UTF-8 length
                0x54,
                0x45,
                0x53,
                0x54,
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[0]));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        org.aion.avm.shadow.java.lang.String bytes = new org.aion.avm.shadow.java.lang.String((String)null);
        bytes.deserializeSelf(null, target);
        Assert.assertEquals("TEST", bytes.getUnderlying());
        
        // Clear the frame.
        InstrumentationHelpers.popExistingStackFrame(runtimeSetup);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    @Test
    public void serializeObjectArray() {
        // (note that we are using the fake stub encoding, in NULL_AUTOMATIC).
        byte[] expected = {
                0x0, 0x0, 0x0, 0x2, //hashcode
                0x0, 0x0, 0x0, 0x1, //length
        };
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(new SerializedRepresentation(expected, new INode[] {null}));
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        ObjectArray bytes = new ObjectArray(null, null);
        bytes.deserializeSelf(null, target);
        Assert.assertEquals(1, bytes.length());
        Assert.assertNull(bytes.get(0));
    }
}
