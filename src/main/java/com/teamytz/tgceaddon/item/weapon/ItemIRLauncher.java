package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.IRMissileProjectile;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.tracking.HeatSourceManager;
import com.teamytz.tgceaddon.util.ProjectileUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.TGSounds;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.items.guns.ProjectileSelector;

/**
 * 红外热追踪导弹发射器(两段式锁定发射)
 *
 * 左键流程:第一次左键进入索敌(白框闪烁)→ 引导头 30° 锥体扫描最热目标
 * (太阳 > 热源玩家)→ 锁定(红框)→ 再次左键发射红外导弹。
 * 性能参数参考科技枪制导导弹。
 */
public class ItemIRLauncher extends ItemLockOnWeapon {

    /** 引导头探测锥体半角(度),总视角 30° */
    public static final float SEARCH_CONE_HALF_ANGLE = 15.0F;
    /** 热源扫描距离(格) */
    public static final double SEARCH_RANGE = 64.0D;

    public ItemIRLauncher(String name, ProjectileSelector projectile_selector) {
        // 参数参考科技枪 guidedmissilelauncher
        super(true, name, projectile_selector, true, 10, 1, 40, 60.0f,
                TGSounds.GUIDEDMISSILE_FIRE, TGSounds.ROCKET_RELOAD, 300, 0.05f);

        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);

        this.setBulletSpeed(1.5f);
        this.setRecoiltime(10);
        this.setDamageDrop(2f, 4f, 50f);
        this.setTexture(new ResourceLocation(TGCEAddon.MODID + ":textures/guns/ir_missile_launcher"));
    }

    @Override
    public int getRequiredLockTicks() {
        return 15; // 0.75 秒索敌,白框闪烁
    }

    @Override
    public float getLockConeHalfAngle() {
        return SEARCH_CONE_HALF_ANGLE; // 锁定框与索敌锥体一致(总 30°)
    }

    @Override
    public double getLockRange() {
        return SEARCH_RANGE;
    }

    /**
     * 服务端每 tick 索敌:引导头 30° 锥体内找最热目标。
     * 优先级:太阳(最热,只要在视角内就锁定太阳)> 热值最高的热源
     * (玩家 100 / 末影水晶 120 / 科技枪导弹 70 / 热源方块 100,热值 > 25)。
     * 热值模型:热值 = 基础值 − 距离 × 0.5(玩家再乘剩余信号强度)。
     */
    @Override
    public void updateSearch(EntityPlayer player) {
        LockState state = getState(player);
        Vec3d look = player.getLookVec().normalize();
        double cosLimit = Math.cos(Math.toRadians(SEARCH_CONE_HALF_ANGLE));
        World world = player.world;

        // 1. 太阳:最热目标,在引导头视角内即锁定(玩家朝太阳方向看)
        Vec3d sunDir = IRMissileProjectile.getSunDirection(world);
        double sunDot = look.dotProduct(sunDir);
        if (TGCEAddon.DEBUG_IR && player.ticksExisted % 20 == 0) {
            TGCEAddon.getLogger().info("[IR] " + player.getName() + " 索敌:太阳方向=("
                    + String.format("%.2f", sunDir.x) + "," + String.format("%.2f", sunDir.y) + ","
                    + String.format("%.2f", sunDir.z) + ") 视角点积=" + String.format("%.2f", sunDot)
                    + " 阈值=" + String.format("%.2f", cosLimit));
        }
        if (sunDot >= cosLimit) {
            state.lockTicks++;
            if (state.lockTicks >= getRequiredLockTicks()) {
                lockSun(player);
            }
            return;
        }

        // 2. 热源(玩家/末影水晶/科技枪导弹/热源方块):取锥体内热值最高的(热值 > 25)
        Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        HeatSourceManager.HeatTarget target = HeatSourceManager.findHottestInCone(world,
                eye.x, eye.y, eye.z, look, cosLimit, SEARCH_RANGE, player, player, true);

        if (target != null) {
            state.lockTicks++;
            if (TGCEAddon.DEBUG_IR && state.lockTicks == 1) {
                String type = target.isBlock() ? "热源方块"
                        : (target.entity instanceof net.minecraft.entity.item.EntityEnderCrystal ? "末影水晶"
                                : (target.entity instanceof techguns.entities.projectiles.RocketProjectile ? "导弹"
                                        : "玩家"));
                TGCEAddon.getLogger().info("[IR] " + player.getName() + " 锁定目标候选: " + type
                        + "(热值 " + String.format("%.1f", target.heat) + ",距离 "
                        + String.format("%.0f", target.dist) + " 格),开始锁定...");
            }
            if (state.lockTicks >= getRequiredLockTicks()) {
                if (target.isBlock()) {
                    lockPos(player, target.block.getX() + 0.5D, target.block.getY() + 0.5D, target.block.getZ() + 0.5D);
                } else {
                    lockEntity(player, target.entity);
                }
            }
        } else {
            state.lockTicks = 0;
        }
    }

    /** 服务端发射:消耗火箭弹,创建红外导弹(锁定实体/太阳方向/方块坐标) */
    @Override
    public void fireProjectile(ItemStack stack, EntityPlayer player, LockState state) {
        if (!player.capabilities.isCreativeMode) {
            if (this.getCurrentAmmo(stack) <= 0) {
                return;
            }
            this.useAmmo(stack, 1);
        }
        if (TGCEAddon.DEBUG_IR) {
            TGCEAddon.getLogger().info("[IR] " + player.getName() + " 发射红外导弹 ("
                    + (state.lockedPos ? "方块坐标" : (state.lockedSun ? "太阳" : "实体")) + ")");
        }
        // 创建红外导弹(性能参考科技枪制导导弹;速度 1.5 快于玩家,叠加射手速度后更快)
        IRMissileProjectile missile;
        if (state.lockedPos) {
            missile = new IRMissileProjectile(player.world, player,
                    60.0f, 1.5f, 300, 0.05f, 2, 4, 50, 0.25f, false,
                    EnumBulletFirePos.CENTER, 4.0f, state.lockedX, state.lockedY, state.lockedZ);
        } else {
            missile = new IRMissileProjectile(player.world, player,
                    60.0f, 1.5f, 300, 0.05f, 2, 4, 50, 0.25f, false,
                    EnumBulletFirePos.CENTER, 4.0f, 0.01f, state.lockedEntity, state.lockedSun);
        }
        missile.setPosition(player.posX, player.posY + player.getEyeHeight() - 0.1, player.posZ);
        // 高速移动中发射(如鞘翅飞行):叠加射手速度,避免导弹比玩家还慢
        ProjectileUtil.applyShooterVelocity(missile, player);
        player.world.spawnEntity(missile);
        // 发射音效
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                TGSounds.GUIDEDMISSILE_FIRE, SoundCategory.PLAYERS, 3.0F, 1.0F);
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
                new ModelResourceLocation(TGCEAddon.MODID + ":ir_missile_launcher", "inventory"));
    }
}
