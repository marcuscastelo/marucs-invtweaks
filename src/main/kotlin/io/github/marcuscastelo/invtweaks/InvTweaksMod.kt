package io.github.marcuscastelo.invtweaks

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory


class InvTweaksMod : ModInitializer, ClientModInitializer {
    companion object {
        //Logger logger = LogManager.getLogger();
        const val MOD_ID = "marucs-invtweaks"
        const val MOD_NAME = "InvTweaks"
        const val MOD_VERSION = "1.0.0"
        const val MOD_AUTHOR = "Marcus Castelo"

        @JvmStatic
        val LOGGER = LoggerFactory.getLogger(MOD_ID)
    }

    override fun onInitializeClient() {}
    override fun onInitialize() {
        LOGGER.info("Ready")
    }

}