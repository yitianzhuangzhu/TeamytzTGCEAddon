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
import techguns.entities.projectiles.LaserProjectile;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;

/**
 * 激光炮塔（资产目录 models/Turret/laserturret，贴图 laser_turret.png，128x128）。
 *
 * 武器 = **科技枪激光枪的激光**（{@link LaserProjectile}，贴图/特效/渲染全部复用科技枪，
 * 已由科技枪注册实体与渲染器，本模组直接生成即可）：
 * - 从模型**中间的发射口**射出单束激光
 * - 伤害 16、弹速 100、光束存活 7 tick、冷却 5 tick —— 全部取自科技枪 lasergun 的定义
 *   （{@code TGuns.lasergun}: damage 16.0 / setBulletSpeed(100.0) / minFiretime 5；
 *    TTL 7 见 {@code LaserProjectile.Factory}）
 * - **不需要弹药**（0.24.0 起）：每次射击消耗 {@link #POWER_PER_SHOT} RF，
 *   能量不足则不开火（原先的"每 20 发一个能量单元"已按要求移除）
 *
 * 炮口位置（由 bbmodel 分析得出，见 TURRET_GUIDE.md）：
 * 模型方向修正后发射端面为 bb z=-18、X[-2,2]、Y[9,13]，端面心 bb=(0, 11, -18)
 * → Java 模型空间 = A(0, 11, -18) = (0, 13, -18)
 */
public class EntityLaserTurret extends EntityBaseTurret {

    /** 炮塔类型标识（对应卡片 NBT turretType） */
    public static final String TYPE = "laser_turret";

    /** 索敌/开火射程（格） */
    public static final double ATTACK_RANGE = 64.0D;

    // ===== 武器参数（取自科技枪 lasergun）=====
    private static final float DAMAGE = 16.0f;
    private static final float SPEED = 100.0f;
    private static final int TTL = 7;
    private static final int FIRE_COOLDOWN = 5;
    /** 伤害衰减：射程内满伤，超出后到 128 格线性衰减到 16（科技枪 lasergun 为 NO_DROP） */
    private static final float DROP_START = 64.0f;
    private static final float DROP_END = 128.0f;
    private static final float DMG_MIN = DAMAGE;

    /** 每次射击消耗的 RF（0.24.0 起由"能量单元弹药"改为消耗机器 RF；数值可按需调整） */
    public static final int POWER_PER_SHOT = 50;

    /** 装了炮塔升级卡后一次射出的光束数（多道光束） */
    public static final int MULTI_BEAM_COUNT = 3;
    /** 多光束之间的横向间距（格）：各束从略微错开的起点**各自瞄准目标**，所以都会命中，只是看起来是多道光束 */
    public static final double MULTI_BEAM_SPACING = 0.22D;

    /** 炮口（Java 模型空间像素坐标）
     *  2026-09-20 模型方向修正后重算：原来炮管朝 +Z，作者把 base/connection 两组各绕 Y 转 -180°，
     *  转换器已把该旋转烘焙进元素坐标 → 炮管现朝 -Z（与 BMPT 等一致），发射端面移到 bb z=-18。
     *  端面 bb=(0, 11, -18)（X[-2,2] Y[9,13]）→ Java = A(0, 11, -18) = (0, 13, -18) */
    private static final float[] MUZZLE_PX = {0.0F, 13.0F, -18.0F};
    /** 俯仰旋转中心 = 俯仰分组 connection 的旋转点（bb origin (0,7,-1) → Java (0, 17, -1)） */
    private static final float[] PIVOT_PX = {0.0F, 17.0F, -1.0F};

    /** 俯仰限幅（用户给定）：俯角 22°、仰角 17° */
    @Override
    protected float getPitchMaxDown() {
        return 22.0F;
    }

    @Override
    protected float getPitchMaxUp() {
        return 17.0F;
    }

    /** 俯仰分组 connection 带 180° 组旋转（模型方向修正留下），俯仰符号需取反 */
    @Override
    public float getModelPitchSign() {
        return -1.0F;
    }

    public EntityLaserTurret(World world) {
        super(world, TYPE);
    }

    public EntityLaserTurret(World world, BlockPos masterPos, EnumFacing facing) {
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
        // 炮塔升级卡：一次射出多道光束（各束从略微错开的起点**各自瞄准目标**，
        // 所以都会命中，视觉上是多道光束而不是散弹打偏）
        int beams = hasUpgrade() ? MULTI_BEAM_COUNT : 1;
        Vec3d aim = aimPointOf(target);
        double[] muzzle = muzzlePos(MUZZLE_PX, PIVOT_PX);

        double ox = this.posX, oy = this.posY, oz = this.posZ;
        if (beams <= 1) {
            this.spawnBeam(muzzle, aim, target);
        } else {
            // 以瞄准方向的水平垂直向量错开起点：-1,0,+1 三束
            double dx = aim.x - muzzle[0];
            double dz = aim.z - muzzle[2];
            double len = Math.sqrt(dx * dx + dz * dz);
            double px = len > 1.0E-4D ? dz / len : 1.0D;
            double pz = len > 1.0E-4D ? -dx / len : 0.0D;
            for (int i = 0; i < beams; i++) {
                double off = (i - (beams - 1) / 2.0D) * MULTI_BEAM_SPACING;
                double[] m = {muzzle[0] + px * off, muzzle[1], muzzle[2] + pz * off};
                this.spawnBeam(m, aim, target);
            }
        }
        restoreFromMuzzle(ox, oy, oz);

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                TGSounds.LASERGUN_FIRE, SoundCategory.BLOCKS, 3.0F, 1.0F);
        return true;
    }

    /** 从指定起点朝目标瞄准点射出一束激光（科技枪激光弹构造即命中，起点必须在构造前摆正） */
    private void spawnBeam(double[] muzzle, Vec3d aim, EntityLivingBase target) {
        float[] ang = aimAnglesFrom(muzzle[0], muzzle[1], muzzle[2], aim);
        alignToMuzzleForSpawn(muzzle, ang[0], ang[1]);
        LaserProjectile beam = new LaserProjectile(this.world, this,
                DAMAGE, SPEED, TTL, 0.0f, DROP_START, DROP_END, DMG_MIN, 0.0f,
                false, EnumBulletFirePos.CENTER);
        this.world.spawnEntity(beam);
        logShot(target, aim, muzzle, beam.distance);
    }
}
