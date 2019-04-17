package org.aion.avm.core.rejection;

import org.aion.avm.internal.AvmException;


/**
 * Throw by RejectionVisitor when it detects a violation of one of its rules.
 * This is a RuntimeException since it is thrown from deep within the visitor machinery and we want to catch it at the top-level.
 */
public class RejectedClassException extends AvmException {
    private static final long serialVersionUID = 1L;

    public static void unsupportedClassVersion(int version) {
        throw new RejectedClassException("Unsupported class version: " + version);
    }

    public static void blacklistedOpcode(int opcode) {
        throw new RejectedClassException("Blacklisted Opcode detected: 0x" + Integer.toHexString(opcode));
    }

    public static void nonWhiteListedClass(String className) {
        throw new RejectedClassException("Class is not on white-list: " + className);
    }

    public static void forbiddenMethodOverride(String methodName) {
        throw new RejectedClassException("Attempted to override forbidden method: " + methodName);
    }

    public static void invalidMethodFlag(String methodName, String flagName) {
        throw new RejectedClassException("Method \"" + methodName + "\" has invalid/forbidden access flag: " + flagName);
    }

    public static void restrictedSuperclass(String className, String superName) {
        throw new RejectedClassException(className + " attempted to subclass restricted class: " + superName);
    }

    public static void jclMethodNotImplemented(String receiver, String name, String descriptor) {
        throw new RejectedClassException("JCL implementation missing method: " + receiver + "#" + name + descriptor);
    }

    public static void nameTooLong(String className) {
        throw new RejectedClassException("Class name is too long: " + className);
    }


    public RejectedClassException(String message) {
        super(message);
    }
}
