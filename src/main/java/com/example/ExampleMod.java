package com.example;

import com.example.platform.ModPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class ExampleMod {
    public static final String MOD_ID = "example_mod";
    private static ExampleMod instance;

    public void init() {
        ModPlatform.get().registerAssetReloadListener(
                Identifier.fromNamespaceAndPath(MOD_ID, "example_reload_listener"),
                (currentReload, taskExecutor, preparationBarrier, reloadExecutor) ->
                        CompletableFuture.completedFuture(null)
                                .thenAccept(preparationBarrier::wait)
        );

        Minecraft.getInstance();
    }

    public static ExampleMod getInstance() {
        if (instance == null) {
            instance = new ExampleMod();
        }
        return instance;
    }
}
