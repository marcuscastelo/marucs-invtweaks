package io.github.marcuscastelo.invtweaks.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import static io.github.marcuscastelo.invtweaks.InvTweaksMod.LOGGER;

public class ChatUtils {
    public static void warnPlayer(String message, boolean onlyForDev) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (onlyForDev && !player.getName().equals("vi121")) {
            LOGGER.info("Skipping warning for player " + player.getName() + ": " + message + " (only for dev)");
            return;
        }
        player.sendMessage(Text.literal(message), false);
    }

    public static void warnPlayer(String message) {
        warnPlayer(message, false);
    }
}
