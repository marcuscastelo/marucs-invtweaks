package com.marcuscastelo.invtweaks.behavior

class InvTweaksVanillaMerchantBehavior : InvTweaksVanillaGenericBehavior() { //    public static InvTweaksVanillaGenericBehavior INSTANCE = new InvTweaksVanillaMerchantBehavior();
    //
    //    protected static final int VILLAGER_OUTPUT_SLOT = 2;
    //
    //    @Override
    //    public OperationResult moveAll(InvTweaksOperationInfo operationInfo) {
    //        System.out.println(operationInfo.clickedSlot().id);
    //        if (operationInfo.clickedSlot().id != VILLAGER_OUTPUT_SLOT) {
    //            return super.moveAll(operationInfo);
    //        }
    //        tradeAll(operationInfo);
    //        return new OperationResult(true);
    //    }
    //
    //    @Override
    //    public OperationResult dropAll(InvTweaksOperationInfo operationInfo) {
    //        if (operationInfo.clickedSlot().id != VILLAGER_OUTPUT_SLOT) {
    //            return super.moveAll(operationInfo);
    //        }
    //        return new OperationResult(true);
    //    }
    //
    //    private void tradeAll(InvTweaksOperationInfo operationInfo) {
    //        if (hasExaustedOutput(operationInfo)) return; //No trade is being made
    //
    //        ItemStack[] requirements = getPlayerOffer(operationInfo);
    //
    //
    //        ItemStack[] currentPlayerOffer, lackingItems;
    //        int[] slots;
    //        int tries = 0, sec_limit = 0;
    //        while (tries++ < 10 && sec_limit++ < 100) {
    //            currentPlayerOffer = getPlayerOffer(operationInfo);
    //            lackingItems = determineLackingItems(currentPlayerOffer, requirements);
    //            slots = findSupplySlots(operationInfo, lackingItems);
    //            prepareNewTrade(operationInfo, slots);
    //            if (!hasExaustedOutput(operationInfo)) tries = 0;
    //
    //            while (!hasExaustedOutput(operationInfo) && sec_limit++ < 100)
    //                takeTrade(operationInfo);
    //        }
    //
    //        moveToInventory(operationInfo.clickedSI().screenHandler(), operationInfo.clickedSlot().id, operationInfo.targetSI(), operationInfo.clickedSlot().getStack().getCount(), false);
    //    }
    //
    //    private ItemStack subtract(ItemStack A, ItemStack B) {
    //        if (A.getItem() != B.getItem()) return ItemStack.EMPTY;
    //        return new ItemStack(A.getItem(), MathHelper.clamp(A.getCount() - B.getCount(), 0, A.getMaxCount()));
    //    }
    //
    //    private ItemStack[] determineLackingItems(ItemStack[] playerOffer, ItemStack[] requirements) {
    //        if (requirements.length == 0) return new ItemStack[0];
    //        if (playerOffer.length == 0) return requirements.clone();
    //
    //        if (playerOffer.length < requirements.length) return new ItemStack[] { subtract(requirements[0], playerOffer[0]), requirements[1] };
    //        if (playerOffer.length > requirements.length) {
    //            ItemStack[] isa = new ItemStack[playerOffer.length];
    //            Arrays.fill(isa, ItemStack.EMPTY);
    //            return isa;
    //        }
    //
    //        return new ItemStack[] { subtract(requirements[0], playerOffer[0]), subtract(requirements[1], playerOffer[1]) };
    //    }
    //
    //    private boolean hasExaustedOutput(InvTweaksOperationInfo operationInfo) {
    //        return operationInfo.clickedSI().screenHandler().slots.get(VILLAGER_OUTPUT_SLOT).getStack().isEmpty();
    //    }
    //
    //    private ItemStack[] getPlayerOffer(InvTweaksOperationInfo operationInfo) {
    //        return new ItemStack[] {
    //                operationInfo.clickedSI().screenHandler().slots.get(0).getStack(),
    //                operationInfo.clickedSI().screenHandler().slots.get(1).getStack()
    //        };
    //    }
    //
    //    private int[] findSupplySlots(InvTweaksOperationInfo operationInfo, ItemStack[] search) {
    //        int[] slots = new int[search.length];
    //        for (int j = 0; j < search.length;) {
    //            for (int i = operationInfo.targetSI().start(); i < operationInfo.targetSI().end(); i++) {
    //                if (operationInfo.targetSI().screenHandler().slots.get(i).getStack().getItem() == search[j].getItem()) {
    //                    slots[j++] = i;
    //                    break;
    //                }
    //                return new int[0];
    //            }
    //        }
    //        return slots;
    //    }
    //
    //    private void prepareNewTrade(InvTweaksOperationInfo operationInfo, int[] fromSlots) {
    //        ScreenInventory cbi = operationInfo.clickedSI();
    //        ScreenInventory obi = operationInfo.targetSI();
    //
    //        for (int i = 0; i < fromSlots.length; i++)
    //            moveToSlot(cbi.screenHandler(), cbi.end(), fromSlots[i], cbi.start() +i, obi.screenHandler().slots.get(fromSlots[i]).getStack().getCount() , false);
    //    }
    //
    //    private void takeTrade(InvTweaksOperationInfo operationInfo) {
    //        ScreenInventory cbi = operationInfo.clickedSI();
    //        ScreenInventory obi = operationInfo.targetSI();
    //
    //        moveToInventory(cbi.screenHandler(), VILLAGER_OUTPUT_SLOT, obi, cbi.screenHandler().slots.get(VILLAGER_OUTPUT_SLOT).getStack().getCount(), false);
    //    }
}