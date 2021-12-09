package io.github.marcuscastelo.invtweaks.inventory;

import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.screen.ScreenHandler;

import java.util.Optional;

public class ScreenInventories {
    public final ScreenInventory playerInventoryWithHotbar;
    public final ScreenInventory playerInventoryNoHotbar;
    public final ScreenInventory playerHotbar;

    //TODO: support more than one external inventory
    public final Optional<ScreenInventory> externalInventory;

    public ScreenInventories(ScreenHandler handler) {
        ScreenSpecification screenSpecification = InvTweaksBehaviorRegistry.getScreenSpecs(handler.getClass());

        int totalSlotCount = handler.slots.size();

        int playerMainInvSize = 27;// screenSpecification.getPlayerMainInvSize();
        int playerHotbarInvSize = 9;//screenSpecification.getPlayerHotbarSize();
        int playerInvSize = playerHotbarInvSize + playerMainInvSize;

        int externalInventorySize = totalSlotCount - playerInvSize;

        if (externalInventorySize > 0)
            externalInventory = Optional.of(new ScreenInventory(handler, 0, externalInventorySize - 1));
        else
            externalInventory = Optional.empty();

        playerInventoryWithHotbar = new ScreenInventory(handler, externalInventorySize, externalInventorySize + playerInvSize - 1);
        playerInventoryNoHotbar = new ScreenInventory(handler, externalInventorySize, externalInventorySize + playerMainInvSize - 1);
        playerHotbar = new ScreenInventory(handler, externalInventorySize + playerMainInvSize, externalInventorySize + playerMainInvSize + playerHotbarInvSize - 1);
    }

    public ScreenInventory getClickedInventory(int slot) {
        int playerInvStart = 0;
        if (externalInventory.isPresent()) playerInvStart += externalInventory.get().getSize();

        if (slot >= playerInvStart) return playerInventoryNoHotbar;
        else return externalInventory.orElseThrow();
    }
}
