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
import techguns.entities.projectiles.TeslaProjectile;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;

/**
 * 闪电炮塔（资产目录 models/Turret/lightningturret，贴图 lightning_turret.png，128x128）。
 *
 * 武器 = **科技枪特斯拉闪电**（{@link TeslaProjectile}，特效/贴图/渲染全部复用科技枪）：
 * - 从模型**中间的发射口**射出闪电
 * - **连续伤害（连锁跳跃）机制一并继承**（原封不动地生成科技枪的弹丸，其自身逻辑即产生连锁）：
 *   {@code TeslaProjectile.onHit} 命中后在其 {@code CHAIN_RANGE=8} 格内寻找下一个目标，
 *   命中后继续连锁，最多 {@code CHAIN_TARGETS=4} 个目标，每跳伤害 ×{@code CHAIN_DAMAGE_FACTOR=0.75}
 *   伤害源为 {@code TGDamageSource.causeLightningDamage}（伤害类型 LIGHTNING）
 * - 伤害 9 / 弹速 80 / 光束存活 10 tick / 冷却 8 tick —— 取自科技枪 teslagun 的定义
 *   （{@code TGuns.teslagun}: damage 9.0 / setBulletSpeed(80.0) / minFiretime 8；TTL 见
 *    {@code TeslaProjectile.TTL}）
 * - **不需要弹药**（0.24.0 起）：每次射击消耗 {@link #POWER_PER_SHOT} RF，能量不足则不开火
 *
 * 炮口位置（bbmodel 分析）：发射口环面 z=-30、X[-4,4]、Y[7,15]，环心 bb=(0, 11, -30)
 * → Java 模型空间 = A(0, 11, -30) = (0, 13, -30)
 */
public class EntityLightningTurret extends EntityBaseTurret {

    /** 炮塔类型标识（对应卡片 NBT turretType） */
    public static final String TYPE = "lightning_turret";

    /** 索敌/开火射程（格） */
    public static final double ATTACK_RANGE = 80.0D;

    // ===== 武器参数（取自科技枪 teslagun）=====
    private static final float DAMAGE = 9.0f;
    private static final float SPEED = 80.0f;
    private static final int FIRE_COOLDOWN = 8;
    private static final float DROP_START = 80.0f;
    private static final float DROP_END = 160.0f;
    private static final float DMG_MIN = DAMAGE;
    /** 连锁目标数（科技枪 TeslaProjectile.CHAIN_TARGETS = 4） */
    private static final int CHAIN_TARGETS = TeslaProjectile.CHAIN_TARGETS;
    /** 装了炮塔升级卡后的连锁目标数（闪电能跳到更多敌人身上） */
    public static final int CHAIN_TARGETS_UPGRADED = 8;

    /** 每次射击消耗的 RF（0.24.0 起由"能量单元弹药"改为消耗机器 RF；连锁伤害故略高于激光塔） */
    public static final int POWER_PER_SHOT = 60;

    /** 炮口（Java 模型空间像素坐标） */
    private static final float[] MUZZLE_PX = {0.0F, 13.0F, -30.0F};
    /** 俯仰旋转中心（俯仰分组 main 的 origin：bb (0,7,0) → Java (0, 17, 0)） */
    private static final float[] PIVOT_PX = {0.0F, 17.0F, 0.0F};

    /** 俯仰限幅（用户给定）：俯角 25°、仰角 60° */
    @Override
    protected float getPitchMaxDown() {
        return 25.0F;
    }

    @Override
    protected float getPitchMaxUp() {
        return 60.0F;
    }

    public EntityLightningTurret(World world) {
        super(world, TYPE);
    }

    public EntityLightningTurret(World world, BlockPos masterPos, EnumFacing facing) {
        super(world, masterPos, facing, TYPE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(ATTACK_RANGE);
    }

    @Override
    protected double getAttackRange() {
        return ATTACK_RANGE;
    }

    @Override
    protected int getFireCooldown() {
        return FIRE_COOLDOWN;
    }

    @Override
    protected boolean tryFire(EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        // 只消耗 RF：能量不足则本次不开火（不生成弹丸、不扣能量）
        if (master == null || !master.consumeTurretPower(POWER_PER_SHOT)) {
            return false;
        }
        // 弹道方向：从本塔炮口指向目标瞄准点（不要用炮塔朝向，朝向有 5° 对准误差 → 命中率低）
        Vec3d aim = aimPointOf(target);
        double[] muzzle = muzzlePos(MUZZLE_PX, PIVOT_PX);
        float[] ang = aimAnglesFrom(muzzle[0], muzzle[1], muzzle[2], aim);

        // 炮塔升级卡：提高连锁目标数（闪电能连续跳到更多敌人身上，机制仍由科技枪弹丸自身实现）
        int chain = hasUpgrade() ? CHAIN_TARGETS_UPGRADED : CHAIN_TARGETS;

        // 闪电弹同样是"构造即命中 + 立即连锁"，起始点必须在构造前摆正
        double ox = this.posX, oy = this.posY, oz = this.posZ;
        alignToMuzzleForSpawn(muzzle, ang[0], ang[1]);
        TeslaProjectile bolt = new TeslaProjectile(this.world, this,
                DAMAGE, SPEED, TeslaProjectile.TTL, 0.0f, DROP_START, DROP_END, DMG_MIN, 0.0f,
                false, EnumBulletFirePos.CENTER, chain);
        restoreFromMuzzle(ox, oy, oz);
        this.world.spawnEntity(bolt);
        logShot(target, aim, muzzle, bolt.distance);

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                TGSounds.TESLA_FIRE, SoundCategory.BLOCKS, 3.0F, 1.0F);
        return true;
    }
}
