package io.github.marcuscastelo.invtweaks.inventory;

import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import io.github.marcuscastelo.invtweaks.util.ChatUtils;
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

    public final Optional<ScreenInventory> craftingSI;

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
            craftingSI = Optional.of(new ScreenInventory(handler, 1, 4));
        } else
            craftingSI = Optional.empty();

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

    public ScreenInventory getOppositeInventory(ScreenInventory initialSI, boolean allowCombined) {
        boolean playerScreen = isPlayerScreen(initialSI);

        if (initialSI == playerMainSI)
            return playerScreen ? playerHotbarSI : externalSI.orElse(playerHotbarSI);
        else if (initialSI == playerHotbarSI)
            return playerScreen ? playerMainSI : externalSI.orElse(playerMainSI);
        else if (initialSI == externalSI.orElse(null))
            return allowCombined ? playerCombinedSI : playerMainSI;
        else {
            ChatUtils.warnPlayer("getOppositeInventory() - Unkown ScreenInventory: " + initialSI);
            return initialSI;
        }

    }

    public ScreenInventory getInventoryUpwards(ScreenInventory initialSI, boolean allowCombined) {
        boolean playerScreen = isPlayerScreen(initialSI);
        ChatUtils.warnPlayer("getInventoryUpwards");
        ChatUtils.warnPlayer("playerScreen: " + playerScreen);
        ChatUtils.warnPlayer("initialSI: " + initialSI);
        ChatUtils.warnPlayer("craftingSI: " + craftingSI);
        ChatUtils.warnPlayer("playerMainSI: " + playerMainSI);
        ChatUtils.warnPlayer("playerHotbarSI: " + playerHotbarSI);
        if (initialSI == playerMainSI)
            return craftingSI.orElseThrow();
//            return playerScreen ? craftingSI.orElse(playerHotbarSI) : externalSI.orElse(playerHotbarSI);
        else if (initialSI == playerHotbarSI)
            return playerMainSI;
        else if (initialSI == externalSI.orElse(null))
            return allowCombined ? playerCombinedSI : playerHotbarSI;
        else {
            ChatUtils.warnPlayer("getInventoryUpwards() - Unkown ScreenInventory: " + initialSI);
            return initialSI;

        }
    }

    public ScreenInventory getInventoryDownwards(ScreenInventory initialSI, boolean allowCombined) {
        if (initialSI == playerMainSI)
            return playerHotbarSI;
        else if (initialSI == playerHotbarSI)
            return externalSI.orElse(null);
        else if (initialSI == externalSI.orElse(null))
            return allowCombined ? playerCombinedSI : playerMainSI;
        else {
            ChatUtils.warnPlayer("getInventoryDownwards() - Unkown ScreenInventory: " + initialSI);
            return initialSI;
        }
    }

    private static boolean isPlayerScreen(ScreenInventory si) { return si.screenHandler() instanceof PlayerScreenHandler; }
}
