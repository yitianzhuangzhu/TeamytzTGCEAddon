package com.teamytz.tgceaddon.entities.projectiles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import techguns.entities.projectiles.EnumBulletFirePos;

/**
 * M1 主战坦克主炮的高爆弹（0.30.0）。
 *
 * <p>与基础炮塔的 HE 弹**同一套机制**（同样的弹道下坠、同样的爆炸特效与音效、
 * 同样的 2 格满伤 / 5 格杀伤半径），只是数值按坦克主炮放大：</p>
 * <ul>
 *   <li>中心伤害 <b>80</b>（基础炮塔 40）</li>
 *   <li>最外圈伤害 <b>25</b>（基础炮塔 6）——中间线性衰减</li>
 * </ul>
 *
 * <p>实现方式：父类把四处数值改成了 {@code getPrimaryDamage()/getSecondaryDamage()/...}
 * 取值点，这里只覆写伤害；半径与破方块系数原样继承，保证"范围继承基础炮塔"。</p>
 */
public class M1HEShellProjectile extends HEShellProjectile {

    /** 主半径内伤害 */
    public static final double M1_PRIMARY_DAMAGE = 80.0D;
    /** 次级半径边缘伤害 */
    public static final double M1_SECONDARY_DAMAGE = 25.0D;

    /** 炮塔侧参考参数（与 EntityM1Turret 共用，避免两边数值走散） */
    public static final float RECOMMENDED_DAMAGE = (float) M1_PRIMARY_DAMAGE;
    public static final float RECOMMENDED_SPEED = 4.0F;
    public static final int RECOMMENDED_TTL = 60;

    public M1HEShellProjectile(World worldIn) {
        super(worldIn);
    }

    public M1HEShellProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch,
                               float damage, float speed, int TTL, float spread, float dmgDropStart,
                               float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
                               EnumBulletFirePos firePos) {
        super(worldIn, posX, posY, posZ, yaw, pitch, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd,
                dmgMin, penetration, blockdamage, firePos);
    }

    public M1HEShellProjectile(World world, EntityLivingBase shooter, float damage, float speed, int TTL,
                               float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                               float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(world, shooter, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration,
                blockdamage, firePos);
    }

    @Override
    protected double getPrimaryDamage() {
        return M1_PRIMARY_DAMAGE;
    }

    @Override
    protected double getSecondaryDamage() {
        return M1_SECONDARY_DAMAGE;
    }
}
