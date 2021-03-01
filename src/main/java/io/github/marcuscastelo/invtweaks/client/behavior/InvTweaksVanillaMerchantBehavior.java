package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InventoryContainerBoundInfo;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

public class InvTweaksVanillaMerchantBehavior extends InvTweaksVanillaGenericBehavior {
    public static InvTweaksVanillaGenericBehavior INSTANCE = new InvTweaksVanillaMerchantBehavior();

    protected static final int VILLAGER_OUTPUT_SLOT = 2;

    @Override
    public void moveAll(InvTweaksOperationInfo operationInfo) {
        System.out.println(operationInfo.clickedSlot.id);
        if (operationInfo.clickedSlot.id != VILLAGER_OUTPUT_SLOT) { super.moveAll(operationInfo); return; }
        tradeAll(operationInfo);
    }

    @Override
    public void dropAll(InvTweaksOperationInfo operationInfo) {
        if (operationInfo.clickedSlot.id != VILLAGER_OUTPUT_SLOT) { super.moveAll(operationInfo); return; }
    }

    private void tradeAll(InvTweaksOperationInfo operationInfo) {
        if (hasExaustedOutput(operationInfo)) return; //No trade is being made

        ItemStack[] requirements = getPlayerOffer(operationInfo);


        ItemStack[] currentPlayerOffer, lackingItems;
        int[] slots;
        int tries = 0, sec_limit = 0;
        while (tries++ < 10 && sec_limit++ < 100) {
            currentPlayerOffer = getPlayerOffer(operationInfo);
            lackingItems = determineLackingItems(currentPlayerOffer, requirements);
            slots = findSupplySlots(operationInfo, lackingItems);
            prepareNewTrade(operationInfo, slots);
            if (!hasExaustedOutput(operationInfo)) tries = 0;

            while (!hasExaustedOutput(operationInfo) && sec_limit++ < 100)
                takeTrade(operationInfo);
        }

        moveToInventory(operationInfo.clickedInventoryBoundInfo.screenHandler, operationInfo.clickedSlot.id, operationInfo.otherInventoryBoundInfo, operationInfo.clickedSlot.getStack().getCount(), false);
    }

    private ItemStack subtract(ItemStack A, ItemStack B) {
        if (A.getItem() != B.getItem()) return ItemStack.EMPTY;
        return new ItemStack(A.getItem(), MathHelper.clamp(A.getCount() - B.getCount(), 0, A.getMaxCount()));
    }

    private ItemStack[] determineLackingItems(ItemStack[] playerOffer, ItemStack[] requirements) {
        if (requirements.length == 0) return new ItemStack[0];
        if (playerOffer.length == 0) return requirements.clone();

        if (playerOffer.length < requirements.length) return new ItemStack[] { subtract(requirements[0], playerOffer[0]), requirements[1] };
        if (playerOffer.length > requirements.length) {
            ItemStack[] isa = new ItemStack[playerOffer.length];
            Arrays.fill(isa, ItemStack.EMPTY);
            return isa;
        }

        return new ItemStack[] { subtract(requirements[0], playerOffer[0]), subtract(requirements[1], playerOffer[1]) };
    }

    private boolean hasExaustedOutput(InvTweaksOperationInfo operationInfo) {
        return operationInfo.clickedInventoryBoundInfo.screenHandler.slots.get(VILLAGER_OUTPUT_SLOT).getStack().isEmpty();
    }

    private ItemStack[] getPlayerOffer(InvTweaksOperationInfo operationInfo) {
        return new ItemStack[] {
                operationInfo.clickedInventoryBoundInfo.screenHandler.slots.get(0).getStack(),
                operationInfo.clickedInventoryBoundInfo.screenHandler.slots.get(1).getStack()
        };
    }

    private int[] findSupplySlots(InvTweaksOperationInfo operationInfo, ItemStack[] search) {
        int[] slots = new int[search.length];
        for (int j = 0; j < search.length;) {
            for (int i = operationInfo.otherInventoryBoundInfo.start; i < operationInfo.otherInventoryBoundInfo.end; i++) {
                if (operationInfo.otherInventoryBoundInfo.screenHandler.slots.get(i).getStack().getItem() == search[j].getItem()) {
                    slots[j++] = i;
                    break;
                }
                return new int[0];
            }
        }
        return slots;
    }

    private void prepareNewTrade(InvTweaksOperationInfo operationInfo, int[] fromSlots) {
        InventoryContainerBoundInfo cbi = operationInfo.clickedInventoryBoundInfo;
        InventoryContainerBoundInfo obi = operationInfo.otherInventoryBoundInfo;

        for (int i = 0; i < fromSlots.length; i++)
            moveToSlot(cbi.screenHandler, cbi.end, fromSlots[i], cbi.start+i, obi.screenHandler.slots.get(fromSlots[i]).getStack().getCount() , false);
    }

    private void takeTrade(InvTweaksOperationInfo operationInfo) {
        InventoryContainerBoundInfo cbi = operationInfo.clickedInventoryBoundInfo;
        InventoryContainerBoundInfo obi = operationInfo.otherInventoryBoundInfo;

        moveToInventory(cbi.screenHandler, VILLAGER_OUTPUT_SLOT, obi, cbi.screenHandler.slots.get(VILLAGER_OUTPUT_SLOT).getStack().getCount(), false);
    }
}
