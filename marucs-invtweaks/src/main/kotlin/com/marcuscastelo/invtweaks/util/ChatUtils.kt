package com.marcuscastelo.invtweaks.util

import com.marcuscastelo.invtweaks.InvTweaksMod
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object ChatUtils {
    @JvmStatic
    @JvmOverloads
    fun warnPlayer(message: String, onlyForDev: Boolean = false) {
        val player = MinecraftClient.getInstance().player
        if (onlyForDev && player!!.name.content.toString() == "vi121") {
            com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.info("Skipping warning for player " + player.name + ": " + message + " (only for dev)")
            return
        }
        player!!.sendMessage(Text.literal(message), false)
    }
}