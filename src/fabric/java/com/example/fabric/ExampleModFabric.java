package com.example.fabric;

import com.example.ExampleMod;
import com.example.fabric.platform.FabricModPlatform;
import com.example.platform.ModPlatformImplHolder;
import net.fabricmc.api.ModInitializer;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModPlatformImplHolder.setImpl(new FabricModPlatform());
        ExampleMod.getInstance().init();
    }
}
