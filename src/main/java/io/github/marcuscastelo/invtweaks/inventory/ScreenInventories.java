package io.github.marcuscastelo.invtweaks.inventory;

import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ScreenInventories {
    public final ScreenInventory playerCombinedSI;
    public final ScreenInventory playerMainSI;
    public final ScreenInventory playerHotbarSI;

    //TODO: support more than one external inventory
    public final Optional<ScreenInventory> externalSI;

    public ScreenInventories(@NotNull ScreenHandler handler) {
        ScreenSpecification screenSpecification = InvTweaksBehaviorRegistry.getScreenSpecs(handler.getClass());

        int screenSlotCount = handler.slots.size();

        //FIXME: this is a hack to get the player inventory size
        int playerMainInvSize = screenSpecification.getInventoriesSpecification().playerMainInvSize;
        int playerHotbarSize = screenSpecification.getInventoriesSpecification().playerHotbarSize;
        int playerCombinedInvSize = playerHotbarSize + playerMainInvSize;

        int externalInventorySize = screenSlotCount - playerCombinedInvSize;

        //TODO: make this generic instead of hardcoded:
        if (handler.getClass().equals(PlayerScreenHandler.class)) {
            externalInventorySize = 9;
        }

        if (externalInventorySize > 0)
            externalSI = Optional.of(new ScreenInventory(handler, 0, externalInventorySize - 1));
        else
            externalSI = Optional.empty();

        playerCombinedSI = new ScreenInventory(handler, externalInventorySize, externalInventorySize + playerCombinedInvSize - 1);
        playerMainSI = new ScreenInventory(handler, externalInventorySize, externalInventorySize + playerMainInvSize - 1);
        playerHotbarSI = new ScreenInventory(handler,externalInventorySize + playerMainInvSize, externalInventorySize + playerMainInvSize + playerHotbarSize - 1);
    }

    public ScreenInventory getClickedInventory(int slotId) {
        ScreenInventory clickedInventory;
        if (externalSI.isPresent() && slotId <= externalSI.get().end()) {
            clickedInventory = externalSI.get();
        }
        else if (slotId <= playerMainSI.end()) {
            clickedInventory = playerMainSI;
        }
        else if (slotId <= playerHotbarSI.end()) {
            clickedInventory = playerHotbarSI;
        }
        else {
            clickedInventory = externalSI.orElse(null);
        }

        return clickedInventory;
    }

    public ScreenInventory getOppositeInventory(ScreenInventory clickedSI, boolean allowCombined) {
        boolean isPlayerScreen = clickedSI.screenHandler() instanceof PlayerScreenHandler;

        if (clickedSI == playerMainSI) return isPlayerScreen ? playerHotbarSI : externalSI.orElse(playerHotbarSI);
        else if (clickedSI == playerHotbarSI) return isPlayerScreen ? playerMainSI : externalSI.orElse(playerMainSI);
        else if (clickedSI == externalSI.orElse(null)) return allowCombined ? playerCombinedSI : playerMainSI;
        else {
            throw new IllegalArgumentException("Unknown inventory");
        }
    }

    public ScreenInventory getInventoryUpwards(ScreenInventory initialSI, boolean allowCombined) {
        if (initialSI == playerMainSI) return externalSI.orElse(playerHotbarSI);
        else if (initialSI == playerHotbarSI) return playerMainSI;
        else if (initialSI == externalSI.orElse(null)) return allowCombined ? playerCombinedSI : playerHotbarSI;
        else {
            throw new IllegalArgumentException("Unknown inventory");
        }
    }

    public ScreenInventory getInventoryDownwards(ScreenInventory initialSI, boolean allowCombined) {
        if (initialSI == playerMainSI) return playerHotbarSI;
        else if (initialSI == playerHotbarSI) return externalSI.orElse(null);
        else if (initialSI == externalSI.orElse(null)) return allowCombined ? playerCombinedSI : playerMainSI;
        else {
            throw new IllegalArgumentException("Unknown inventory");
        }
    }
}
