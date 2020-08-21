package io.github.marcuscastelo.invtweaks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class InvTweaksMod implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[Marucs' InvTweaks] Ready!");
    }

    @Override
    public void onInitialize() {}
}
