package io.github.marcuscastelo.invtweaks.registry;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.OperationExecutor;
import io.github.marcuscastelo.invtweaks.api.ScreenInventoriesSpecification;
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.client.behavior.*;
import net.minecraft.screen.*;

import java.util.HashMap;
import java.util.Optional;

public class InvTweaksBehaviorRegistry {
    public static HashMap<Class<? extends ScreenHandler>, ScreenSpecification> screenBehaviorMap = new HashMap<>();

    public static ScreenSpecification register(ScreenSpecification screenSpecification) {
        if (screenBehaviorMap.containsKey(screenSpecification.getHandlerClass())) throw new IllegalArgumentException("Screen " + screenSpecification.getHandlerClass() + " is already registered");
        screenBehaviorMap.put(screenSpecification.getHandlerClass(), screenSpecification);
        return screenSpecification;
    }

    public static ScreenSpecification getScreenSpecs(Class<? extends ScreenHandler> screenHandlerClass) {
        if (!isScreenSupported(screenHandlerClass)) throw new IllegalArgumentException("Screen " + screenHandlerClass + " is not supported");
        return screenBehaviorMap.get(screenHandlerClass);
    }

    public static void executeOperation(Class<? extends ScreenHandler> screenHandlerClass, InvTweaksOperationInfo operationInfo) throws IllegalArgumentException{
        if (!isScreenSupported(screenHandlerClass))
            throw new IllegalArgumentException("Screen "  + screenHandlerClass + " doesn't have a behavior");

        IInvTweaksBehavior behavior = screenBehaviorMap.get(screenHandlerClass).getInvTweaksBehavior();
        Optional<OperationExecutor> executor = operationInfo.type().asOperationExecutor(behavior);
        if (executor.isPresent()) {
            executor.get().execute(operationInfo);
        } else {
            System.out.println("Operation " + operationInfo.type() + " is not supported by " + behavior.getClass());
        }
    }

    public static boolean isScreenSupported(Class<? extends ScreenHandler> screenHandlerClass) {
        return screenBehaviorMap.containsKey(screenHandlerClass);
    }

    public static ScreenSpecification.Builder createSpecsBuilder(Class<? extends ScreenHandler> handlerClass) {
        return new ScreenSpecification.Builder(handlerClass);
    }

    public static ScreenSpecification.Builder createVanillaGenericSpecsBuilder(Class<? extends ScreenHandler> handlerClass) {
        return createSpecsBuilder(handlerClass).withBehavior(new InvTweaksVanillaGenericBehavior());
    }

    public static ScreenSpecification buildVanillaGeneric(Class<? extends ScreenHandler> handlerClass) {
        return createVanillaGenericSpecsBuilder(handlerClass).build();
    }

    static {
        //Default behaviour
        register(buildVanillaGeneric(GenericContainerScreenHandler.class));
        register(buildVanillaGeneric(ShulkerBoxScreenHandler.class));
        register(buildVanillaGeneric(BrewingStandScreenHandler.class));
        register(buildVanillaGeneric(BeaconScreenHandler.class));
        register(buildVanillaGeneric(HopperScreenHandler.class));
        register(buildVanillaGeneric(Generic3x3ContainerScreenHandler.class));
        register(buildVanillaGeneric(FurnaceScreenHandler.class));
        register(buildVanillaGeneric(SmokerScreenHandler.class));
        register(buildVanillaGeneric(BlastFurnaceScreenHandler.class));
        register(buildVanillaGeneric(EnchantmentScreenHandler.class));
        register(buildVanillaGeneric(AnvilScreenHandler.class));
        register(buildVanillaGeneric(CartographyTableScreenHandler.class));
        register(buildVanillaGeneric(LoomScreenHandler.class));
        register(buildVanillaGeneric(SmithingScreenHandler.class));
        register(buildVanillaGeneric(GrindstoneScreenHandler.class));
        register(buildVanillaGeneric(HorseScreenHandler.class));

        ScreenInventoriesSpecification playerScreenInvSpecs = new ScreenInventoriesSpecification(false, 27, 9);
        register(createSpecsBuilder(PlayerScreenHandler.class).withBehavior(new InvTweaksVanillaPlayerBehaviour()).withInventoriesSpecification(playerScreenInvSpecs).build());


        //Merchant behaviour
        register(createSpecsBuilder(MerchantScreenHandler.class).withBehavior(new InvTweaksVanillaMerchantBehavior()).build());


        //Crafting behaviour
        register(createSpecsBuilder(CraftingScreenHandler.class).withBehavior(new InvTweaksVanillaCraftingBehavior()).build());
    }

}
