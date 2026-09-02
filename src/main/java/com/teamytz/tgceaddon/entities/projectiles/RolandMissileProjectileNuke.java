package com.teamytz.tgceaddon.entities.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import techguns.TGPackets;
import techguns.TGRadiationSystem;
import techguns.TGSounds;
import techguns.Techguns;
import techguns.damagesystem.TGDamageSource;
import techguns.damagesystem.TGExplosion;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.special.EntityRadiation;
import techguns.packets.PacketSpawnParticle;

/**
 * 罗兰核导弹(核火箭变体)
 *
 * 与罗兰普通导弹相同的指令引导/失导惯性机制,但命中后触发科技枪核爆:
 * - NukeExplosion 粒子 + 核爆音效 + 大范围 TGExplosion(摧毁地形)+ 辐射
 * - 完全模仿科技枪 RocketProjectileNuke 的爆炸逻辑
 *
 * 注意:GenericProjectile 的 damage/damageMin/damageDropStart/damageDropEnd 是包私有字段,
 * 跨包子类无法读取,因此爆炸参数在构造时保存为本地字段。
 */
public class RolandMissileProjectileNuke extends RolandMissileProjectile {

    /** 近炸引信距离(格):引导目标进入该距离内直接引爆 */
    public static final double PROXIMITY_FUSE_DIST = 3.0D;

    /** 核爆参数(构造时保存;GenericProjectile 的同名字段包私有不可读) */
    private final float nukeDamage;
    private final float nukeDamageMin;
    private final float nukeDropStart;
    private final float nukeDropEnd;
    private final boolean nukeBlockDamage;

    public RolandMissileProjectileNuke(World worldIn) {
        super(worldIn);
        this.nukeDamage = 0.0f;
        this.nukeDamageMin = 0.0f;
        this.nukeDropStart = 0.0f;
        this.nukeDropEnd = 0.0f;
        this.nukeBlockDamage = false;
    }

    public RolandMissileProjectileNuke(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity, int targetId, boolean guided) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, gravity, targetId, guided);
        this.nukeDamage = damage;
        this.nukeDamageMin = dmgMin;
        this.nukeDropStart = dmgDropStart;
        this.nukeDropEnd = dmgDropEnd;
        this.nukeBlockDamage = blockdamage;
    }

    /** 近炸引信:引导目标进入 3 格内直接引爆(防空核弹,避免擦身而过) */
    @Override
    public void onUpdate() {
        if (!this.world.isRemote) {
            Entity target = this.world.getEntityByID(this.targetEntityId);
            if (target != null && !target.isDead
                    && this.getDistanceSq(target) <= PROXIMITY_FUSE_DIST * PROXIMITY_FUSE_DIST) {
                this.explodeRocket();
                return;
            }
        }
        super.onUpdate();
    }

    /** 核爆(模仿科技枪 RocketProjectileNuke.explodeRocket) */
    @Override
    protected void explodeRocket() {
        if (!this.world.isRemote) {
            // 核爆粒子(大范围可见)
            TGPackets.wrapper.sendToAllAround(new PacketSpawnParticle("NukeExplosion", this.posX, this.posY, this.posZ),
                    TGPackets.targetPointAroundEnt(this, 150.0f));
            // 大范围爆炸(半径 = max(dropStart, dropEnd),可摧毁地形)
            TGExplosion explosion = new TGExplosion(this.world, this.shooter, this,
                    posX, posY, posZ, this.nukeDamage, this.nukeDamageMin, this.nukeDropStart, this.nukeDropEnd,
                    this.nukeBlockDamage ? 0.5 : 0.0);
            explosion.blockDropChance = 0.05f;
            explosion.setDmgSrc(getProjectileDamageSource());
            explosion.doExplosion(false);
            // 核爆音效
            this.world.playSound(null, this.posX, this.posY, this.posZ,
                    TGSounds.NUKE_EXPLOSION, SoundCategory.BLOCKS, 4.0F, 1.0F);
            // 辐射(科技枪辐射系统开启时)
            if (TGRadiationSystem.isEnabled()) {
                EntityRadiation rad = new EntityRadiation(this, 20 * 60, this.nukeDropStart, 9, this.nukeDropEnd, 2);
                this.world.spawnEntity(rad);
            }
        } else {
            // 客户端:核爆光脉冲
            Techguns.proxy.createLightPulse(this.posX, this.posY, this.posZ, 5, 80, 80.0f, 1.0f, 1f, 0.9f, 0.5f);
        }
        this.setDead();
    }

    @Override
    protected TGDamageSource getProjectileDamageSource() {
        TGDamageSource dmgsrc = TGDamageSource.causeExplosionDamage(this, this.shooter, DeathType.LASER);
        dmgsrc.goreChance = 1.0f;
        dmgsrc.knockbackMultiplier = 3.0f;
        return dmgsrc;
    }
}
