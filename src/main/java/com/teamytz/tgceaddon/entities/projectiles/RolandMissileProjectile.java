package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.TGPackets;
import techguns.Techguns;
import techguns.damagesystem.TGExplosion;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.packets.PacketSpawnParticle;
import techguns.util.MathUtil;

/**
 * 罗兰防空系统指令线导弹(炮塔专用定制版)
 *
 * 相对玩家手持指令线导弹(跟随准星)的差异:
 * - 引导目标 = 炮塔锁定的攻击目标实体(非射手准星)
 * - 引导条件:炮塔(射手)到目标视线可达 → 持续转向目标;
 *   视线被遮挡/目标死亡/目标丢失 → 失导,转为惯性直线飞行(大号火箭弹),
 *   不再有任何引导能力
 * - 近炸引信:引导目标进入 3 格内直接引爆(防空弹,避免高速目标擦身而过)
 * - 爆炸威力与飞行衰减解耦:科技枪的 damageDropStart/End 同时驱动"飞行距离
 *   伤害衰减"(getDamage)与"爆炸主/次半径"(TGExplosion),若沿用同一组参数,
 *   要么爆炸半径小、要么远距离飞行伤害被削到最低值。
 *   这里重写 explodeRocket,爆炸半径用独立参数(主 6 / 次 12 格),
 *   飞行衰减区间后移到 150~300 格,保证 300 格交战距离内威力充足。
 */
public class RolandMissileProjectile extends CommandLineMissileProjectile {

    /** 转向上限:每 tick 4°(低机动防空弹,玩家绕圈机动可甩脱;原 9° 太灵活可绕玩家转圈) */
    public static final double MAX_TURN_ANGLE = 4.0 * MathUtil.D2R;
    /** 近炸引信距离(格):引导目标进入该距离内直接引爆 */
    public static final double PROXIMITY_FUSE_DIST = 3.0D;

    /** 目标实体 ID(服务端使用) */
    protected int targetEntityId = -1;  // 核弹变体近炸引信需要读取
    /** 是否处于引导状态(服务端):false = 惯性飞行 */
    private boolean guided = false;

    /** 爆炸参数(构造时保存;GenericProjectile 的同名字段包私有不可读) */
    private final float blastDamage;
    private final float blastDamageMin;
    private final float blastStart;
    private final float blastEnd;
    private final boolean blastBlockDamage;

    public RolandMissileProjectile(World worldIn) {
        super(worldIn);
        this.blastDamage = 0.0f;
        this.blastDamageMin = 0.0f;
        this.blastStart = 0.0f;
        this.blastEnd = 0.0f;
        this.blastBlockDamage = false;
    }

    /**
     * 旧签名(核弹变体使用):爆炸参数默认沿用飞行衰减参数(与科技枪原版行为一致;
     * 核弹变体重写 explodeRocket,不使用这些字段)
     */
    public RolandMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity, int targetId, boolean guided) {
        this(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage,
                leftGun, radius, gravity, targetId, guided,
                damage, dmgMin, dmgDropStart, dmgDropEnd, blockdamage);
    }

    /**
     * 完整签名:爆炸参数(主/次半径、主/次伤害、是否毁地形)独立于飞行衰减参数,
     * 由调用方(炮塔 fireMissile)显式指定
     *
     * @param targetId 引导目标实体 ID;<=0 表示发射即无引导(惯性飞行)
     * @param guided   发射时炮塔视线是否可达目标(决定初始是否引导)
     */
    public RolandMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity, int targetId, boolean guided,
            float blastDamage, float blastDamageMin, float blastStart, float blastEnd, boolean blastBlockDamage) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, gravity);
        this.targetEntityId = targetId;
        this.guided = guided;
        this.blastDamage = blastDamage;
        this.blastDamageMin = blastDamageMin;
        this.blastStart = blastStart;
        this.blastEnd = blastEnd;
        this.blastBlockDamage = blastBlockDamage;
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote) {
            // 近炸引信:引导目标进入 3 格内直接引爆(防空弹,避免高速目标擦身而过)
            Entity target = this.world.getEntityByID(this.targetEntityId);
            if (target != null && !target.isDead
                    && this.getDistanceSq(target) <= PROXIMITY_FUSE_DIST * PROXIMITY_FUSE_DIST) {
                this.explodeRocket();
                return;
            }
            updateGuidance();
        } else {
            // 客户端:全程匀速飞行(无动力段/滑行段之分)→ 全程携带持续尾焰光源
            updateMotorLight();
        }
        super.onUpdate();
        if (this.world.isRemote && this.isDead) {
            killMotorLight();
        }
    }

    /** 持续尾焰光强(14=火把级) */
    private static final int LIGHT_MOTOR = 14;
    /** 客户端:跟随本导弹的持续光源 */
    private com.teamytz.tgceaddon.entities.EntityMuzzleLight motorLight = null;

    private void updateMotorLight() {
        if (this.motorLight == null || this.motorLight.isDead) {
            this.motorLight = com.teamytz.tgceaddon.entities.EntityMuzzleLight.spawnContinuous(
                    this.world, this.posX, this.posY, this.posZ, LIGHT_MOTOR);
        } else {
            this.motorLight.setPositionAndUpdate(this.posX, this.posY, this.posZ);
            this.motorLight.renew(40);
        }
    }

    private void killMotorLight() {
        if (this.motorLight != null) {
            this.motorLight.kill();
            this.motorLight = null;
        }
    }

    /**
     * 爆炸(与科技枪 RocketProjectile.explodeRocket 相同的常规火箭爆,但用独立的爆炸半径参数):
     * - 主半径(blastStart)内满伤 blastDamage,主~次半径(blastEnd)间线性衰减到 blastDamageMin,
     *   次半径外无伤
     * - blockdamage=false 不摧毁地形(常规防空弹;核弹变体才摧毁地形)
     */
    @Override
    protected void explodeRocket() {
        if (!this.world.isRemote) {
            TGPackets.wrapper.sendToAllAround(new PacketSpawnParticle("RocketExplosion", this.posX, this.posY, this.posZ),
                    TGPackets.targetPointAroundEnt(this, 50.0f));
            TGExplosion explosion = new TGExplosion(this.world, this.shooter, this,
                    posX, posY, posZ, this.blastDamage, this.blastDamageMin, this.blastStart, this.blastEnd,
                    this.blastBlockDamage ? 0.5 : 0.0);
            explosion.blockDropChance = 0.05f;
            explosion.setDmgSrc(getProjectileDamageSource());
            explosion.doExplosion(true);
        } else {
            Techguns.proxy.createLightPulse(this.posX, this.posY, this.posZ, 5, 15, 10.0f, 1.0f, 1.0f, 0.9f, 0.5f);
        }
        this.setDead();
    }

    /**
     * 炮塔指令引导(服务端每 tick):
     * - guided 且目标存活 且 炮塔(射手)到目标视线可达 → 朝目标转向
     * - 任一条件不满足 → 失导,惯性直线飞行(大号火箭弹)
     */
    private void updateGuidance() {
        Entity target = this.world.getEntityByID(this.targetEntityId);
        boolean canGuide = this.guided && target != null && !target.isDead
                && this.shooter != null && !this.shooter.isDead
                && this.shooter.canEntityBeSeen(target);

        if (!canGuide) {
            if (this.guided && TGCEAddon.DEBUG_IR) {
                TGCEAddon.getLogger().info("[Roland导弹] 失导:目标="
                        + (target != null ? target.getName() : "null")
                        + " 视线=" + (this.shooter != null && target != null && this.shooter.canEntityBeSeen(target)));
            }
            this.guided = false;
            return; // 惯性飞行
        }

        Vec3d aim = new Vec3d(target.posX, target.posY + target.height * 0.5F, target.posZ);
        Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
        double speed = motion.lengthVector();
        if (speed <= 0.0001) {
            return;
        }
        Vec3d v2 = aim.subtract(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
        Vec3d v1 = motion.normalize();
        double dot = MathUtil.clamp(v1.dotProduct(v2), -1.0, 1.0);
        double angle = Math.acos(dot);
        Vec3d axis = v1.crossProduct(v2);
        if (angle < MAX_TURN_ANGLE) {
            motion = v2.scale(speed);
        } else if (axis.lengthVector() > 1.0E-6) {
            motion = MathUtil.rotateVector(v1, axis.normalize(), MAX_TURN_ANGLE).scale(speed);
        } else {
            motion = v2.scale(speed);
        }
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
    }
}
