package io.github.marcuscastelo.invtweaks.inventory;

import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer;

public class ScreenInventories {
    public final ScreenInventory playerCombinedSI;
    public final ScreenInventory playerMainSI;
    public final ScreenInventory playerHotbarSI;

    //TODO: support more than one external inventory
    public final Optional<ScreenInventory> storageSI;

    public final Optional<ScreenInventory> craftingSI;

    public final Optional<ScreenInventory> craftingResultSI;

    public final Optional<ScreenInventory> armorSI;

    private Stream<ScreenInventory> extraInvs() {
        Stream<Optional<ScreenInventory>> optionalStream = Arrays.stream(new Optional[]{storageSI, craftingSI, craftingResultSI, armorSI});
        return optionalStream.filter(Optional::isPresent).map(Optional::get);
    }

    private boolean isExtraInv(ScreenInventory si) {
        return extraInvs().anyMatch(extraInv -> extraInv == si);
    }

    public ScreenInventories(@NotNull ScreenHandler handler) {
        ScreenSpecification screenSpecification = InvTweaksBehaviorRegistry.getScreenSpecs(handler.getClass());

        int screenSlotCount = handler.slots.size();

        //FIXME: this is a hack to get the player inventory size
        int playerMainInvSize = screenSpecification.getInventoriesSpecification().playerMainInvSize;
        int playerHotbarSize = screenSpecification.getInventoriesSpecification().playerHotbarSize;
        int playerCombinedInvSize = playerHotbarSize + playerMainInvSize;

        int storageInventorySize = screenSlotCount - playerCombinedInvSize;

        //TODO: make this generic instead of hardcoded:
        if (handler.getClass().equals(PlayerScreenHandler.class)) {
            storageInventorySize = 0;
            craftingSI = Optional.of(new ScreenInventory(handler, 1, 4));
            craftingResultSI = Optional.of(new ScreenInventory(handler, 0, 0));
            armorSI = Optional.of(new ScreenInventory(handler, 5, 8));
        } else if (handler.getClass().equals(CraftingScreenHandler.class)) {
            storageInventorySize = 0;
            craftingSI = Optional.of(new ScreenInventory(handler, 1, 9));
            craftingResultSI = Optional.of(new ScreenInventory(handler, 0, 0));
            armorSI = Optional.empty();
        } else {
            craftingSI = Optional.empty();
            craftingResultSI = Optional.empty();
            armorSI = Optional.empty();
        }

        if (storageInventorySize > 0)
            storageSI = Optional.of(new ScreenInventory(handler, 0, storageInventorySize - 1));
        else
            storageSI = Optional.empty();

        Stream<ScreenInventory> extraInvsStream = extraInvs();
        int extraInvsSize = extraInvsStream.map(ScreenInventory::getSize).reduce(Integer::sum).orElse(0);

        playerCombinedSI = new ScreenInventory(handler, extraInvsSize, extraInvsSize + playerCombinedInvSize - 1);
        playerMainSI = new ScreenInventory(handler, extraInvsSize, extraInvsSize + playerMainInvSize - 1);
        playerHotbarSI = new ScreenInventory(handler,extraInvsSize + playerMainInvSize, extraInvsSize + playerMainInvSize + playerHotbarSize - 1);
    }

    public ScreenInventory getClickedInventory(int slotId) {
        ScreenInventory clickedInventory;

        List<ScreenInventory> results = extraInvs().filter(screenInventory -> slotId >= screenInventory.start() && slotId <= screenInventory.end()).toList();
        long resultCount = results.size();
        if (resultCount > 1) {
            warnPlayer("Please tell Marucs there is a bug on the code!!");
            warnPlayer("Couldn't determine which inventory was clicked (more than one have slotId = " + slotId + ")");
            warnPlayer("Results: " + results.stream().map(ScreenInventory::toString).reduce((a, s) -> a + ", " + s));
        }

        if (resultCount > 0) {
            clickedInventory = results.get(0);
        }
        else if (slotId <= playerMainSI.end()) {
            clickedInventory = playerMainSI;
        }
        else if (slotId <= playerHotbarSI.end()) {
            clickedInventory = playerHotbarSI;
        }
        else {
            warnPlayer("Hmm... This is a bug! Couldn't determine which inventory has slotId = " + slotId);
            clickedInventory = playerHotbarSI; //Just to avoid crash
        }

        return clickedInventory;
    }

    public ScreenInventory getOppositeInventory(ScreenInventory initialSI, boolean allowCombined) {
        boolean playerScreen = isPlayerScreen(initialSI);
        ScreenInventory main = allowCombined ? playerCombinedSI : playerMainSI;
        ScreenInventory hotbar = allowCombined ? playerCombinedSI : playerHotbarSI;


        if (initialSI == playerMainSI)
            return playerScreen ? playerHotbarSI : extraInvs().findAny().orElse(playerHotbarSI);
        else if (initialSI == playerHotbarSI)
            return playerScreen ? playerMainSI : extraInvs().findAny().orElse(playerMainSI);
        else if (isExtraInv(initialSI))
            return main;
        else {
            warnPlayer("getOppositeInventory() - Unkown ScreenInventory: " + initialSI);
            return initialSI;
        }

    }

    public ScreenInventory getInventoryUpwards(ScreenInventory initialSI, boolean allowCombined) {
        boolean playerScreen = isPlayerScreen(initialSI);
        ScreenInventory main = allowCombined ? playerCombinedSI : playerMainSI;
        ScreenInventory hotbar = allowCombined ? playerCombinedSI : playerHotbarSI;

        if (initialSI == playerMainSI)
            return extraInvs().findAny().orElse(playerHotbarSI);
        else if (initialSI == playerHotbarSI)
            return playerMainSI;
        else if (isExtraInv(initialSI))
            return hotbar;
        else {
            warnPlayer("getInventoryUpwards() - Unkown ScreenInventory: " + initialSI);
            return initialSI;

        }
    }

    public ScreenInventory getInventoryDownwards(ScreenInventory initialSI, boolean allowCombined) {
        boolean playerScreen = isPlayerScreen(initialSI);
        ScreenInventory main = allowCombined ? playerCombinedSI : playerMainSI;
        ScreenInventory hotbar = allowCombined ? playerCombinedSI : playerHotbarSI;

        if (initialSI == playerMainSI)
            return playerHotbarSI;
        else if (initialSI == playerHotbarSI)
            return extraInvs().findAny().orElse(playerMainSI);
        else if (isExtraInv(initialSI))
            return main;
        else {
            warnPlayer("getInventoryDownwards() - Unkown ScreenInventory: " + initialSI);
            return initialSI;
        }
    }

    private static boolean isPlayerScreen(ScreenInventory si) { return si.screenHandler() instanceof PlayerScreenHandler; }
}
