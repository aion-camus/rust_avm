#!/bin/bash

# Build the AVM project
( cd aion_vm ; ant )

# Build the JNI
( cd jni; ant )
