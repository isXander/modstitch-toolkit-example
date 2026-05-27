package com.example.fabric.platform;

import com.example.platform.ModPlatform;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public class FabricModPlatform implements ModPlatform {
    @Override
    public void registerAssetReloadListener(Identifier id, PreparableReloadListener listener) {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                id, listener
        );
    }
}
