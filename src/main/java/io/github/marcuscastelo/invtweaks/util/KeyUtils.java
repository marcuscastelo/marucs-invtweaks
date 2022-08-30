package io.github.marcuscastelo.invtweaks.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class KeyUtils {
    public static boolean isKeyPressed(int key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key);
    }
}
