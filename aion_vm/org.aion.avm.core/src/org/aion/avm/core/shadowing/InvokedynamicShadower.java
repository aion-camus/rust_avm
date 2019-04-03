package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;

/**
 * Class visitor dedicated for invokedyanmic. Currently supported types:
 * 1) String concatenation
 * 2) Lambda expression
 * <p>
 * NOTE: this visitor requires the {@link IObjectReplacer} to deal with Object-IObject
 * replacement in method handle.
 *
 * @author Roman Katerinenko
 */
public class InvokedynamicShadower extends ClassToolchain.ToolChainClassVisitor {
    private final IObjectReplacer replacer;
    private final String postRenameStringConcatFactory;
    private final String postRenameLambdaFactory;

    public InvokedynamicShadower(String shadowPackage) {
        super(Opcodes.ASM6);
        replacer = new IObjectReplacer(shadowPackage);
        postRenameStringConcatFactory = shadowPackage + "java/lang/invoke/StringConcatFactory";
        postRenameLambdaFactory = shadowPackage + "java/lang/invoke/LambdaMetafactory";
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, null, exceptions);
        return new IndyMethodVisitor(mv);
    }

    private final class IndyMethodVisitor extends MethodVisitor {
        private IndyMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM6, methodVisitor);
        }

        @Override
        public void visitInvokeDynamicInsn(String origMethodName, String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            String methodOwner = bootstrapMethodHandle.getOwner();
            if (isStringConcatIndy(methodOwner, origMethodName)) {
                handleStringConcatIndy(origMethodName, methodDescriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            } else if (isLambdaIndy(methodOwner)) {
                handleLambdaIndy(origMethodName, methodDescriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            } else {
                throw new IllegalStateException("Unsupported invokedymanic: boostrap:" + origMethodName + " owner:" + methodOwner);
            }
        }

        private boolean isStringConcatIndy(String owner, String origMethodName) {
            return postRenameStringConcatFactory.equals(owner) && NamespaceMapper.mapMethodName("makeConcatWithConstants").equals(origMethodName);
        }

        private boolean isLambdaIndy(String owner) {
            return postRenameLambdaFactory.equals(owner);
        }

        private void handleLambdaIndy(String origMethodName, String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            final String newMethodName = origMethodName;
            final String newMethodDescriptor = replacer.replaceMethodDescriptor(methodDescriptor);
            final Handle newHandle = newLambdaHandleFrom(bootstrapMethodHandle, false);
            final Object[] newBootstrapMethodArgs = newShadowLambdaArgsFrom(bootstrapMethodArguments);
            super.visitInvokeDynamicInsn(newMethodName, newMethodDescriptor, newHandle, newBootstrapMethodArgs);
        }

        private void handleStringConcatIndy(String origMethodName, String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            // Note that we currently only use the avm_makeConcatWithConstants invoked name.
            RuntimeAssertionError.assertTrue("avm_makeConcatWithConstants".equals(origMethodName));
            final String newMethodDescriptor = replacer.replaceMethodDescriptor(methodDescriptor);
            final Handle newHandle = newLambdaHandleFrom(bootstrapMethodHandle, false);
            super.visitInvokeDynamicInsn(origMethodName, newMethodDescriptor, newHandle, bootstrapMethodArguments);
        }

        private Handle newLambdaHandleFrom(Handle origHandle, boolean shadowMethodDescriptor) {
            final String owner = origHandle.getOwner();
            final String newOwner = owner;
            final String newMethodName = origHandle.getName();
            final String newMethodDescriptor = shadowMethodDescriptor ? replacer.replaceMethodDescriptor(origHandle.getDesc()) : origHandle.getDesc();
            return new Handle(origHandle.getTag(), newOwner, newMethodName, newMethodDescriptor, origHandle.isInterface());
        }

        private Object[] newShadowLambdaArgsFrom(Object[] origArgs) {
            final var newArgs = new ArrayList<>(origArgs.length);
            for (final Object origArg : origArgs) {
                final Object newArg;
                if (origArg instanceof Type) {
                    newArg = newMethodTypeFrom((Type) origArg);
                } else if (origArg instanceof Handle) {
                    newArg = newLambdaHandleFrom((Handle) origArg, true);
                } else {
                    newArg = origArg;
                }
                newArgs.add(newArg);
            }
            return newArgs.toArray();
        }

        private Type newMethodTypeFrom(Type origType) {
            return Type.getMethodType(replacer.replaceMethodDescriptor(origType.getDescriptor()));
        }
    }
}
