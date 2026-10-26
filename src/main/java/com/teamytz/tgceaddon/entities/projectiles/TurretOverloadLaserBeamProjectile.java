package com.teamytz.tgceaddon.entities.projectiles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import techguns.entities.projectiles.EnumBulletFirePos;

/**
 * 过载激光光束（激光炮塔 Mk.1 装上炮塔升级卡后使用）。
 *
 * 与 {@link TurretLaserBeamProjectile} 的唯一区别是爆炸参数（其余逻辑全部继承）：
 * - 爆炸强度 3.0（普通弹是 1.5）
 * - **会破坏"徒手可挖"的软方块**（硬度 ≤ {@link #SMASH_MAX_HARDNESS}：泥土/沙子/树叶/玻璃…），
 *   石头（1.5）、原木（2.0）等硬方块不受影响，且破坏不掉落物品
 *
 * 为什么单独一个类：科技枪激光弹在**构造器里就完成命中判定并回调 onHit**，
 * 参数必须在构造前确定，无法事后设置字段 —— 用子类把常量固化进构造路径最干净。
 */
public class TurretOverloadLaserBeamProjectile extends TurretLaserBeamProjectile {

    /** 过载弹的爆炸强度（普通弹的 2 倍） */
    public static final float OVERLOAD_EXPLOSION_STRENGTH = 3.0F;

    public TurretOverloadLaserBeamProjectile(World worldIn) {
        super(worldIn);
    }

    public TurretOverloadLaserBeamProjectile(World world, EntityLivingBase shooter, float damage, float speed, int TTL,
                                             float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                                             float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(world, shooter, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration,
                blockdamage, firePos);
    }

    @Override
    protected float getHitExplosionStrength() {
        return OVERLOAD_EXPLOSION_STRENGTH;
    }

    @Override
    protected boolean shouldSmashBlocks() {
        return true;
    }
}
