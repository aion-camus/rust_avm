package org.aion.avm.core.arraywrapping;

import java.util.regex.Pattern;
import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Set;

/**
 * A method visitor that replace access bytecode
 *
 * The following bytecode
 *
 * CALOAD   CASTORE
 * DALOAD   DASTORE
 * FALOAD   FASTORE
 * IALOAD   IASTORE
 * LALOAD   LASTORE
 * SALOAD   SASTORE
 *
 * are replaced with virtual calls on array wrapper object.
 *
 * The following bytecode
 *
 * NEWARRAY
 * ANEWARRAY
 * MULTIANEWARRAY
 *
 * are replaced with static calls on array wrapper class.
 *
 * The following bytecode
 *
 * AALOAD
 * AASTORE
 * BALOAD
 * BASTORE
 *
 * are handled by {@link org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef}
 *
 */

class ArrayWrappingMethodAdapter extends AdviceAdapter implements Opcodes {
    static private Pattern PRIMITIVE_ARRAY_FORMAT = Pattern.compile("[\\$\\[]+[IJZBSDFC]");

    private Type typeA = Type.getType(org.aion.avm.arraywrapper.IArray.class);
    private Type typeBA = Type.getType(ByteArray.class);
    private Type typeZA = Type.getType(BooleanArray.class);
    private Type typeCA = Type.getType(CharArray.class);
    private Type typeDA = Type.getType(DoubleArray.class);
    private Type typeFA = Type.getType(FloatArray.class);
    private Type typeIA = Type.getType(IntArray.class);
    private Type typeLA = Type.getType(LongArray.class);
    private Type typeSA = Type.getType(ShortArray.class);

    private static final Set<String> SHADOW_JDK_ENUM_DESC = Set.of(new String[] {
            "()[Lorg/aion/avm/shadow/java/math/RoundingMode;",
            "()[Lorg/aion/avm/shadow/java/util/concurrent/TimeUnit;",
    });


    ArrayWrappingMethodAdapter(final MethodVisitor mv, final int access, final String name, final String desc)
    {
        super(Opcodes.ASM6, mv, access, name, desc);
    }

    @Override
    public void visitInsn(final int opcode) {

        Method m;

        switch (opcode) {
            // Static type
            case Opcodes.CALOAD:
                m = Method.getMethod("char get(int)");
                invokeVirtual(typeCA, m);
                break;
            case Opcodes.DALOAD:
                m = Method.getMethod("double get(int)");
                invokeVirtual(typeDA, m);
                break;
            case Opcodes.FALOAD:
                m = Method.getMethod("float get(int)");
                invokeVirtual(typeFA, m);
                break;
            case Opcodes.IALOAD:
                m = Method.getMethod("int get(int)");
                invokeVirtual(typeIA, m);
                break;
            case Opcodes.LALOAD:
                m = Method.getMethod("long get(int)");
                invokeVirtual(typeLA, m);
                break;
            case Opcodes.SALOAD:
                m = Method.getMethod("short get(int)");
                invokeVirtual(typeSA, m);
                break;
            case Opcodes.CASTORE:
                m = Method.getMethod("void set(int, char)");
                invokeVirtual(typeCA, m);
                break;
            case Opcodes.DASTORE:
                m = Method.getMethod("void set(int, double)");
                invokeVirtual(typeDA, m);
                break;
            case Opcodes.FASTORE:
                m = Method.getMethod("void set(int, float)");
                invokeVirtual(typeFA, m);
                break;
            case Opcodes.IASTORE:
                m = Method.getMethod("void set(int, int)");
                invokeVirtual(typeIA, m);
                break;
            case Opcodes.LASTORE:
                m = Method.getMethod("void set(int, long)");
                invokeVirtual(typeLA, m);
                break;
            case Opcodes.SASTORE:
                m = Method.getMethod("void set(int, short)");
                invokeVirtual(typeSA, m);
                break;
            case Opcodes.ARRAYLENGTH:
                m = Method.getMethod("int length()");
                invokeInterface(typeA, m);
                break;

            case Opcodes.AALOAD:
                throw RuntimeAssertionError.unreachable("Primitive array wrapping adapter catch AALOAD");
            case Opcodes.AASTORE:
                throw RuntimeAssertionError.unreachable("Primitive array wrapping adapter catch AASTORE");

            default:
                this.mv.visitInsn(opcode);
        }
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        Method m;

        if (opcode == Opcodes.NEWARRAY) {
            switch (operand) {
                case Opcodes.T_BOOLEAN:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "BooleanArray initArray(int)");
                    invokeStatic(typeZA, m);
                    break;
                case Opcodes.T_BYTE:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "ByteArray initArray(int)");
                    invokeStatic(typeBA, m);
                    break;
                case Opcodes.T_SHORT:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "ShortArray initArray(int)");
                    invokeStatic(typeSA, m);
                    break;
                case Opcodes.T_INT:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "IntArray initArray(int)");
                    invokeStatic(typeIA, m);
                    break;
                case Opcodes.T_LONG:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "LongArray initArray(int)");
                    invokeStatic(typeLA, m);
                    break;
                case Opcodes.T_CHAR:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "CharArray initArray(int)");
                    invokeStatic(typeCA, m);
                    break;
                case Opcodes.T_FLOAT:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "FloatArray initArray(int)");
                    invokeStatic(typeFA, m);
                    break;
                case Opcodes.T_DOUBLE:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "DoubleArray initArray(int)");
                    invokeStatic(typeDA, m);
                    break;
                default:
                    this.mv.visitIntInsn(opcode, operand);
            }
        }else{
            this.mv.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitTypeInsn(int opcode, java.lang.String type){

        String wName;

        switch(opcode){
            case Opcodes.ANEWARRAY:
                // allows us to continue to do invokestatic but then return in terms of unifying type.
                if (type.startsWith("[")){
                    wName = ArrayNameMapper.getPreciseArrayWrapperDescriptor("[" + type);
                }else{
                    wName = ArrayNameMapper.getPreciseArrayWrapperDescriptor("[L" + type);
                }

                this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, wName, "initArray", "(I)L" + wName + ";", false);
                break;

            case Opcodes.CHECKCAST: {
                wName = type;
                if (type.startsWith("[")) {
                    wName = ArrayNameMapper.getUnifyingArrayWrapperDescriptor(type);
                }
                this.mv.visitTypeInsn(opcode, wName);
                break;
            }
            case Opcodes.INSTANCEOF: {
                wName = type;
                if (type.startsWith("[")) {
                    wName = ArrayNameMapper.getUnifyingArrayWrapperDescriptor(type);
                }
                this.mv.visitTypeInsn(opcode, wName);
                break;
            }
            default:
                this.mv.visitTypeInsn(opcode, type);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        String desc = descriptor;
        if (name.equals("avm_values") && SHADOW_JDK_ENUM_DESC.contains(descriptor)){
            desc = "()[L" + PackageConstants.kShadowSlashPrefix + "java/lang/Object;";
        } else if ((name.equals("avm_clone") && (!PRIMITIVE_ARRAY_FORMAT.matcher(owner).matches()))) {
            opcode = INVOKEINTERFACE;
            isInterface = true;
        }

        desc = ArrayNameMapper.updateMethodDesc(desc);
        String newOwner = ArrayNameMapper.getUnifyingArrayWrapperDescriptor(owner);
        this.mv.visitMethodInsn(opcode, newOwner, name, desc, isInterface);
    }

    @Override
    public void visitLocalVariable(java.lang.String name,
                               java.lang.String descriptor,
                               java.lang.String signature,
                               Label start,
                               Label end,
                               int index)
    {
        String desc = descriptor;
        if (descriptor.startsWith("[")) {
            desc = "L" + ArrayNameMapper.getUnifyingArrayWrapperDescriptor(descriptor) + ";";
        }

        this.mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitFieldInsn(int opcode,
                           java.lang.String owner,
                           java.lang.String name,
                           java.lang.String descriptor)
    {
        String desc = descriptor;
        if (descriptor.startsWith("[")) {
            desc = "L" + ArrayNameMapper.getUnifyingArrayWrapperDescriptor(descriptor) + ";";
        }

        this.mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(java.lang.String descriptor, int d)
    {
        String wName = ArrayNameMapper.getPreciseArrayWrapperDescriptor(descriptor);
        String facDesc = ArrayNameMapper.getFactoryDescriptor(wName, d);

        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, wName, "initArray", facDesc, false);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        String desc = ArrayNameMapper.updateMethodDesc(descriptor);

        super.visitInvokeDynamicInsn(name, desc, bootstrapMethodHandle, bootstrapMethodArguments);
    }
}
