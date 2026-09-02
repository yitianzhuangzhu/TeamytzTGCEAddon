package com.teamytz.tgceaddon.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * 投射物工具
 */
public final class ProjectileUtil {

    private ProjectileUtil() {
    }

    /**
     * 把射手速度叠加到投射物初速(初速继承)。
     *
     * 高速移动中(如鞘翅喷气背包飞行)发射导弹/火箭时,弹体初速应加上玩家速度,
     * 否则会出现"导弹比玩家还慢、追不上"的问题。
     * 需在投射物构造完成(初速已按视线×射速设定)后、spawnEntity 前调用。
     *
     * 实现要点:
     * 1. 玩家速度用 PlayerVelocityTracker 的多 tick 指数平滑值(服务端可靠,
     *    不受 motion 字段/单 tick 包时序影响),滑翔/冲刺时能真实读到速度;
     *    非玩家射手退回单 tick 位移估算。
     * 2. 只把玩家速度在"弹体飞行方向"上的投影分量加入初速:
     *    导弹沿弹道方向更快(玩家朝前飞、导弹朝前发射 → 初速 = 自身射速 + 玩家速度),
     *    但方向不变——玩家下落/俯冲时不会把导弹带偏成"发射即朝下砸地"。
     */
    public static void applyShooterVelocity(Entity projectile, EntityLivingBase shooter) {
        if (projectile == null || shooter == null) {
            return;
        }

        // 玩家速度(服务端平滑追踪);非玩家射手用单 tick 位移估算
        double pvx;
        double pvy;
        double pvz;
        if (shooter instanceof EntityPlayer) {
            Vec3d v = PlayerVelocityTracker.getVelocity((EntityPlayer) shooter);
            pvx = v.x;
            pvy = v.y;
            pvz = v.z;
        } else {
            pvx = shooter.posX - shooter.lastTickPosX;
            pvy = shooter.posY - shooter.lastTickPosY;
            pvz = shooter.posZ - shooter.lastTickPosZ;
        }

        // 弹体自身初速(方向 × 射速)
        double mx = projectile.motionX;
        double my = projectile.motionY;
        double mz = projectile.motionZ;
        double mlen = Math.sqrt(mx * mx + my * my + mz * mz);
        if (mlen < 1.0E-4D) {
            return;
        }

        // 玩家速度在弹道方向上的投影:proj = (玩家速度 · 弹道单位方向)
        double proj = (pvx * mx + pvy * my + pvz * mz) / mlen;
        if (proj <= 0.0D) {
            return; // 玩家相对弹道反向移动,不加速
        }

        // 沿弹道方向叠加:初速 += 弹道单位方向 × proj(方向不变,速度增加)
        projectile.motionX += mx / mlen * proj;
        projectile.motionY += my / mlen * proj;
        projectile.motionZ += mz / mlen * proj;
    }
}
