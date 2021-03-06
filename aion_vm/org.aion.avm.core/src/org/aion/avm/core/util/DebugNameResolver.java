package org.aion.avm.core.util;

import org.aion.avm.internal.PackageConstants;

public class DebugNameResolver {

    public static String getUserPackageSlashPrefix (String name, boolean preserveDebuggability){
        return preserveDebuggability ? name : PackageConstants.kUserSlashPrefix + name;
    }

    public static String getUserPackageDotPrefix (String name, boolean preserveDebuggability){
        return preserveDebuggability ? name : PackageConstants.kUserDotPrefix + name;
    }
}
