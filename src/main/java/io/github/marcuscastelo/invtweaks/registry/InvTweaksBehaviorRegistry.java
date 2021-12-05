package io.github.marcuscastelo.invtweaks.registry;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.api.ScreenInfo;
import io.github.marcuscastelo.invtweaks.client.behavior.InvTweaksVanillaGenericBehavior;
import io.github.marcuscastelo.invtweaks.client.behavior.InvTweaksVanillaMerchantBehavior;
import net.minecraft.screen.*;

import java.util.HashMap;

public class InvTweaksBehaviorRegistry {
    public static HashMap<Class<? extends ScreenHandler>, ScreenInfo> screenBehaviorMap = new HashMap<>();

    public static ScreenInfo register(ScreenInfo screenInfo) {
        if (screenBehaviorMap.containsKey(screenInfo.getHandlerClass())) throw new IllegalArgumentException("Screen " + screenInfo.getHandlerClass() + " is already registered");
        screenBehaviorMap.put(screenInfo.getHandlerClass(), screenInfo);
        return screenInfo;
    }

    public static ScreenInfo getScreenInfo(Class<? extends ScreenHandler> screenHandlerClass) {
        if (!isScreenSupported(screenHandlerClass)) throw new IllegalArgumentException("Screen " + screenHandlerClass + " is not supported");
        return screenBehaviorMap.get(screenHandlerClass);
    }

    public static void executeOperation(Class<? extends ScreenHandler> screenHandlerClass, InvTweaksOperationInfo operationInfo) throws IllegalArgumentException{
        if (!isScreenSupported(screenHandlerClass)) throw new IllegalArgumentException("Screen "  + screenHandlerClass + " doesn't have a behavior");
        operationInfo.type.asOperationExecutor(screenBehaviorMap.get(screenHandlerClass).getInvTweaksBehavior()).execute(operationInfo);
    }

    public static boolean isScreenSupported(Class<? extends ScreenHandler> screenHandlerClass) {
        return screenBehaviorMap.containsKey(screenHandlerClass);
    }

    public static ScreenInfo.Builder createScreenInfoBuilder(Class<? extends ScreenHandler> handlerClass) {
        return new ScreenInfo.Builder(handlerClass);
    }

    public static ScreenInfo.Builder createVanillaGenericScreenInfoBuilder(Class<? extends ScreenHandler> handlerClass) {
        return createScreenInfoBuilder(handlerClass).setBehavior(InvTweaksVanillaGenericBehavior.INSTANCE);
    }

    public static ScreenInfo buildDefaultedVanillaGenericScreenInfo(Class<? extends ScreenHandler> handlerClass) {
        return createVanillaGenericScreenInfoBuilder(handlerClass).build();
    }

    static {
        register(buildDefaultedVanillaGenericScreenInfo(GenericContainerScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(ShulkerBoxScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(CraftingScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(BrewingStandScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(BeaconScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(HopperScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(Generic3x3ContainerScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(FurnaceScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(SmokerScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(BlastFurnaceScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(EnchantmentScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(AnvilScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(CartographyTableScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(LoomScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(SmithingScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(GrindstoneScreenHandler.class));
        register(buildDefaultedVanillaGenericScreenInfo(HorseScreenHandler.class));

        register(createVanillaGenericScreenInfoBuilder(PlayerScreenHandler.class).setPlayerInvTotalSize(37).build());
        register(createScreenInfoBuilder(MerchantScreenHandler.class).setBehavior(InvTweaksVanillaMerchantBehavior.INSTANCE).build());
    }

}
