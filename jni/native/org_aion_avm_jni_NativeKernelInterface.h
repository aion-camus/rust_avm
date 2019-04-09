/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_aion_avm_jni_NativeKernelInterface */

#ifndef _Included_org_aion_avm_jni_NativeKernelInterface
#define _Included_org_aion_avm_jni_NativeKernelInterface
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    createAccount
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_createAccount
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    hasAccountState
 * Signature: (J[B)Z
 */
JNIEXPORT jboolean JNICALL Java_org_aion_avm_jni_NativeKernelInterface_hasAccountState
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    putCode
 * Signature: (J[B[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_putCode
  (JNIEnv *, jclass, jlong, jbyteArray, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    getCode
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_aion_avm_jni_NativeKernelInterface_getCode
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    putStorage
 * Signature: (J[B[B[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_putStorage
  (JNIEnv *, jclass, jlong, jbyteArray, jbyteArray, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    getStorage
 * Signature: (J[B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_aion_avm_jni_NativeKernelInterface_getStorage
  (JNIEnv *, jclass, jlong, jbyteArray, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    deleteAccount
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_deleteAccount
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    getBalance
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_aion_avm_jni_NativeKernelInterface_getBalance
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    increaseBalance
 * Signature: (J[B[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_increaseBalance
  (JNIEnv *, jclass, jlong, jbyteArray, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    decreaseBalance
 * Signature: (J[B[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_decreaseBalance
  (JNIEnv *, jclass, jlong, jbyteArray, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    getNonce
 * Signature: (J[B)J
 */
JNIEXPORT jlong JNICALL Java_org_aion_avm_jni_NativeKernelInterface_getNonce
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    incrementNonce
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_incrementNonce
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    touchAccount
 * Signature: (J[BI)V
 */
JNIEXPORT void JNICALL Java_org_aion_avm_jni_NativeKernelInterface_touchAccount
  (JNIEnv *, jclass, jlong, jbyteArray, jint);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    sendSignal
 * Signature: (JI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_aion_avm_jni_NativeKernelInterface_sendSignal
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     org_aion_avm_jni_NativeKernelInterface
 * Method:    contract_address
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_aion_avm_jni_NativeKernelInterface_contract_1address
  (JNIEnv *, jclass, jbyteArray, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
