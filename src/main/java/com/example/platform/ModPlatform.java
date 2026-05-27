package com.example.platform;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface ModPlatform {
    static ModPlatform get() {
        return ModPlatformImplHolder.impl;
    }

    void registerAssetReloadListener(Identifier id, PreparableReloadListener listener);
}
