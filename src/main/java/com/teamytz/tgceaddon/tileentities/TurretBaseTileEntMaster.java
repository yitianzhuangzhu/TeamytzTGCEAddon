package com.teamytz.tgceaddon.tileentities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.EntityBMPTTurret;
import com.teamytz.tgceaddon.entities.EntityRolandTurret;
import com.teamytz.tgceaddon.init.BlockTurretBase;
import com.teamytz.tgceaddon.init.BlockTurretBaseSlave;
import com.teamytz.tgceaddon.init.ModItems;
import com.teamytz.tgceaddon.item.ItemTurretCard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import techguns.TGItems;
import techguns.gui.ButtonConstants;
import techguns.tileentities.BasicPoweredTileEnt;
import techguns.tileentities.operation.ItemStackHandlerPlus;
import techguns.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 炮塔基座 - 主方块 TileEntity（炮塔控制器）
 * 负责 3x1x3 多方块结构的形成与解散，并作为炮塔 GUI 的服务端逻辑：
 * - master 位于结构中心 (0,0,0)，周围 8 格为 slave
 * - 继承 BasicPoweredTileEnt：自动获得 RF 能量存储、红石控制、安全(所有者)、
 *   物品栏(NBT 同步) 等科技枪机器能力
 * - 槽位：9 弹药输入 + 9 弹药输出 + 1 卡片槽（放入炮塔卡片，生成对应炮塔实体，后续实现）
 */
public class TurretBaseTileEntMaster extends BasicPoweredTileEnt implements ITickable {

    // ===== 槽位定义 =====
    public static final int SLOT_INPUT1 = 0;
    public static final int INPUTS_SIZE = 9;
    public static final int SLOT_OUTPUT1 = SLOT_INPUT1 + INPUTS_SIZE;
    public static final int OUTPUTS_SIZE = 9;
    public static final int SLOT_CARD = SLOT_OUTPUT1 + OUTPUTS_SIZE;
    /** 升级槽(对应科技枪炮台防护板槽位 index 19):放入炮塔升级卡 */
    public static final int SLOT_UPGRADE = SLOT_CARD + 1;

    // ===== 最大能量存储 (RF) =====
    public static final int MAX_POWER = 5000;

    // ===== 按钮 ID（沿用科技枪：SECURITY=1, REDSTONE=2, 之后为我们扩展）=====
    public static final int BUTTON_ID_TARGET_ANIMALS = ButtonConstants.BUTTON_ID_REDSTONE + 1;
    public static final int BUTTON_ID_PVP_SETTING = ButtonConstants.BUTTON_ID_REDSTONE + 2;

    // ===== 索敌设置 =====
    /** 是否攻击动物（GUI 动物按钮切换） */
    public boolean attackAnimals = false;
    /** PVP 设置：0=不攻击玩家，4=攻击所有其他玩家（GUI PVP 按钮切换） */
    protected byte pvpsetting = 0;

    // ===== 3x1x3 结构：master 中心，8 个 slave 围绕 =====
    private static final int[][] SLAVE_OFFSETS = {
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0, 0}, {1, 0, 0},
            {-1, 0, 1}, {0, 0, 1}, {1, 0, 1}
    };

    protected boolean formed = false;
    protected EnumFacing multiblockDirection = EnumFacing.SOUTH;
    /** 是否由玩家亲手放置(onBlockPlacedBy)。结构生成/模组放置的炮塔为 false:
     *  structure 模组用 setBlockState 直接放方块,不会调用 onBlockPlacedBy,
     *  但可能把原作者存档里的 owner/pvp 一起写进 NBT,导致玩家目标判定被绕过。 */
    protected boolean playerPlaced = false;

    // ===== 炮塔实体管理 =====
    /** 当前生成的炮塔实体（仅服务端持有引用，BMPT/罗兰共用） */
    private Entity mountedTurret = null;
    /** 炮塔实体的 UUID，用于世界重载后找回已存在的实体，避免重复生成 */
    private UUID mountedTurretUUID = null;
    /** debug：上次记录的状态，仅状态变化时打日志避免刷屏 */
    private boolean lastDebugFormed = false;
    private String lastDebugCardType = "";
    /** 结构完整性自检计时器(每 10 tick 一次) */
    private int integrityCheckTimer = 0;

    public TurretBaseTileEntMaster() {
        super(SLOT_UPGRADE + 1, false, MAX_POWER);
        this.inventory = new ItemStackHandlerPlus(SLOT_UPGRADE + 1) {
            @Override
            protected boolean allowItemInSlot(int slot, ItemStack stack) {
                if (slot == SLOT_CARD) {
                    return stack.getItem() instanceof ItemTurretCard;
                } else if (slot == SLOT_UPGRADE) {
                    // 升级槽:只允许炮塔升级卡
                    return stack.getItem() instanceof com.teamytz.tgceaddon.item.ItemTurretUpgrade;
                } else if (slot >= SLOT_INPUT1 && slot < SLOT_INPUT1 + INPUTS_SIZE) {
                    // 弹药输入槽：允许任意物品（换弹时消耗，由炮塔实体逻辑决定哪些是弹药）
                    return true;
                }
                // 输出槽不允许手动放入
                return false;
            }

            @Override
            protected boolean allowExtractFromSlot(int slot, int amount) {
                // 输出槽、卡片槽和升级槽允许取出
                return (slot >= SLOT_OUTPUT1 && slot < SLOT_OUTPUT1 + OUTPUTS_SIZE)
                        || slot == SLOT_CARD || slot == SLOT_UPGRADE;
            }
        };
    }

    public boolean isFormed() {
        return formed;
    }

    /**
     * 红石信号检测：遍历整个 3x3 结构（master + 8 个从方块）位置，
     * 任一位置收到强红石信号即视为有信号——任意炮塔方块上放拉杆/红石均可控制炮塔
     */
    @Override
    public void onNeighborBlockChange() {
        if (!this.world.isRemote) {
            boolean signal = false;
            for (int dx = -1; dx <= 1 && !signal; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (this.world.getStrongPower(this.pos.add(dx, 0, dz)) > 0) {
                        signal = true;
                        break;
                    }
                }
            }
            if (signal != this.hasSignal) {
                this.hasSignal = signal;
                this.needUpdate();
            }
        }
    }

    /**
     * 每 tick 检查卡片状态，生成/移除炮塔实体（仅服务端生效）
     * - 结构已形成且卡片槽有有效卡片 → 生成对应炮塔实体
     * - 结构解散或卡片被取出 → 移除炮塔实体
     */
    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        // 实体已死亡则清除引用
        if (this.mountedTurret != null && this.mountedTurret.isDead) {
            TGCEAddon.getLogger().info("[debug][炮塔] 实体已死亡，清除引用 (master@" + this.pos + ")");
            this.mountedTurret = null;
        }

        // 结构完整性自检(每 10 tick):自然结构/模组放置的炮塔 slave 可能未经过
        // checkAndForm 链接(hasMaster=false,破坏 slave 不会解散结构),这里补链接;
        // 任一 slave 缺失 → 解散整个结构,保证破坏任意结构方块即拆散。
        if (this.formed && ++this.integrityCheckTimer >= 10) {
            this.integrityCheckTimer = 0;
            this.validateMultiblock();
        }

        boolean wantTurret = this.formed && !getCardType().isEmpty();
        String cardType = getCardType();
        // debug：状态变化时记录（formed / 卡片类型 / 是否需要实体），避免每 tick 刷屏
        if (this.formed != this.lastDebugFormed || !cardType.equals(this.lastDebugCardType)) {
            TGCEAddon.getLogger().info("[debug][炮塔] 状态变化 master=" + this.pos
                    + " formed=" + this.formed + " cardType='" + cardType + "'"
                    + " wantTurret=" + wantTurret
                    + " mountedTurret=" + (this.mountedTurret != null ? "存在" : "空"));
            this.lastDebugFormed = this.formed;
            this.lastDebugCardType = cardType;
        }

        if (wantTurret) {
            if (this.mountedTurret == null) {
                // 世界重载后尝试按 UUID 找回已保存的实体
                if (this.mountedTurretUUID != null) {
                    for (Entity ent : this.world.loadedEntityList) {
                        if ((ent instanceof EntityBMPTTurret || ent instanceof EntityRolandTurret)
                                && ent.getUniqueID().equals(this.mountedTurretUUID)) {
                            this.mountedTurret = ent;
                            break;
                        }
                    }
                }
            }
            if (this.mountedTurret == null) {
                this.spawnTurretEntity();
            }
        } else if (this.mountedTurret != null) {
            this.killTurretEntity();
        }
    }

    /**
     * 在 master 上方生成炮塔实体(按卡片类型:bmpt → BMPT 炮塔,roland → 罗兰防空系统)
     */
    private void spawnTurretEntity() {
        Entity turret;
        String cardType = getCardType();
        if ("roland".equals(cardType)) {
            turret = new EntityRolandTurret(this.world, this.pos, this.multiblockDirection);
        } else {
            // 默认/bmpt
            turret = new EntityBMPTTurret(this.world, this.pos, this.multiblockDirection);
        }
        boolean spawned = this.world.spawnEntity(turret);
        this.mountedTurret = turret;
        this.mountedTurretUUID = turret.getUniqueID();
        TGCEAddon.getLogger().info("[debug] 炮塔实体已生成 (master@" + this.pos + ", 类型" + cardType
                + ", spawnEntity=" + spawned + ", entityId=" + turret.getEntityId() + ")");
    }

    /**
     * 移除当前炮塔实体
     */
    public void killTurretEntity() {
        if (this.mountedTurret != null && !this.mountedTurret.isDead) {
            this.mountedTurret.setDead();
        }
        this.mountedTurret = null;
        this.mountedTurretUUID = null;
    }

    public EnumFacing getMultiblockDirection() {
        return multiblockDirection;
    }

    /** 是否由玩家亲手放置(自然结构/模组生成的炮塔为 false,视为敌对建筑) */
    public boolean isPlayerPlaced() {
        return this.playerPlaced;
    }

    public void setPlayerPlaced(boolean v) {
        this.playerPlaced = v;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("container.tgceaddon.turret_base");
    }

    // ===== 卡片相关（炮塔实体生成逻辑后续补充）=====
    public ItemStack getCard() {
        return this.inventory.getStackInSlot(SLOT_CARD);
    }

    /**
     * 当前卡片指定的炮塔类型，无卡片/未知时返回空字符串
     */
    public String getCardType() {
        return ItemTurretCard.getTurretType(getCard());
    }

    // ===== 升级槽相关 =====
    public ItemStack getUpgrade() {
        return this.inventory.getStackInSlot(SLOT_UPGRADE);
    }

    /**
     * 升级槽中是否安装了指定类型的升级卡
     */
    public boolean hasUpgrade(String type) {
        return com.teamytz.tgceaddon.item.ItemTurretUpgrade.getUpgradeType(getUpgrade()).equals(type);
    }

    // ===== GUI 按钮事件（科技枪 PacketGuiButtonClick -> tile.buttonClicked）=====
    @Override
    public void buttonClicked(int id, EntityPlayer ply, String data) {
        if (id < BUTTON_ID_TARGET_ANIMALS) {
            super.buttonClicked(id, ply, data);
        } else if (id == BUTTON_ID_TARGET_ANIMALS && this.isUseableByPlayer(ply)) {
            this.attackAnimals = !this.attackAnimals;
            if (!this.world.isRemote) {
                this.needUpdate();
            }
        } else if (id == BUTTON_ID_PVP_SETTING && this.isOwnedByPlayer(ply)) {
            // 简化版 PVP 切换（与科技枪无 FTB 时一致）：0 <-> 4
            this.pvpsetting = (byte) (this.pvpsetting == 0 ? 4 : 0);
            if (!this.world.isRemote) {
                this.needUpdate();
            }
        }
    }

    public byte getPvpSetting() {
        return pvpsetting;
    }

    // ===== 炮塔开火消耗（供炮塔实体调用）=====
    /**
     * 消耗 RF，成功返回 true（不消耗则 false）
     */
    public boolean consumeTurretPower(int amount) {
        return this.consumePower(amount);
    }

    /**
     * 弹药输入槽中是否有机炮炮弹
     * 注意:InventoryUtil.canConsumeItem 返回 0 表示"找到足够弹药",>0 表示不足,
     * 所以"有弹药"的判定是 <= 0(曾误用 > 0 导致判定完全反相)
     */
    public boolean hasCannonAmmo() {
        return InventoryUtil.canConsumeItem(this.inventory, new ItemStack(ModItems.cannonShell),
                SLOT_INPUT1, SLOT_INPUT1 + INPUTS_SIZE) <= 0;
    }

    /**
     * 消耗一发机炮炮弹（从弹药输入槽扣）
     */
    public boolean consumeCannonAmmo() {
        return InventoryUtil.consumeAmmo(this.inventory, new ItemStack(ModItems.cannonShell),
                SLOT_INPUT1, SLOT_INPUT1 + INPUTS_SIZE);
    }

    /**
     * 消耗一发火箭弹（TGItems.ROCKET，从弹药输入槽扣）
     * 注意：混淆版 techguns 2.1.3.0 中 TGItems.ROCKET 是 ItemStack 类型（共享物品默认栈）
     */
    public boolean consumeRocketAmmo() {
        return InventoryUtil.consumeAmmo(this.inventory, TGItems.ROCKET,
                SLOT_INPUT1, SLOT_INPUT1 + INPUTS_SIZE);
    }

    /**
     * 弹药输入槽中是否有火箭弹(返回 0 = 足够,见 hasCannonAmmo 注释)
     */
    public boolean hasRocketAmmo() {
        return InventoryUtil.canConsumeItem(this.inventory, TGItems.ROCKET,
                SLOT_INPUT1, SLOT_INPUT1 + INPUTS_SIZE) <= 0;
    }

    /**
     * 弹药输入槽中是否有核火箭(核弹变体弹药)
     */
    public boolean hasNukeAmmo() {
        return InventoryUtil.canConsumeItem(this.inventory, TGItems.ROCKET_NUKE,
                SLOT_INPUT1, SLOT_INPUT1 + INPUTS_SIZE) <= 0;
    }

    /**
     * 消耗一发核火箭(TGItems.ROCKET_NUKE,从弹药输入槽扣)
     */
    public boolean consumeNukeAmmo() {
        return InventoryUtil.consumeAmmo(this.inventory, TGItems.ROCKET_NUKE,
                SLOT_INPUT1, SLOT_INPUT1 + INPUTS_SIZE);
    }

    /**
     * 读取指定弹药输入槽的物品(调试用)
     */
    public ItemStack getInputStack(int slot) {
        if (slot >= SLOT_INPUT1 && slot < SLOT_INPUT1 + INPUTS_SIZE) {
            return this.inventory.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取所有 slave 相对 master 的偏移位置
     */
    public static List<BlockPos> getSlaveOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int[] off : SLAVE_OFFSETS) {
            offsets.add(new BlockPos(off[0], off[1], off[2]));
        }
        return offsets;
    }

    /**
     * 检查 8 个 slave 位置是否都是炮塔基座从方块，若满足则形成结构
     * debug：形成失败时输出缺失位置日志
     */
    public boolean checkAndForm(EntityPlayer player, EnumFacing facing) {
        World w = this.world;
        if (w == null || w.isRemote) {
            return false;
        }
        EnumFacing dir = facing.getAxis().isHorizontal() ? facing : EnumFacing.SOUTH;

        // 检查所有 slave 位置，记录缺失位置用于 debug 提示
        List<BlockPos> missing = new ArrayList<>();
        for (BlockPos off : getSlaveOffsets()) {
            BlockPos p = this.pos.add(off.getX(), off.getY(), off.getZ());
            if (!(w.getBlockState(p).getBlock() instanceof BlockTurretBaseSlave)) {
                missing.add(off);
            }
        }
        if (!missing.isEmpty()) {
            StringBuilder sb = new StringBuilder("[debug] 炮塔基座结构检查失败 (master@")
                    .append(this.pos).append(")，缺失的支撑方块位置(相对master)：");
            for (BlockPos off : missing) {
                sb.append(String.format(" (%d,%d,%d)", off.getX(), off.getY(), off.getZ()));
            }
            TGCEAddon.getLogger().info(sb.toString());
            return false;
        }

        // 形成结构
        this.formed = true;
        this.multiblockDirection = dir;
        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos)
                .withProperty(BlockTurretBase.FORMED, true), 3);

        for (BlockPos off : getSlaveOffsets()) {
            BlockPos p = this.pos.add(off.getX(), off.getY(), off.getZ());
            TileEntity tile = w.getTileEntity(p);
            if (tile instanceof TurretBaseTileEntSlave) {
                ((TurretBaseTileEntSlave) tile).form(this.pos);
            }
        }
        this.needUpdate();
        TGCEAddon.getLogger().info("[debug] 炮塔基座结构形成成功 (master@" + this.pos + ", 朝向" + dir.getName() + ", 8个支撑已链接)");
        return true;
    }

    /**
     * 结构完整性自检:
     * - 存在但未链接的 slave(form 设 hasMaster + master 坐标,兼容结构生成/模组放置)
     * - 任一 slave 缺失 → 解散整个结构
     */
    private void validateMultiblock() {
        boolean complete = true;
        for (BlockPos off : getSlaveOffsets()) {
            BlockPos p = this.pos.add(off.getX(), off.getY(), off.getZ());
            if (!(this.world.getBlockState(p).getBlock() instanceof BlockTurretBaseSlave)) {
                complete = false;
                break;
            }
            TileEntity te = this.world.getTileEntity(p);
            if (te instanceof TurretBaseTileEntSlave && !((TurretBaseTileEntSlave) te).hasMaster()) {
                ((TurretBaseTileEntSlave) te).form(this.pos);
            }
        }
        if (!complete) {
            TGCEAddon.getLogger().info("[debug] 炮塔基座结构完整性检查:slave 缺失,解散 (master@" + this.pos + ")");
            this.onMultiBlockBreak();
        }
    }

    /**
     * 解散结构（master 已存在时调用，如被破坏前）
     */
    public void onMultiBlockBreak() {
        if (!this.formed) {
            return;
        }
        World w = this.world;
        if (w == null || w.isRemote) {
            return;
        }
        for (BlockPos off : getSlaveOffsets()) {
            BlockPos p = this.pos.add(off.getX(), off.getY(), off.getZ());
            TileEntity tile = w.getTileEntity(p);
            if (tile instanceof TurretBaseTileEntSlave) {
                ((TurretBaseTileEntSlave) tile).unform();
            }
        }
        this.unform();
        TGCEAddon.getLogger().info("[debug] 炮塔基座结构已解散 (master@" + this.pos + ")");
    }

    /**
     * master 自身解散
     */
    public void unform() {
        this.formed = false;
        this.killTurretEntity();
        if (this.world != null && !this.world.isRemote) {
            if (this.world.getBlockState(this.pos).getBlock() instanceof BlockTurretBase) {
                this.world.setBlockState(this.pos, this.world.getBlockState(this.pos)
                        .withProperty(BlockTurretBase.FORMED, false), 3);
                // setBlockState(flag 3) 已通知客户端刷新。
                // 仅在方块仍存在时发送 TE 更新：master 被破坏时 blockstate 已是空气，
                // 若仍发送 TE 更新包，客户端会报 "invalid update packet for null tile entity"。
                this.needUpdate();
            }
        }
    }

    /**
     * 已形成状态下用扳手旋转结构朝向
     */
    public void rotateStructure(EnumFacing side) {
        if (!this.formed || !side.getAxis().isHorizontal()) {
            return;
        }
        this.multiblockDirection = side;
        if (this.world != null && !this.world.isRemote) {
            this.needUpdate();
        }
    }

    // ===== 网络/NBT 同步：父类链已处理 energy/redstone/security/inventory =====
    @Override
    public void writeClientDataToNBT(NBTTagCompound tags) {
        super.writeClientDataToNBT(tags);
        tags.setBoolean("formed", this.formed);
        if (this.formed) {
            tags.setByte("multiblockDirection", (byte) this.multiblockDirection.getIndex());
        }
        tags.setBoolean("attackAnimals", this.attackAnimals);
        tags.setByte("pvpsetting", this.pvpsetting);
        tags.setBoolean("playerPlaced", this.playerPlaced);
        if (this.mountedTurretUUID != null) {
            tags.setString("mountedTurretUUID", this.mountedTurretUUID.toString());
        }
    }

    @Override
    public void readClientDataFromNBT(NBTTagCompound tags) {
        super.readClientDataFromNBT(tags);
        this.formed = tags.getBoolean("formed");
        if (this.formed) {
            this.multiblockDirection = EnumFacing.getFront(tags.getByte("multiblockDirection"));
        }
        this.attackAnimals = tags.getBoolean("attackAnimals");
        this.pvpsetting = tags.getByte("pvpsetting");
        this.playerPlaced = tags.getBoolean("playerPlaced");
        if (tags.hasKey("mountedTurretUUID")) {
            try {
                this.mountedTurretUUID = UUID.fromString(tags.getString("mountedTurretUUID"));
            } catch (IllegalArgumentException e) {
                this.mountedTurretUUID = null;
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        // 仅方块类型变化才刷新 TE（FORMED 属性变化保留 TE，保持库存/能量数据）
        return oldState.getBlock() != newState.getBlock();
    }
}
