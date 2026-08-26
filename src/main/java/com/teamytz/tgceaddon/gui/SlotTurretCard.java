package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.item.ItemTurretCard;
import net.minecraft.item.ItemStack;
import techguns.gui.widgets.SlotMachineInput;
import techguns.gui.widgets.SlotTG;
import techguns.tileentities.operation.ItemStackHandlerPlus;

/**
 * 炮塔卡片槽：只允许放入 ItemTurretCard，替代科技枪炮台 GUI 中的武器槽
 */
public class SlotTurretCard extends SlotMachineInput {

    public SlotTurretCard(ItemStackHandlerPlus itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemTurretCard;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public String getSlotTexture() {
        return SlotTG.TURRETGUNSLOT_TEX.toString();
    }
}
