package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.util.ModParticles;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.init.SoundEvents;
import techguns.TGPackets;
import techguns.Techguns;
import techguns.damagesystem.TGDamageSource;
import techguns.damagesystem.TGExplosion;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.packets.PacketSpawnParticle;

/**
 * HE（高爆弹）—— 普通炮塔的两种弹药之一。
 *
 * 行为：命中方块或生物时**就地爆炸**，爆炸本身很小（杀伤半径小），
 * 但**实体杀伤范围很大**（次级半径 6 格内衰减杀伤），并且**有下坠**（抛物线弹道）。
 *
 * 实现要点：
 * - **爆炸外观 = 火箭弹那一套**（0.28.6）：科技枪的 `RocketExplosion` 特效包
 *   （冲击波 + 地面冲击波 + 大烟 + 火球 + 冲击环，与 Roland 导弹同一行调用）
 *   ＋原版粒子大爆团兜底（EXPLOSION_HUGE/LARGE + FLAME + 两层 SMOKE，
 *     走 {@code ModParticles} → WorldServer longDistance 广播，512 格内都看得见）
 *   ＋原版爆炸音效。
 * - 爆炸**伤害**用科技枪 {@link TGExplosion}：主半径 2 格满伤、到 **5 格**线性衰减，
 *   对每个候选生物做视线检测（躲在墙后不受伤）；破方块系数 0 = 不破坏地形。
 *   **只用它一家结算伤害**——不再叠加原版 Explosion（否则两份伤害、两次音效）。
 * - `BLOCK_DAMAGE_FACTOR = 0` → 不破坏方块（只炸生物）；想炸出弹坑就调到 0.05~0.15。
 * - 下坠、炮口烟、尾迹都在父类 {@link TurretShellProjectile} 里（与机炮炮弹同一套做法）。
 */
public class HEShellProjectile extends TurretShellProjectile {

    // ===== 爆炸参数（调这几个数就能改手感）=====
    /** 主杀伤半径（格）：半径内造成全额伤害 */
    public static final double PRIMARY_RADIUS = 2.0D;
    /** 次级杀伤半径（格）：**"实体杀伤范围很大"就是这个值**（用户要求保持 5 格），边缘处伤害降到 SECONDARY_DAMAGE */
    public static final double SECONDARY_RADIUS = 5.0D;
    /** 主杀伤伤害（主半径内的伤害，与直击伤害叠加） */
    public static final double PRIMARY_DAMAGE = 40.0D;
    /** 次级杀伤伤害（次级半径边缘的伤害，中间线性插值） */
    public static final double SECONDARY_DAMAGE = 6.0D;
    /** 破方块系数：0 = 不破坏方块（默认）；>0.01 才会破坏地形 */
    public static final double BLOCK_DAMAGE_FACTOR = 0.0D;

    // ===== 弹道 =====
    /** 每 tick 的下坠速度（格/tick²）：**"有下坠"就是这个值**；0 = 直线弹道。
     *  下坠量 ≈ GRAVITY × t² / 2（t = 飞行 tick 数），默认 0.01 → 20 tick(≈100 格) 下坠约 2 格 */
    public static final double GRAVITY = 0.01D;

    // ===== 炮塔开火时的参考参数（供 EntityTurret 取用，避免两边数值走散）=====
    public static final float RECOMMENDED_DAMAGE = 30.0F;
    public static final float RECOMMENDED_SPEED = 4.0F;
    public static final int RECOMMENDED_TTL = 60;

    /** 原版爆炸视觉兜底的粒子量/范围（火箭弹级别的爆团：内核闪光 + 火 + 两层烟） */
    private static final int FX_HUGE = 2;
    private static final int FX_LARGE = 8;
    private static final int FX_FLAME = 12;
    private static final int FX_SMOKE_LARGE = 16;
    private static final int FX_SMOKE_NORMAL = 24;
    /** 粒子位置抖动（格）与速度：爆团看着有 4~5 格宽的球体 */
    private static final double FX_SPREAD = 2.2D;
    private static final double FX_SPEED = 0.25D;

    public HEShellProjectile(World worldIn) {
        super(worldIn);
    }

    public HEShellProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch,
            float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
            float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(worldIn, posX, posY, posZ, yaw, pitch, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin,
                penetration, blockdamage, firePos);
    }

    public HEShellProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos firePos) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos);
    }

    /** 抛物线弹道（下坠由父类统一施加，客户端同样要减，见 TurretShellProjectile 注释） */
    @Override
    protected double getShellGravity() {
        return GRAVITY;
    }

    @Override
    protected TGDamageSource getProjectileDamageSource() {
        TGDamageSource src = TGDamageSource.causeExplosionDamage(this, this.shooter, DeathType.GORE);
        src.goreChance = 1.0F;
        src.knockbackMultiplier = 3.0F;
        return src;
    }

    /** 打中生物：先由父类结算直击伤害，这里再触发爆炸 */
    @Override
    protected void onHitEffect(EntityLivingBase ent, RayTraceResult raytraceResultIn) {
        explodeAt(raytraceResultIn.hitVec);
    }

    /** 打中方块：直接爆炸（父类的跳弹逻辑在 hitBlock 之后跑，此时已 setDead，不会真的跳弹） */
    @Override
    protected void hitBlock(RayTraceResult raytraceResultIn) {
        explodeAt(raytraceResultIn.hitVec);
    }

    /** 爆炸：火箭弹同款特效 + 原版爆团兜底，杀伤半径保持 5 格 */
    protected void explodeAt(Vec3d hit) {
        explodeAt(hit.x, hit.y, hit.z);
    }

    // ===== 爆炸伤害/半径取值点（子类可覆写，做成"同机制不同数值"的变体，例如 M1 主炮的 HE）=====

    /** 主半径内伤害 */
    protected double getPrimaryDamage() {
        return PRIMARY_DAMAGE;
    }

    /** 次级半径边缘伤害（中间线性插值） */
    protected double getSecondaryDamage() {
        return SECONDARY_DAMAGE;
    }

    protected double getPrimaryRadius() {
        return PRIMARY_RADIUS;
    }

    protected double getSecondaryRadius() {
        return SECONDARY_RADIUS;
    }

    protected double getBlockDamageFactor() {
        return BLOCK_DAMAGE_FACTOR;
    }

    protected void explodeAt(double x, double y, double z) {
        if (!this.world.isRemote) {
            // ===== 1. 爆炸外观（主）：科技枪的**火箭弹特效包**（与 Roland 导弹同一行调用）=====
            // RocketExplosion = 冲击波 + 地面冲击波 + 大烟 + 火球 + 冲击环
            TGPackets.wrapper.sendToAllAround(new PacketSpawnParticle("RocketExplosion", x, y, z),
                    TGPackets.targetPointAroundEnt(this, 80.0F));

            // ===== 2. 爆炸音效（原版 boom，与科技枪火箭同款音量）=====
            this.world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
                    4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

            // ===== 3. 可见性兜底：原版粒子大爆团 =====
            // 科技枪 FX 包在本整合包里不保证渲染（0.27 的教训），所以原版粒子必须有：
            // 服务端走 ModParticles → WorldServer longDistance 广播（512 格内都看得见）
            ModParticles.burst(this.world, EnumParticleTypes.EXPLOSION_HUGE, x, y, z, FX_HUGE, 0.4D, 0.0D);
            ModParticles.burst(this.world, EnumParticleTypes.EXPLOSION_LARGE, x, y, z, FX_LARGE, 1.4D, 0.0D);
            ModParticles.burst(this.world, EnumParticleTypes.FLAME, x, y, z, FX_FLAME, 1.6D, 0.08D);
            ModParticles.burst(this.world, EnumParticleTypes.SMOKE_LARGE, x, y, z, FX_SMOKE_LARGE, FX_SPREAD, 0.03D);
            ModParticles.burst(this.world, EnumParticleTypes.SMOKE_NORMAL, x, y, z, FX_SMOKE_NORMAL, FX_SPREAD * 1.4D, 0.05D);

            // ===== 4. 伤害：仍只用科技枪 TGExplosion（**5 格杀伤半径**，线性衰减 + 视线遮挡判定）=====
            // 不再叠加原版 Explosion：那会变成两份伤害、两次音效；
            // tnt 式的击退由 TGExplosion 的伤害源负责（knockbackMultiplier = 3）。
            TGExplosion blast = new TGExplosion(this.world, this.shooter, this, x, y, z,
                    getPrimaryDamage(), getSecondaryDamage(), getPrimaryRadius(), getSecondaryRadius(),
                    getBlockDamageFactor());
            blast.setDmgSrc(this.getProjectileDamageSource());
            blast.doExplosion(false);   // 音效已人工播放，别放两遍
        } else {
            Techguns.proxy.createLightPulse(x, y, z, 5, 15, 10.0F, 1.0F, 1.0F, 0.9F, 0.5F);
        }
        this.setDead();
    }
}
