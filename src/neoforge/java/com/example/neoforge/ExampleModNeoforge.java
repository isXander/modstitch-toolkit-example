package com.example.neoforge;

import com.example.ExampleMod;
import com.example.neoforge.platform.NeoforgeModPlatform;
import com.example.platform.ModPlatformImplHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ExampleMod.MOD_ID)
public class ExampleModNeoforge {
    public ExampleModNeoforge(IEventBus modEventBus) {
        ModPlatformImplHolder.setImpl(new NeoforgeModPlatform(modEventBus));
        ExampleMod.getInstance().init();
    }
}
