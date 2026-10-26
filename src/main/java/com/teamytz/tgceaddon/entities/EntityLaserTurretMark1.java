package com.teamytz.tgceaddon.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.TGSounds;
import techguns.entities.projectiles.EnumBulletFirePos;
import com.teamytz.tgceaddon.entities.projectiles.TurretLaserBeamProjectile;
import com.teamytz.tgceaddon.entities.projectiles.TurretOverloadLaserBeamProjectile;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;

/**
 * 激光炮塔 Mk.1（资产目录 models/Turret/laserturretMark1，贴图 laser_turret_mark1.png，256x256）。
 *
 * 武器 = 科技枪激光（{@link LaserProjectile}），**一次射击左右两门各射一道，共两束**：
 * - 伤害 16 / 弹速 100 / 光束存活 7 tick / 冷却 5 tick（同科技枪 lasergun；两束即双倍输出）
 * - **不需要弹药**（0.24.0 起）：每次射击消耗 {@link #POWER_PER_SHOT} RF，能量不足则不开火
 *
 * 炮口位置（bbmodel 分析）：
 * 两门炮的前端面 z=-41，X 分别 [-19.5,-11.5] 与 [11.5,19.5]，Y[7,21]（面心 y=14）
 * - bb (-15.5, 14, -41) → Java 模型空间 (15.5, 10, -41)
 * - bb ( 15.5, 14, -41) → Java 模型空间 (-15.5, 10, -41)
 * 左右由 bbmodel 的 l / r 分组给出，两者关于 x=0 完全对称。
 */
public class EntityLaserTurretMark1 extends EntityBaseTurret {

    /** 炮塔类型标识（对应卡片 NBT turretType） */
    public static final String TYPE = "laser_turret_mk1";

    /** 索敌/开火射程（格） */
    public static final double ATTACK_RANGE = 96.0D;

    // ===== 武器参数（取自科技枪 lasergun）=====
    private static final float DAMAGE = 16.0f;
    private static final float SPEED = 100.0f;
    private static final int TTL = 7;
    private static final int FIRE_COOLDOWN = 5;
    /** 装了炮塔升级卡后的射击间隔（tick）：间隔增大（射得更慢），换取更强爆炸与破坏软方块的能力 */
    public static final int FIRE_COOLDOWN_OVERLOAD = 20;
    private static final float DROP_START = 96.0f;
    private static final float DROP_END = 160.0f;
    private static final float DMG_MIN = DAMAGE;

    /** 每次射击消耗的 RF（0.24.0 起由"能量单元弹药"改为消耗机器 RF；一发 = 左右两束，故高于单束塔） */
    public static final int POWER_PER_SHOT = 100;

    /** 左门炮口（Java 模型空间像素坐标） */
    private static final float[] MUZZLE_LEFT_PX = {15.5F, 10.0F, -41.0F};
    /** 右门炮口 */
    private static final float[] MUZZLE_RIGHT_PX = {-15.5F, 10.0F, -41.0F};
    /** 俯仰旋转中心（俯仰分组 turret 的 origin：bb (0,14,0) → Java (0, 10, 0)） */
    private static final float[] PIVOT_PX = {0.0F, 10.0F, 0.0F};

    /** 俯仰限幅（用户给定）：俯角 40°、仰角不设限制（取 90° 作为实际上限，避免越过垂直） */
    @Override
    protected float getPitchMaxDown() {
        return 40.0F;
    }

    @Override
    protected float getPitchMaxUp() {
        return 90.0F;
    }

    public EntityLaserTurretMark1(World world) {
        super(world, TYPE);
    }

    public EntityLaserTurretMark1(World world, BlockPos masterPos, EnumFacing facing) {
        super(world, masterPos, facing, TYPE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(ATTACK_RANGE);
    }

    @Override
    protected double getAttackRange() {
        return ATTACK_RANGE;
    }

    @Override
    protected int getFireCooldown() {
        // 炮塔升级卡：射击间隔增大（更慢），换取更强的爆炸与破坏软方块的能力
        return hasUpgrade() ? FIRE_COOLDOWN_OVERLOAD : FIRE_COOLDOWN;
    }

    @Override
    protected boolean tryFire(EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        // 只消耗 RF：能量不足则本次不开火（不生成弹丸、不扣能量）
        if (master == null || !master.consumeTurretPower(POWER_PER_SHOT)) {
            return false;
        }
        // 炮塔升级卡：本次射击使用过载弹（爆炸更强 + 破坏徒手可挖的软方块）
        boolean overload = hasUpgrade();
        // 弹道方向：**两束必须保持平行**（本塔的设计），但整组以"两炮口中点 → 目标"的方向为准，
        // 这样平行光束对正对目标中心，而不是各打各的（两门相距约 1.9 格）
        Vec3d aim = aimPointOf(target);
        double[] left = muzzlePos(MUZZLE_LEFT_PX, PIVOT_PX);
        double[] right = muzzlePos(MUZZLE_RIGHT_PX, PIVOT_PX);
        float[] ang = aimAnglesFrom(
                (left[0] + right[0]) * 0.5D, (left[1] + right[1]) * 0.5D, (left[2] + right[2]) * 0.5D, aim);

        // 激光是命中即判定的光束：每束都要在构造前把炮塔摆到对应炮口，构造完复位
        double ox = this.posX, oy = this.posY, oz = this.posZ;
        this.spawnBeamAt(left, ang, aim, target, overload);
        this.spawnBeamAt(right, ang, aim, target, overload);
        restoreFromMuzzle(ox, oy, oz);

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                TGSounds.LASERGUN_FIRE, SoundCategory.BLOCKS, 3.0F, 1.0F);
        return true;
    }

    /**
     * 从指定炮口射出一束激光；两束共用同一方向 ang（保持平行）。
     * 普通弹 = {@link TurretLaserBeamProjectile}（小型爆炸、不破坏方块）；
     * 过载弹 = {@link TurretOverloadLaserBeamProjectile}（爆炸更强，并破坏徒手可挖的软方块）。
     */
    private void spawnBeamAt(double[] muzzle, float[] ang, Vec3d aim, EntityLivingBase target, boolean overload) {
        alignToMuzzleForSpawn(muzzle, ang[0], ang[1]);
        TurretLaserBeamProjectile beam = overload
                ? new TurretOverloadLaserBeamProjectile(this.world, this,
                        DAMAGE, SPEED, TTL, 0.0f, DROP_START, DROP_END, DMG_MIN, 0.0f,
                        false, EnumBulletFirePos.CENTER)
                : new TurretLaserBeamProjectile(this.world, this,
                        DAMAGE, SPEED, TTL, 0.0f, DROP_START, DROP_END, DMG_MIN, 0.0f,
                        false, EnumBulletFirePos.CENTER);
        this.world.spawnEntity(beam);
        logShot(target, aim, muzzle, beam.distance);
    }
}
