package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.IRMissileProjectile;
import com.teamytz.tgceaddon.network.IRFireMessage;
import com.teamytz.tgceaddon.network.LockOnStartMessage;
import com.teamytz.tgceaddon.network.LockStateMessage;
import com.teamytz.tgceaddon.network.PacketHandler;
import com.teamytz.tgceaddon.tracking.HeatSourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import techguns.items.guns.GenericGun;
import techguns.items.guns.ProjectileSelector;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 两段式锁定发射武器基类(非蓄力)
 *
 * 左键流程:第一次左键 → 进入索敌(白框闪烁)→ 索敌锁定(红框)→ 再次左键发射。
 * - 索敌扫描由子类实现(updateSearch,服务端每 tick 调用)
 * - 发射由子类实现(fireProjectile,服务端)
 * - 客户端通过 RenderGameOverlayEvent 绘制瞄准框(白/红),状态由 LockStateMessage 同步
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public abstract class ItemLockOnWeapon extends GenericGun {

    /** 玩家锁定状态(两端各自维护,客户端由 LockStateMessage 同步) */
    public static class LockState {
        /** 索敌中(白框闪烁) */
        public boolean searching = false;
        /** 已锁定(红框) */
        public boolean locked = false;
        /** 索敌进度(tick) */
        public int lockTicks = 0;
        /** 锁定的实体(可为 null) */
        public Entity lockedEntity = null;
        /** 锁定的是太阳(无实体目标) */
        public boolean lockedSun = false;
        /** 锁定的是方块热源坐标(无实体目标) */
        public boolean lockedPos = false;
        /** 方块热源锁定坐标 */
        public double lockedX = 0.0D;
        public double lockedY = 0.0D;
        public double lockedZ = 0.0D;
    }

    private static final WeakHashMap<EntityPlayer, LockState> STATES = new WeakHashMap<>();

    public ItemLockOnWeapon(boolean addToGunList, String name, ProjectileSelector projectile_selector,
            boolean semiAuto, int minFiretime, int clipsize, int reloadtime, float damage,
            net.minecraft.util.SoundEvent firesound, net.minecraft.util.SoundEvent reloadsound,
            int TTL, float accuracy) {
        super(addToGunList, name, projectile_selector, semiAuto, minFiretime, clipsize, reloadtime, damage,
                firesound, reloadsound, TTL, accuracy);
    }

    public static LockState getState(EntityPlayer player) {
        LockState state = STATES.get(player);
        if (state == null) {
            state = new LockState();
            STATES.put(player, state);
        }
        return state;
    }

    public static void clearState(EntityPlayer player) {
        STATES.remove(player);
    }

    /** 索敌所需 tick(子类可覆写,默认 10 tick = 0.5 秒) */
    public int getRequiredLockTicks() {
        return 10;
    }

    /** 锁定框锥体半角(度):锁定后目标必须保持在该锥体内,否则脱锁。默认 15°(总 30°) */
    public float getLockConeHalfAngle() {
        return 15.0F;
    }

    /** 锁定保持距离(格):方块热源无热值衰减,用距离兜底。默认 64 */
    public double getLockRange() {
        return 64.0D;
    }

    /** 服务端每 tick 索敌扫描(子类实现):找到目标后调用 lockEntity/lockSun */
    public abstract void updateSearch(EntityPlayer player);

    /** 服务端发射(子类实现,创建对应投射物)。stack 用于消耗弹药等 */
    public abstract void fireProjectile(ItemStack stack, EntityPlayer player, LockState state);

    /**
     * 服务端发射请求处理(IRFireMessage 触发):
     * 锁定完成后的发射——弹药由子类 fireProjectile 内处理(或基类统一处理),
     * 发射后清除锁定状态。
     */
    public void fireRequested(EntityPlayer player, ItemStack stack) {
        LockState state = getState(player);
        if (!state.locked) {
            return;
        }
        fireProjectile(stack, player, state);
        resetLock(player);
    }

    /** 客户端是否显示瞄准框 */
    public boolean showLockFrame() {
        return true;
    }

    // ===== 状态操作(供子类/网络包调用) =====
    public void startSearch(EntityPlayer player) {
        LockState state = getState(player);
        state.searching = true;
        state.locked = false;
        state.lockTicks = 0;
        state.lockedEntity = null;
        state.lockedSun = false;
        state.lockedPos = false;
        state.lockedX = state.lockedY = state.lockedZ = 0.0D;
        if (TGCEAddon.DEBUG_IR) {
            TGCEAddon.getLogger().info("[IR] " + player.getName() + " 开始索敌(引导头 30° 锥体)");
        }
    }

    public void lockEntity(EntityPlayer player, Entity entity) {
        LockState state = getState(player);
        state.searching = false;
        state.locked = true;
        state.lockedEntity = entity;
        state.lockedSun = false;
        state.lockedPos = false;
        if (TGCEAddon.DEBUG_IR) {
            TGCEAddon.getLogger().info("[IR] " + player.getName() + " 锁定热源实体: " + entity.getName()
                    + " @ (" + (int) entity.posX + "," + (int) entity.posY + "," + (int) entity.posZ + ")");
        }
        syncLockState(player);
    }

    public void lockSun(EntityPlayer player) {
        LockState state = getState(player);
        state.searching = false;
        state.locked = true;
        state.lockedEntity = null;
        state.lockedSun = true;
        state.lockedPos = false;
        if (TGCEAddon.DEBUG_IR) {
            TGCEAddon.getLogger().info("[IR] " + player.getName() + " 锁定太阳!");
        }
        syncLockState(player);
    }

    /** 锁定方块热源坐标(服务端) */
    public void lockPos(EntityPlayer player, double x, double y, double z) {
        LockState state = getState(player);
        state.searching = false;
        state.locked = true;
        state.lockedEntity = null;
        state.lockedSun = false;
        state.lockedPos = true;
        state.lockedX = x;
        state.lockedY = y;
        state.lockedZ = z;
        if (TGCEAddon.DEBUG_IR) {
            TGCEAddon.getLogger().info("[IR] " + player.getName() + " 锁定热源方块 @ ("
                    + (int) x + "," + (int) y + "," + (int) z + ")");
        }
        syncLockState(player);
    }

    /** 发射后清除锁定状态 */
    public void resetLock(EntityPlayer player) {
        LockState state = getState(player);
        state.searching = false;
        state.locked = false;
        state.lockTicks = 0;
        state.lockedEntity = null;
        state.lockedSun = false;
        state.lockedPos = false;
        state.lockedX = state.lockedY = state.lockedZ = 0.0D;
        syncLockState(player);
    }

    /**
     * 脱锁:锁定目标脱离锁定框区域(或热值过低/死亡/遮挡)时调用。
     * 与 resetLock 不同:脱锁后自动重新进入白色索敌状态(继续 updateSearch)。
     */
    public void breakLock(EntityPlayer player) {
        LockState state = getState(player);
        if (!state.searching) {
            state.searching = true;
            state.locked = false;
            state.lockTicks = 0;
            state.lockedEntity = null;
            state.lockedSun = false;
            state.lockedPos = false;
            state.lockedX = state.lockedY = state.lockedZ = 0.0D;
            if (TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[IR] " + player.getName() + " 目标脱离锁定框,脱锁 → 重新索敌");
            }
            syncLockState(player);
        }
    }

    /**
     * 锁定保持检查(服务端每 tick):红框锁定后,目标必须持续满足:
     * - 实体:存活 + 视线可达 + 位于锁定框锥体内 + 热值 > 25
     * - 方块热源:位于锁定框锥体内 + 距离 ≤ 搜索范围(方块热值恒定)
     * - 太阳:仍在锁定框锥体内
     * 任一不满足 → breakLock 脱锁并回到白色索敌。
     */
    protected void checkLockMaintained(EntityPlayer player, LockState state) {
        Vec3d look = player.getLookVec().normalize();
        double cosLimit = Math.cos(Math.toRadians(getLockConeHalfAngle()));
        Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        if (state.lockedSun) {
            Vec3d sunDir = IRMissileProjectile.getSunDirection(player.world);
            if (look.dotProduct(sunDir) < cosLimit) {
                breakLock(player);
            }
            return;
        }

        if (state.lockedEntity != null) {
            Entity t = state.lockedEntity;
            if (t.isDead || !player.canEntityBeSeen(t)) {
                breakLock(player);
                return;
            }
            Vec3d to = new Vec3d(t.posX - eye.x,
                    t.posY + t.height * 0.5D - eye.y,
                    t.posZ - eye.z).normalize();
            if (look.dotProduct(to) < cosLimit) {
                breakLock(player);
                return;
            }
            // 热值检查:热源随距离衰减,热值 ≤ 25 则脱锁(玩家/末影水晶/导弹统一处理)
            float heat = HeatSourceManager.getEntityHeat(t, eye.x, eye.y, eye.z);
            if (heat <= HeatSourceManager.MIN_LOCK_HEAT) {
                if (TGCEAddon.DEBUG_IR) {
                    TGCEAddon.getLogger().info("[IR] " + player.getName() + " 目标热值过低("
                            + String.format("%.1f", heat) + "),脱锁");
                }
                breakLock(player);
            }
            return;
        }

        if (state.lockedPos) {
            Vec3d center = new Vec3d(state.lockedX, state.lockedY, state.lockedZ);
            Vec3d to = center.subtract(eye);
            double dist = to.lengthVector();
            if (dist > getLockRange()) { // 方块热源恒定热值,用距离兜底
                breakLock(player);
                return;
            }
            if (look.dotProduct(to.normalize()) < cosLimit) {
                breakLock(player);
            }
        }
    }

    /** 服务端把锁定状态同步给客户端 */
    protected void syncLockState(EntityPlayer player) {
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            LockState state = getState(player);
            int entityId = state.lockedEntity != null ? state.lockedEntity.getEntityId() : -1;
            PacketHandler.sendTo(new LockStateMessage(state.searching, state.locked, state.lockedSun, entityId,
                    state.lockedPos, state.lockedX, state.lockedY, state.lockedZ), (EntityPlayerMP) player);
        }
    }

    // ===== 左键两段式 =====
    @Override
    public void shootGunPrimary(ItemStack stack, World world, EntityPlayer player, boolean zooming, EnumHand hand, Entity target) {
        LockState state = getState(player);

        if (world.isRemote) {
            // ===== 客户端 =====
            if (!state.searching && !state.locked) {
                // 第一次左键:进入索敌
                state.searching = true;
                state.lockTicks = 0;
                state.lockedEntity = null;
                state.lockedSun = false;
                state.lockedPos = false;
                PacketHandler.sendToServer(new LockOnStartMessage());
                return;
            } else if (!state.locked) {
                // 索敌中:左键不动作
                return;
            }
            // 已锁定:请求发射(服务端执行)
            if (TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[IR] " + player.getName() + " 左键请求发射(已锁定)");
            }
            PacketHandler.sendToServer(new IRFireMessage());
            return;
        }
        // 服务端 shootGunPrimary 由发射包(IRFireMessage)统一驱动,这里不重复发射
    }

    // ===== 右键取消锁定 =====
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        LockState state = getState(playerIn);
        if (state.searching || state.locked) {
            // 索敌中/已锁定:右键 = 取消锁定
            if (worldIn.isRemote) {
                // 客户端立即清除本地状态(瞄准框消失),等待服务端 LockStateMessage 确认
                state.searching = false;
                state.locked = false;
                state.lockTicks = 0;
                state.lockedEntity = null;
                state.lockedSun = false;
                state.lockedPos = false;
                state.lockedX = state.lockedY = state.lockedZ = 0.0D;
            } else {
                // 服务端清除并同步给客户端
                resetLock(playerIn);
            }
            if (TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[IR] " + playerIn.getName() + " 右键取消锁定");
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        // 无锁定状态:走原版副手动作(GenericGun.gunSecondaryAction 等)
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    // ===== 服务端每 tick 索敌 =====
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }
        EntityPlayer player = event.player;
        ItemStack stack = player.getHeldItemMainhand();
        if (!(stack.getItem() instanceof ItemLockOnWeapon)) {
            return;
        }
        ItemLockOnWeapon weapon = (ItemLockOnWeapon) stack.getItem();
        LockState state = getState(player);
        if (state.searching && !state.locked) {
            weapon.updateSearch(player);
            // 子类锁定后状态已更新
        }
        // 锁定保持:目标必须持续位于锁定框区域内,否则脱锁回到白色索敌
        if (state.locked) {
            weapon.checkLockMaintained(player, state);
        }
        // 清理:玩家不再手持武器时清除状态(延迟)
        if (player.ticksExisted % 40 == 0) {
            cleanupStates(player);
        }
    }

    /** 定期清理不相关玩家的状态(防泄漏) */
    private static void cleanupStates(EntityPlayer current) {
        Iterator<Map.Entry<EntityPlayer, LockState>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<EntityPlayer, LockState> entry = it.next();
            EntityPlayer p = entry.getKey();
            if (p == null || p.isDead) {
                it.remove();
                continue;
            }
            ItemStack held = p.getHeldItemMainhand();
            if (held.isEmpty() || !(held.getItem() instanceof ItemLockOnWeapon)) {
                it.remove();
            }
        }
    }
}
