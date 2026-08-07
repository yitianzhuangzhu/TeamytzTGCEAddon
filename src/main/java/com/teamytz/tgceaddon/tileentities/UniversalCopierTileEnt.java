package com.teamytz.tgceaddon.tileentities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModAmmoTypes;
import com.teamytz.tgceaddon.item.ItemBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import techguns.gui.ButtonConstants;
import techguns.tileentities.BasicMachineTileEnt;
import techguns.tileentities.operation.ItemStackHandlerPlus;
import techguns.tileentities.operation.MachineOperation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用复制机：使用 RF 能量来生成蓝图所指定的物品。
 * - 蓝图槽放入蓝图后，点击"注册"按钮将蓝图中的目标物品存储到机器内部
 * - 蓝图会被消耗，信息存储在 TileEntity 的 NBT 中
 * - 使用左右按钮切换已注册的物品列表
 * - 消耗 RF 能量生产物品
 */
public class UniversalCopierTileEnt extends BasicMachineTileEnt {

    // ===== 常量：槽位索引、按钮ID、能耗参数 =====
    public static final int SLOT_BLUEPRINT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int BUTTON_ID_NEXT = ButtonConstants.BUTTON_ID_REDSTONE + 1;
    public static final int BUTTON_ID_PREV = ButtonConstants.BUTTON_ID_REDSTONE + 2;
    public static final int BUTTON_ID_REGISTER = ButtonConstants.BUTTON_ID_REDSTONE + 3;
    public static final int POWER_PER_TICK = 120;
    public static final int CRAFT_TIME = 100;
    // 爆弹生产：100RF/tick × 100tick = 10000RF，每次产出10发
    public static final int BOLT_POWER_PER_TICK = 100;
    public static final int BOLT_OUTPUT_COUNT = 10;

    // ===== NBT 标签键名 =====
    private static final String TAG_REGISTERED_TARGETS = "RegisteredTargets";
    private static final String TAG_REGISTERED_NAMES = "RegisteredNames";
    private static final String TAG_SELECTED_INDEX = "SelectedIndex";

    // ===== 已注册目标列表 + 当前选中索引（服务端权威，客户端通过 Container 同步）=====
    /** 已注册的目标物品列表 */
    public List<ItemStack> registeredTargets = new ArrayList<>();
    /** 已注册的目标名称列表 */
    public List<String> registeredNames = new ArrayList<>();
    /** 当前选中的目标索引 */
    public int selectedIndex = 0;

    // ===== 构造：2 槽位 + 5万 RF 容量，自定义 ItemStackHandlerPlus 限制输入输出 =====
    public UniversalCopierTileEnt() {
        super(2, false, 50000);
        this.inventory = new ItemStackHandlerPlus(2) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setContentsChanged(true);
            }

            @Override
            protected boolean allowItemInSlot(int slot, ItemStack stack) {
                return slot == SLOT_BLUEPRINT && stack.getItem() instanceof ItemBlueprint;
            }

            @Override
            protected boolean allowExtractFromSlot(int slot, int amount) {
                return slot == SLOT_BLUEPRINT || slot == SLOT_OUTPUT;
            }
        };
    }

    // ===== 主循环：消耗 RF 推进进度，完成后产出物品并检查下一轮 =====
    @Override
    public void update() {
        if (this.isRedstoneEnabled() && this.enabled()) {
            if (this.currentOperation != null) {
                if (this.consumePower(this.getNeededPower() * this.currentOperation.getStackMultiplier())) {
                    this.progress++;
                    playAmbientSound();
                    if (progress >= totaltime) {
                        if (!this.world.isRemote) {
                            this.finishedOperation();
                        }
                        this.progress = 0;
                        this.totaltime = 0;
                        this.currentOperation = null;
                        if (!this.world.isRemote) {
                            checkAndStartOperation();
                            this.needUpdate();
                        }
                    }
                }
            } else {
                if (!this.world.isRemote) {
                    checkAndStartOperation();
                }
            }
        }
    }

    // ===== NBT 持久化：客户端数据读写（selectedIndex + 已注册列表）=====
    @Override
    public void readClientDataFromNBT(NBTTagCompound tags) {
        super.readClientDataFromNBT(tags);
        this.selectedIndex = tags.getInteger(TAG_SELECTED_INDEX);
        loadRegisteredTargets(tags);
    }

    @Override
    public void writeClientDataToNBT(NBTTagCompound tags) {
        super.writeClientDataToNBT(tags);
        tags.setInteger(TAG_SELECTED_INDEX, this.selectedIndex);
        saveRegisteredTargets(tags);
    }

    // ===== 已注册列表的 NBT 序列化/反序列化 =====
    private void loadRegisteredTargets(NBTTagCompound tags) {
        registeredTargets.clear();
        registeredNames.clear();
        if (tags.hasKey(TAG_REGISTERED_TARGETS)) {
            NBTTagList targets = tags.getTagList(TAG_REGISTERED_TARGETS, 10);
            NBTTagList names = tags.getTagList(TAG_REGISTERED_NAMES, 8);
            for (int i = 0; i < targets.tagCount(); i++) {
                registeredTargets.add(new ItemStack(targets.getCompoundTagAt(i)));
            }
            for (int i = 0; i < names.tagCount(); i++) {
                registeredNames.add(names.getStringTagAt(i));
            }
        }
    }

    private void saveRegisteredTargets(NBTTagCompound tags) {
        NBTTagList targets = new NBTTagList();
        NBTTagList names = new NBTTagList();
        for (ItemStack target : registeredTargets) {
            targets.appendTag(target.writeToNBT(new NBTTagCompound()));
        }
        for (String name : registeredNames) {
            names.appendTag(new NBTTagString(name));
        }
        tags.setTag(TAG_REGISTERED_TARGETS, targets);
        tags.setTag(TAG_REGISTERED_NAMES, names);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("container.tgceaddon.universal_copier");
    }

    // ===== 按钮处理：REGISTER 消耗蓝图并注册目标物品；NEXT/PREV 切换选中 =====
    @Override
    public void buttonClicked(int id, EntityPlayer ply, String data) {
        if (id < BUTTON_ID_NEXT) {
            super.buttonClicked(id, ply, data);
        } else if (id == BUTTON_ID_REGISTER) {
            // 注册蓝图
            if (this.isUseableByPlayer(ply)) {
                ItemStack blueprint = inventory.getStackInSlot(SLOT_BLUEPRINT);
                if (!blueprint.isEmpty() && blueprint.getItem() instanceof ItemBlueprint) {
                    int count = ItemBlueprint.getTargetCount(blueprint);
                    if (count > 0) {
                        for (int i = 0; i < count; i++) {
                            ItemStack target = ItemBlueprint.getTargetAt(blueprint, i);
                            String name = ItemBlueprint.getTargetNameAt(blueprint, i);
                            if (target != null && !target.isEmpty()) {
                                registeredTargets.add(target);
                                registeredNames.add(name != null ? name : target.getDisplayName());
                            }
                        }
                        // 消耗蓝图
                        inventory.setStackInSlot(SLOT_BLUEPRINT, ItemStack.EMPTY);
                        if (selectedIndex >= registeredTargets.size()) {
                            selectedIndex = 0;
                        }
                        if (!this.world.isRemote) {
                            this.needUpdate();
                        }
                        ply.sendStatusMessage(
                            new TextComponentString("已注册 " + count + " 个物品蓝图"), true);
                    }
                }
            }
        } else {
            // 切换按钮
            if (this.isUseableByPlayer(ply) && !registeredTargets.isEmpty()) {
                int count = registeredTargets.size();
                switch (id) {
                    case BUTTON_ID_NEXT:
                        selectedIndex = (selectedIndex + 1) % count;
                        break;
                    case BUTTON_ID_PREV:
                        selectedIndex = (selectedIndex - 1 + count) % count;
                        break;
                }
                if (!this.world.isRemote) {
                    this.needUpdate();
                }
            }
        }
    }

    // ===== 查询接口：蓝图/产物/数量/名称/是否可注册 =====
    /**
     * 获取蓝图槽中的蓝图（用于显示）
     */
    public ItemStack getCurrentBlueprint() {
        return this.inventory.getStackInSlot(SLOT_BLUEPRINT);
    }

    /**
     * 获取当前选中的目标物品
     */
    @Nullable
    public ItemStack getCurrentOutput() {
        if (registeredTargets.isEmpty()) return null;
        int idx = Math.min(Math.max(selectedIndex, 0), registeredTargets.size() - 1);
        return registeredTargets.get(idx);
    }

    /**
     * 获取已注册的物品数量
     */
    public int getUnlockedCount() {
        return registeredTargets.size();
    }

    /**
     * 获取指定索引的名称
     */
    @Nullable
    public String getRegisteredNameAt(int index) {
        if (index < 0 || index >= registeredNames.size()) return null;
        return registeredNames.get(index);
    }

    /**
     * 检查是否可以注册蓝图
     */
    public boolean canRegisterBlueprint() {
        ItemStack blueprint = inventory.getStackInSlot(SLOT_BLUEPRINT);
        return !blueprint.isEmpty() && blueprint.getItem() instanceof ItemBlueprint
            && ItemBlueprint.getTargetCount(blueprint) > 0;
    }

    // ===== 机器操作：开启生产任务、检查输出槽、完成时放入产物 =====
    @Override
    protected int getNeededPower() {
        if (this.currentOperation != null) {
            ItemStack output = this.currentOperation.getItemOutput0();
            if (isBoltAmmo(output)) {
                return BOLT_POWER_PER_TICK;
            }
        }
        return POWER_PER_TICK;
    }

    /**
     * 判断目标物品是否为爆弹弹药
     */
    private boolean isBoltAmmo(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemStack boltItem = ModAmmoTypes.BOLT_ITEM;
        if (boltItem == null || boltItem.isEmpty()) return false;
        return stack.isItemEqual(boltItem);
    }

    /**
     * 获取目标物品的产出数量
     */
    private int getOutputCount(ItemStack target) {
        return isBoltAmmo(target) ? BOLT_OUTPUT_COUNT : 1;
    }

    @Override
    protected void checkAndStartOperation() {
        this.setContentsChanged(false);

        if (registeredTargets.isEmpty()) {
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= registeredTargets.size()) {
            selectedIndex = 0;
        }

        ItemStack target = registeredTargets.get(selectedIndex);
        if (target == null || target.isEmpty()) {
            return;
        }

        // 检查输出槽是否可接收
        if (!canOutput(target)) {
            return;
        }

        // 开始生产
        this.currentOperation = new MachineOperation(target.copy());
        this.progress = 0;
        this.totaltime = CRAFT_TIME;

        if (!this.world.isRemote) {
            this.needUpdate();
        }
    }

    protected boolean canOutput(ItemStack output) {
        if (output.isEmpty()) return false;
        int count = getOutputCount(output);
        ItemStack existing = this.inventory.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) return true;
        // 如果已有相同物品且未满，可以继续堆叠
        if (existing.isItemEqual(output) && existing.getCount() + count <= existing.getMaxStackSize()) {
            return true;
        }
        return false;
    }

    @Override
    protected void finishedOperation() {
        ItemStack result = this.currentOperation.getItemOutput0();
        int count = getOutputCount(result);
        result = result.copy();
        result.setCount(count);
        if (this.inventory.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            this.inventory.setStackInSlot(SLOT_OUTPUT, result);
        } else {
            this.inventory.insertItemNoCheck(SLOT_OUTPUT, result, false);
        }
    }

    // ===== 生产音效：进度开始时播放一次活塞声 =====
    @Override
    protected void playAmbientSound() {
        if (this.progress == 1) {
            double x = this.pos.getX() + 0.5d;
            double y = this.pos.getY() + 0.5d;
            double z = this.pos.getZ() + 0.5d;
            world.playSound(x, y, z,
                net.minecraft.init.SoundEvents.BLOCK_PISTON_EXTEND,
                SoundCategory.BLOCKS, 0.5f, 1.0f, true);
        }
    }
}
