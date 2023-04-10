package com.sleepwalker.luckperms.common.plugin.classpath;

import me.lucko.luckperms.forge.LPForgeBootstrap;
import me.lucko.luckperms.forge.capabilities.UserCapabilityImpl;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;

public class LPLoader {

    @Nonnull
    private final LPForgeBootstrap bootstrap;

    public LPLoader(@Nonnull LPForgeBootstrap bootstrap) {
        this.bootstrap = bootstrap;

    }

    @SubscribeEvent
    public void setup(@Nonnull FMLCommonSetupEvent event){
        UserCapabilityImpl.register();
        bootstrap.onLoad();
    }
}
