package com.example.platform;

public class ModPlatformImplHolder {
    static ModPlatform impl;

    // Dependency injection for platform-specific utilities
    public static void setImpl(ModPlatform impl) {
        ModPlatformImplHolder.impl = impl;
    }
}
