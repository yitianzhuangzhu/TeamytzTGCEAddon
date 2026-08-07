package com.teamytz.tgceaddon.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import techguns.gui.containers.BasicMachineContainer;
import techguns.gui.widgets.SlotItemHandlerOutput;
import techguns.tileentities.operation.ItemStackHandlerPlus;
import com.teamytz.tgceaddon.tileentities.UniversalCopierTileEnt;

public class UniversalCopierContainer extends BasicMachineContainer {
    // ===== 字段同步 ID：用于服务端→客户端同步 selectedIndex =====
    public static final int FIELD_SYNC_ID_SELECTED_INDEX = FIELD_SYNC_ID_POWER_STORED + 1;

    // ===== 槽位屏幕坐标（必须和 Gui 中常量一致）=====
    public static final int SLOT_BLUEPRINT_X = 120;
    public static final int SLOT_BLUEPRINT_Y = 15;
    public static final int SLOT_OUTPUT_X = 120;
    public static final int SLOT_OUTPUT_Y = 60;

    protected final UniversalCopierTileEnt tile;
    private int lastSelectedIndex = 0;

    // ===== 构造：添加蓝图槽 + 输出槽 + 玩家背包 =====
    public UniversalCopierContainer(InventoryPlayer player, UniversalCopierTileEnt ent) {
        super(player, ent);
        this.tile = ent;

        IItemHandler inventory = ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.SOUTH);

        if (inventory instanceof ItemStackHandlerPlus) {
            this.addSlotToContainer(new SlotBlueprintInput(inventory, UniversalCopierTileEnt.SLOT_BLUEPRINT, SLOT_BLUEPRINT_X, SLOT_BLUEPRINT_Y));
            this.addSlotToContainer(new SlotItemHandlerOutput(inventory, UniversalCopierTileEnt.SLOT_OUTPUT, SLOT_OUTPUT_X, SLOT_OUTPUT_Y));
        }
        this.playerInv(player, 8, 116);
    }

    // ===== 数据同步：selectedIndex 变化时推送给客户端 Container =====
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            if (this.lastSelectedIndex != this.tile.selectedIndex) {
                listener.sendWindowProperty(this, FIELD_SYNC_ID_SELECTED_INDEX, this.tile.selectedIndex);
            }
        }
        this.lastSelectedIndex = this.tile.selectedIndex;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == FIELD_SYNC_ID_SELECTED_INDEX) {
            this.tile.selectedIndex = data;
        } else {
            super.updateProgressBar(id, data);
        }
    }

    // ===== Shift 点击搬运：机器↔玩家背包，只允许蓝图进蓝图槽 =====
    @Override
    public ItemStack transferStackInSlot(EntityPlayer ply, int id) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(id);

        if (slot.getHasStack()) {
            ItemStack stack1 = slot.getStack();
            stack = stack1.copy();
            if (!stack.isEmpty()) {
                // 从机器 -> 玩家背包
                if (id <= UniversalCopierTileEnt.SLOT_OUTPUT) {
                    if (!this.mergeItemStack(stack1, UniversalCopierTileEnt.SLOT_OUTPUT + 1, UniversalCopierTileEnt.SLOT_OUTPUT + 37, false)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(stack1, stack);
                }
                // 从玩家背包 -> 蓝图槽
                else if (id < UniversalCopierTileEnt.SLOT_OUTPUT + 37) {
                    if (!this.mergeItemStack(stack1, UniversalCopierTileEnt.SLOT_BLUEPRINT, UniversalCopierTileEnt.SLOT_BLUEPRINT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(stack1, stack);
                }

                if (stack1.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }

                if (stack1.getCount() == stack.getCount()) {
                    return ItemStack.EMPTY;
                }
                slot.onTake(ply, stack1);
            }
        }
        return stack;
    }
}
