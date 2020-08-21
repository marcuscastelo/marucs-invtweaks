package io.github.marcuscastelo.invtweaks.registry;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.client.behavior.IInvTweaksBehavior;
import io.github.marcuscastelo.invtweaks.client.behavior.InvTweaksVanillaBehavior;
import net.minecraft.screen.*;

import java.util.HashMap;

public class InvTweaksBehaviorRegistry {
    public static HashMap<Class<? extends ScreenHandler>, IInvTweaksBehavior> screenBehaviorMap = new HashMap<>();

    public static IInvTweaksBehavior register(Class<? extends ScreenHandler> screenHandlerClass, IInvTweaksBehavior screenBehavior) {
        if (screenBehaviorMap.containsKey(screenHandlerClass)) throw new IllegalArgumentException("Screen " + screenHandlerClass + " is already registered");
        screenBehaviorMap.put(screenHandlerClass, screenBehavior);
        return screenBehavior;
    }

    public static void executeOperation(Class<? extends ScreenHandler> screenHandlerClass, InvTweaksOperationInfo operationInfo) throws IllegalArgumentException{
        if (!screenBehaviorMap.containsKey(screenHandlerClass)) throw new IllegalArgumentException("Screen "  + screenHandlerClass + " doesn't have a behavior");
        operationInfo.type.asOperationExecutor(screenBehaviorMap.get(screenHandlerClass)).execute(operationInfo);
    }

    static {
        register(GenericContainerScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
        register(ShulkerBoxScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
        register(CraftingScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
        register(BrewingStandScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
        register(BeaconScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
        register(HopperScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
        register(Generic3x3ContainerScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);

        register(PlayerScreenHandler.class, InvTweaksVanillaBehavior.INSTANCE);
    }

}
