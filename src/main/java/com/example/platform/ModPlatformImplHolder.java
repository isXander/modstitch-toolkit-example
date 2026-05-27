package com.example.platform;

public class ModPlatformImplHolder {
    static ModPlatform impl;

    public static void setImpl(ModPlatform impl) {
        ModPlatformImplHolder.impl = impl;
    }
}
