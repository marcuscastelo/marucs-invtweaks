package io.github.marcuscastelo.invtweaks.registry;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.client.behavior.InvTweaksVanillaGenericBehavior;
import io.github.marcuscastelo.invtweaks.client.behavior.InvTweaksVanillaMerchantBehavior;
import io.github.marcuscastelo.invtweaks.client.behavior.InvTweaksVanillaPlayerBehaviour;
import net.minecraft.screen.*;

import java.util.HashMap;

public class InvTweaksBehaviorRegistry {
    public static HashMap<Class<? extends ScreenHandler>, ScreenSpecification> screenBehaviorMap = new HashMap<>();

    public static ScreenSpecification register(ScreenSpecification screenSpecification) {
        if (screenBehaviorMap.containsKey(screenSpecification.getHandlerClass())) throw new IllegalArgumentException("Screen " + screenSpecification.getHandlerClass() + " is already registered");
        screenBehaviorMap.put(screenSpecification.getHandlerClass(), screenSpecification);
        return screenSpecification;
    }

    public static ScreenSpecification getScreenInfo(Class<? extends ScreenHandler> screenHandlerClass) {
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

    public static ScreenSpecification.Builder createScreenInfoBuilder(Class<? extends ScreenHandler> handlerClass) {
        return new ScreenSpecification.Builder(handlerClass);
    }

    public static ScreenSpecification.Builder createVanillaGenericScreenInfoBuilder(Class<? extends ScreenHandler> handlerClass) {
        return createScreenInfoBuilder(handlerClass).setBehavior(new InvTweaksVanillaGenericBehavior());
    }

    public static ScreenSpecification buildDefaultedVanillaGenericScreenInfo(Class<? extends ScreenHandler> handlerClass) {
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


        register(createScreenInfoBuilder(PlayerScreenHandler.class).setBehavior(new InvTweaksVanillaPlayerBehaviour()).setPlayerInvTotalSize(37).build());
        register(createScreenInfoBuilder(MerchantScreenHandler.class).setBehavior(new InvTweaksVanillaMerchantBehavior()).build());
    }

}
