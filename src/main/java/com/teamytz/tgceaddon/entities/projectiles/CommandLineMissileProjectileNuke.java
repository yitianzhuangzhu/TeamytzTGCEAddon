package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.util.ProjectileUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import techguns.TGPackets;
import techguns.TGRadiationSystem;
import techguns.TGSounds;
import techguns.Techguns;
import techguns.api.damagesystem.DamageType;
import techguns.damagesystem.TGDamageSource;
import techguns.damagesystem.TGExplosion;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.special.EntityRadiation;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.packets.PacketSpawnParticle;

/**
 * 玩家惯性(指令线)核导弹
 *
 * 与普通指令线导弹相同的引导机制(跟随玩家鼠标准星),但命中后触发科技枪核爆:
 * - NukeExplosion 粒子 + 核爆音效 + 大范围 TGExplosion(摧毁地形)+ 辐射
 * - 完全模仿科技枪 RocketProjectileNuke 的爆炸逻辑
 *
 * 通过弹药变体选择:发射器装上核火箭(ROCKET_NUKE)即发射本变体,普通火箭发射普通导弹。
 * 注意:GenericProjectile 的 damage 等字段包私有,爆炸参数构造时保存为本地字段。
 */
public class CommandLineMissileProjectileNuke extends CommandLineMissileProjectile {

    /** 核爆参数(构造时保存;GenericProjectile 同名字段包私有不可读) */
    private final float nukeDamage;
    private final float nukeDamageMin;
    private final float nukeDropStart;
    private final float nukeDropEnd;
    private final boolean nukeBlockDamage;

    public CommandLineMissileProjectileNuke(World worldIn) {
        super(worldIn);
        this.nukeDamage = 0.0f;
        this.nukeDamageMin = 0.0f;
        this.nukeDropStart = 0.0f;
        this.nukeDropEnd = 0.0f;
        this.nukeBlockDamage = false;
    }

    public CommandLineMissileProjectileNuke(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos leftGun, float radius, double gravity) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, leftGun,
                radius, gravity);
        this.nukeDamage = damage;
        this.nukeDamageMin = dmgMin;
        this.nukeDropStart = dmgDropStart;
        this.nukeDropEnd = dmgDropEnd;
        this.nukeBlockDamage = blockdamage;
    }

    /** 核爆(模仿科技枪 RocketProjectileNuke.explodeRocket) */
    @Override
    protected void explodeRocket() {
        if (!this.world.isRemote) {
            TGPackets.wrapper.sendToAllAround(new PacketSpawnParticle("NukeExplosion", this.posX, this.posY, this.posZ),
                    TGPackets.targetPointAroundEnt(this, 150.0f));
            TGExplosion explosion = new TGExplosion(this.world, this.shooter, this,
                    posX, posY, posZ, this.nukeDamage, this.nukeDamageMin, this.nukeDropStart, this.nukeDropEnd,
                    this.nukeBlockDamage ? 0.5 : 0.0);
            explosion.blockDropChance = 0.05f;
            explosion.setDmgSrc(getProjectileDamageSource());
            explosion.doExplosion(false);
            this.world.playSound(null, this.posX, this.posY, this.posZ,
                    TGSounds.NUKE_EXPLOSION, SoundCategory.BLOCKS, 4.0F, 1.0F);
            if (TGRadiationSystem.isEnabled()) {
                EntityRadiation rad = new EntityRadiation(this, 20 * 60, this.nukeDropStart, 9, this.nukeDropEnd, 2);
                this.world.spawnEntity(rad);
            }
        } else {
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

    public static class Factory implements IProjectileFactory<CommandLineMissileProjectileNuke> {

        @Override
        public CommandLineMissileProjectileNuke createProjectile(GenericGun gun, World world, EntityLivingBase p,
                float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                float penetration, boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity) {
            // 核弹:伤害/射程/半径 5 倍,摧毁地形(模仿科技枪核弹修饰)
            CommandLineMissileProjectileNuke proj = new CommandLineMissileProjectileNuke(world, p,
                    damage * 5f, speed, TTL, spread,
                    dmgDropStart * 5f, dmgDropEnd * 5f, dmgMin * 5f, penetration, true,
                    firePos, radius * 5f, gravity);
            // 高速移动中发射:叠加射手速度
            ProjectileUtil.applyShooterVelocity(proj, p);
            return proj;
        }

        @Override
        public DamageType getDamageType() {
            return DamageType.EXPLOSION;
        }
    }
}
