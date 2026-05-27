package com.example.neoforge.platform;

import com.example.ExampleMod;
import com.example.platform.ModPlatform;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public record NeoforgeModPlatform(IEventBus modEventBus) implements ModPlatform {
    @Override
    public void registerAssetReloadListener(Identifier id, PreparableReloadListener listener) {
        modEventBus().<AddClientReloadListenersEvent>addListener(e -> {
            e.addListener(id, listener);
        });
    }
}
