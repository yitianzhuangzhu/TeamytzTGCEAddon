package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.item.ItemBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import techguns.tileentities.operation.ItemStackHandlerPlus;

import javax.annotation.Nonnull;

/**
 * 通用复制机的蓝图输入槽：只允许放入 ItemBlueprint 类物品
 * GUI 玩家取回蓝图走 extractWithoutCheck（绕过 allowExtractFromSlot），
 * 外部管道/漏斗受 ItemStackHandlerPlus.allowExtractFromSlot 限制无法抽走蓝图
 */
public class SlotBlueprintInput extends SlotItemHandler {

    protected final ItemStackHandlerPlus inventory;
    protected final int index;

    public SlotBlueprintInput(ItemStackHandlerPlus itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.inventory = itemHandler;
        this.index = index;
    }

    // ===== 限制只有蓝图物品才能放入此槽 =====
    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemBlueprint;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return !inventory.extractWithoutCheck(index, 1, true).isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        return inventory.extractWithoutCheck(index, amount, false);
    }
}
