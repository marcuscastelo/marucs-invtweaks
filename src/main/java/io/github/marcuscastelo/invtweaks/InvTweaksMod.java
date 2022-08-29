package io.github.marcuscastelo.invtweaks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvTweaksMod implements ModInitializer, ClientModInitializer {
    //Logger logger = LogManager.getLogger();
    public static final String MOD_ID = "marucs-invtweaks";
    public static final String MOD_NAME = "InvTweaks";
    public static final String MOD_VERSION = "1.0.0";
    public static final String MOD_AUTHOR = "Marcus Castelo";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Ready");
    }
}
