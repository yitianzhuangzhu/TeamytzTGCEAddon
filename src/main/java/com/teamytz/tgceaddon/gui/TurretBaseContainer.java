package com.teamytz.tgceaddon.gui;

import com.teamytz.tgceaddon.item.ItemTurretCard;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import techguns.gui.containers.RedstoneTileContainer;
import techguns.gui.widgets.SlotItemHandlerOutput;
import techguns.gui.widgets.SlotMachineInput;
import techguns.gui.widgets.SlotTG;
import techguns.tileentities.operation.ItemStackHandlerPlus;

/**
 * 炮塔基座容器（套用科技枪 TurretContainer 布局）
 * 布局：3x3 弹药输入 + 3x3 弹药输出 + 1 卡片槽（原武器槽位置）
 * 红石/安全/能量字段同步由父类链 + 本类能量字段同步完成
 */
public class TurretBaseContainer extends RedstoneTileContainer {

    public static final int FIELD_SYNC_ID_POWER_STORED = FIELD_SYNC_ID_REDSTONE + 1;

    protected final TurretBaseTileEntMaster tile;
    protected int lastPowerStored = 0;

    public TurretBaseContainer(InventoryPlayer player, TurretBaseTileEntMaster ent) {
        super(player, ent);
        this.tile = ent;

        IItemHandler inventory = ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.SOUTH);

        if (inventory instanceof ItemStackHandlerPlus) {
            ItemStackHandlerPlus handler = (ItemStackHandlerPlus) inventory;

            // 3x3 弹药输入槽（与科技枪炮台一致：18,17 起）
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    this.addSlotToContainer(new SlotMachineInput(handler, TurretBaseTileEntMaster.SLOT_INPUT1 + (i * 3) + j, 18 + 18 * i, 17 + 18 * j) {
                        @Override
                        public String getSlotTexture() {
                            return SlotTG.AMMOSLOT_TEX.toString();
                        }
                    });
                }
            }

            // 3x3 弹药输出槽（116,17 起）
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    this.addSlotToContainer(new SlotItemHandlerOutput(handler, TurretBaseTileEntMaster.SLOT_OUTPUT1 + (i * 3) + j, 116 + 18 * i, 17 + 18 * j) {
                        @Override
                        public String getSlotTexture() {
                            return SlotTG.AMMOEMPTYSLOT_TEX.toString();
                        }
                    });
                }
            }

            // 卡片槽（原武器槽位置 85,19）
            this.addSlotToContainer(new SlotTurretCard(handler, TurretBaseTileEntMaster.SLOT_CARD, 85, 19));
        }

        this.addDefaultPlayerInventorySlots(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int j = 0; j < this.listeners.size(); ++j) {
            IContainerListener listener = this.listeners.get(j);
            if (this.lastPowerStored != this.tile.getEnergyStorage().getEnergyStored()) {
                listener.sendWindowProperty(this, FIELD_SYNC_ID_POWER_STORED, this.tile.getEnergyStorage().getEnergyStored());
            }
        }
        this.lastPowerStored = this.tile.getEnergyStorage().getEnergyStored();
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == FIELD_SYNC_ID_POWER_STORED) {
            this.tile.getEnergyStorage().setEnergyStored(data);
        } else {
            super.updateProgressBar(id, data);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotid) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(slotid);

        if (slot.getHasStack()) {
            ItemStack stack1 = slot.getStack();
            stack = stack1.copy();
            if (!stack.isEmpty()) {
                // 机器槽位 -> 玩家背包
                if (slotid <= TurretBaseTileEntMaster.SLOT_CARD) {
                    if (!this.mergeItemStack(stack1, TurretBaseTileEntMaster.SLOT_CARD + 1, TurretBaseTileEntMaster.SLOT_CARD + 1 + 36, false)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onSlotChange(stack1, stack);
                } else if (slotid > TurretBaseTileEntMaster.SLOT_CARD) {
                    // 玩家背包 -> 机器
                    if (stack.getItem() instanceof ItemTurretCard) {
                        // 卡片进卡片槽
                        if (!this.mergeItemStack(stack1, TurretBaseTileEntMaster.SLOT_CARD, TurretBaseTileEntMaster.SLOT_CARD + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                        slot.onSlotChange(stack1, stack);
                    } else {
                        // 其他物品进弹药输入槽
                        if (!this.mergeItemStack(stack1, TurretBaseTileEntMaster.SLOT_INPUT1, TurretBaseTileEntMaster.SLOT_INPUT1 + TurretBaseTileEntMaster.INPUTS_SIZE, false)) {
                            return ItemStack.EMPTY;
                        }
                        slot.onSlotChange(stack1, stack);
                    }
                }

                if (stack1.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }

                if (stack1.getCount() == stack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(player, stack1);
            }
        }
        return stack;
    }
}
