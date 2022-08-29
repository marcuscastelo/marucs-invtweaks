package io.github.marcuscastelo.invtweaks.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class ChatUtils {
    public static void warnPlayer(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        player.sendMessage(Text.literal(message), false);
    }
}
