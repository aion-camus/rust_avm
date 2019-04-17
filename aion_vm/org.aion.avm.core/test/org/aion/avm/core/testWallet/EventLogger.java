package org.aion.avm.core.testWallet;

import avm.Address;
import avm.Blockchain;


/**
 * For now, we just use the method name as the indexable argument to the log, with no payload.
 * In the future, we may change how we expose event logging.
 */
public class EventLogger {
    public static final String kConfirmation = "confirmation";
    public static final String kRevoke = "revoke";
    public static final String kOwnerChanged = "ownerChanged";
    public static final String kOwnerAdded = "ownerAdded";
    public static final String kOwnerRemoved = "ownerRemoved";
    public static final String kRequirementChanged = "requirementChanged";
    public static final String kDeposit = "deposit";
    public static final String kSingleTransact = "singleTransact";
    public static final String kMultiTransact = "multiTransact";
    public static final String kConfirmationNeeded = "confirmationNeeded";

    public static void confirmation(Address sender, Operation operation) {
        byte[] senderBytes = sender.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] data = ByteArrayHelpers.concatenate(senderBytes, operationBytes);
        Blockchain.log(kConfirmation.getBytes(), data);
    }

    public static void revoke(Address sender, Operation operation) {
        byte[] senderBytes = sender.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] data = ByteArrayHelpers.concatenate(senderBytes, operationBytes);
        Blockchain.log(kRevoke.getBytes(), data);
    }

    public static void ownerChanged(Address oldOwner, Address newOwner) {
        byte[] oldBytes = oldOwner.unwrap();
        byte[] newBytes = newOwner.unwrap();
        byte[] data = ByteArrayHelpers.concatenate(oldBytes, newBytes);
        Blockchain.log(kOwnerChanged.getBytes(), data);
    }

    public static void ownerAdded(Address newOwner) {
        byte[] data = newOwner.unwrap();
        Blockchain.log(kOwnerAdded.getBytes(), data);
    }

    public static void ownerRemoved(Address oldOwner) {
        byte[] data = oldOwner.unwrap();
        Blockchain.log(kOwnerRemoved.getBytes(), data);
    }

    public static void requirementChanged(int newRequired) {
        byte[] data = ByteArrayHelpers.encodeInt(newRequired);
        Blockchain.log(kRequirementChanged.getBytes(), data);
    }

    public static void deposit(Address from, long value) {
        byte[] fromBytes = from.unwrap();
        byte[] data = ByteArrayHelpers.appendLong(fromBytes, value);
        Blockchain.log(kDeposit.getBytes(), data);
    }

    public static void singleTransact(Address owner, long value, Address to, byte[] data) {
        byte[] ownerBytes = owner.unwrap();
        byte[] ownerValue = ByteArrayHelpers.appendLong(ownerBytes, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(ownerValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        Blockchain.log(kSingleTransact.getBytes(), finalData);
    }

    public static void multiTransact(Address owner, Operation operation, long value, Address to, byte[] data) {
        byte[] ownerBytes = owner.unwrap();
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] ownerOperation = ByteArrayHelpers.concatenate(ownerBytes, operationBytes);
        byte[] ownerOperationValue = ByteArrayHelpers.appendLong(ownerOperation, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(ownerOperationValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        Blockchain.log(kMultiTransact.getBytes(), finalData);
    }

    public static void confirmationNeeded(Operation operation, Address initiator, long value, Address to, byte[] data) {
        byte[] operationBytes = operation.getByteArrayAccess();
        byte[] initBytes = initiator.unwrap();
        byte[] operationInit = ByteArrayHelpers.concatenate(operationBytes, initBytes);
        byte[] operationInitValue = ByteArrayHelpers.appendLong(operationInit, value);
        byte[] toBytes = to.unwrap();
        byte[] partial = ByteArrayHelpers.concatenate(operationInitValue, toBytes);
        byte[] finalData = ByteArrayHelpers.concatenate(partial, data);
        Blockchain.log(kConfirmationNeeded.getBytes(), finalData);
    }
}
