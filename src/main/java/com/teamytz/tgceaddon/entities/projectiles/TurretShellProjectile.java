package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;

/**
 * 普通炮塔两种炮弹（HE / APFSDS）的公共基类：**炮口烟 + 弹道尾迹 + 下坠**。
 *
 * 实现方式与 BMPT 的机炮炮弹（{@link CannonShellProjectile}）**完全一致**，这是刻意统一的：
 * - 粒子由**弹丸实体在客户端自己生成**（`world.isRemote` + 首个 tick 一次），
 *   不走服务端广播、也不走科技枪 FX 包。BMPT 那套是实测可见的，统一到它才不会出现
 *   "炮塔发了烟但看不到"这种事。
 * - 炮口烟 = EXPLOSION_NORMAL（短命白色爆烟，为主）+ SMOKE_LARGE + 少量 SMOKE_NORMAL，
 *   沿炮管方向**加速前冲**、向四周扩散；再配一发 OptiFine 动态光源闪光。
 * - 尾迹 = SMOKE_NORMAL，每 tick 沿"上一 tick → 本 tick"的线段补几颗
 *   （BMPT 的机炮 1 格/tick，每 tick 一颗就够；本模组的炮弹 4~6 格/tick，
 *    单点会变成断断续续的点点，所以在段上插值）。
 *
 * 关于"烟存在时间更短"：原版粒子寿命是写死在粒子类里的（不能通过 spawnParticle 传参），
 * EXPLOSION_NORMAL 平均寿命最短（约 4~40 tick）、SMOKE_NORMAL/SMOKE_LARGE 最长（约 8~40 tick），
 * 所以"更短"是靠**比例**做的：以 EXPLOSION_NORMAL 为主，长寿命的 SMOKE_NORMAL 只留少量。
 * 想更短就继续把 MUZZLE_SMOKE_NORMAL 调小、MUZZLE_PUFF_COUNT 调大。
 */
public abstract class TurretShellProjectile extends GenericProjectile {

    // ===== 炮口烟（数量/扩散/前冲速度，按用户要求调过：更多、更大、更快）=====
    // 0.28.5：粒子总数由 24 提到 44（一发一团浓烟；4 秒才一发，客户端开销可忽略）
    /** 短命白爆烟颗数（主力） */
    private static final int MUZZLE_PUFF_COUNT = 26;
    /** 大烟团颗数 */
    private static final int MUZZLE_SMOKE_LARGE = 10;
    /** 小烟点颗数（寿命最长，所以最少） */
    private static final int MUZZLE_SMOKE_NORMAL = 8;
    /** 沿炮管方向的前冲速度（格/tick）：BMPT 是 0.5，这里更快 */
    private static final double MUZZLE_FORWARD = 1.6D;
    /** 横向扩散速度（格/tick）：BMPT 是 0.3，这里范围更大 */
    private static final double MUZZLE_SPREAD = 0.9D;
    /** 起点在炮口附近的抖动（格）：粒子变多后也跟着放开一点，否则会糊成一坨 */
    private static final double MUZZLE_JITTER = 0.18D;

    // ===== 弹道尾迹 =====
    /** 每 tick 在移动线段上补几颗尾迹烟 */
    private static final int TRAIL_COUNT = 3;

    /** 客户端：炮口烟是否已生成（每发只生成一次） */
    private boolean muzzleSmokeSpawned = false;
    /** 客户端：上一 tick 的烟尾迹起点（用于在移动线段上插值） */
    private double trailPrevX;
    private double trailPrevY;
    private double trailPrevZ;
    private boolean trailPrevValid = false;

    public TurretShellProjectile(World worldIn) {
        super(worldIn);
    }

    public TurretShellProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch,
            float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
            float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(worldIn, posX, posY, posZ, yaw, pitch, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin,
                penetration, blockdamage, firePos);
    }

    public TurretShellProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos firePos) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos);
    }

    /** 本种弹的下坠（格/tick²）：0 = 直线弹道。techguns 的 gravity 字段是包级私有，改不了，自己减。 */
    protected abstract double getShellGravity();

    @Override
    public void onUpdate() {
        // 炮口烟必须**在 super.onUpdate() 之前**生成：此刻实体位置还是出膛位置（炮口），
        // 之后本 tick 的位置增量已经把它推出好几格（BMPT 机炮 1 格/tick 无所谓，我们 4~6 格/tick）
        if (this.world.isRemote && !this.isDead && !this.muzzleSmokeSpawned) {
            this.muzzleSmokeSpawned = true;
            spawnMuzzleSmoke();
            EntityMuzzleLight.spawnFlash(this.world, this.posX, this.posY, this.posZ, 15, 4);
        }

        super.onUpdate();

        if (this.isDead) {
            return;
        }
        double gravity = getShellGravity();
        if (gravity != 0.0D) {
            this.motionY -= gravity;
        }
        if (this.world.isRemote) {
            spawnTrailSmoke();
        }
    }

    /**
     * 炮口烟：BMPT 机炮炮弹的同一套（客户端本地原版粒子），只是更多、扩散更大、前冲更快。
     * 方向取弹丸出膛方向的**单位向量**（不能乘整个速度：本模组炮弹 4~6 格/tick，乘上去烟会瞬间飞出视野）。
     */
    private void spawnMuzzleSmoke() {
        double len = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY
                + this.motionZ * this.motionZ);
        double ux = len > 1.0E-6D ? this.motionX / len : 0.0D;
        double uy = len > 1.0E-6D ? this.motionY / len : 0.0D;
        double uz = len > 1.0E-6D ? this.motionZ / len : 0.0D;

        for (int i = 0; i < MUZZLE_PUFF_COUNT; i++) {
            double[] p = jitteredPos();
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, p[0], p[1], p[2],
                    ux * MUZZLE_FORWARD + rand(MUZZLE_SPREAD),
                    uy * MUZZLE_FORWARD + rand(MUZZLE_SPREAD * 0.6D) + 0.05D,
                    uz * MUZZLE_FORWARD + rand(MUZZLE_SPREAD));
        }
        for (int i = 0; i < MUZZLE_SMOKE_LARGE; i++) {
            double[] p = jitteredPos();
            this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, p[0], p[1], p[2],
                    ux * MUZZLE_FORWARD * 0.7D + rand(MUZZLE_SPREAD * 0.7D),
                    uy * MUZZLE_FORWARD * 0.7D + rand(MUZZLE_SPREAD * 0.4D) + 0.05D,
                    uz * MUZZLE_FORWARD * 0.7D + rand(MUZZLE_SPREAD * 0.7D));
        }
        for (int i = 0; i < MUZZLE_SMOKE_NORMAL; i++) {
            double[] p = jitteredPos();
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, p[0], p[1], p[2],
                    ux * MUZZLE_FORWARD * 0.5D + rand(MUZZLE_SPREAD * 0.5D),
                    uy * MUZZLE_FORWARD * 0.5D + rand(MUZZLE_SPREAD * 0.3D) + 0.03D,
                    uz * MUZZLE_FORWARD * 0.5D + rand(MUZZLE_SPREAD * 0.5D));
        }
    }

    /** 弹道尾迹：沿"上一 tick → 本 tick"的线段插值补烟，高速弹丸才不会画成虚线 */
    private void spawnTrailSmoke() {
        double px = this.posX;
        double py = this.posY;
        double pz = this.posZ;
        if (!this.trailPrevValid) {
            this.trailPrevValid = true;
        } else {
            for (int i = 1; i <= TRAIL_COUNT; i++) {
                double t = (double) i / (TRAIL_COUNT + 1);
                double x = this.trailPrevX + (px - this.trailPrevX) * t;
                double y = this.trailPrevY + (py - this.trailPrevY) * t;
                double z = this.trailPrevZ + (pz - this.trailPrevZ) * t;
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z,
                        rand(0.06D), 0.01D + rand(0.03D), rand(0.06D));
            }
        }
        this.trailPrevX = px;
        this.trailPrevY = py;
        this.trailPrevZ = pz;
    }

    /** 炮口附近的随机起点 */
    private double[] jitteredPos() {
        return new double[]{
                this.posX + rand(MUZZLE_JITTER),
                this.posY + rand(MUZZLE_JITTER),
                this.posZ + rand(MUZZLE_JITTER)};
    }

    /** [-range, +range] 的随机速度分量 */
    private double rand(double range) {
        return (this.rand.nextDouble() - 0.5D) * 2.0D * range;
    }
}
