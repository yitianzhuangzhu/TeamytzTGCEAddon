package com.teamytz.tgceaddon.entities.projectiles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import techguns.TGPackets;
import techguns.Techguns;
import techguns.api.damagesystem.DamageType;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.items.guns.ammo.DamageModifier;
import techguns.packets.PacketSpawnParticle;

public class BoltProjectile extends GenericProjectile {

    public BoltProjectile(World worldIn) {
        super(worldIn);
    }

    public BoltProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch,
            float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
            float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(worldIn, posX, posY, posZ, yaw, pitch, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration,
                blockdamage, firePos);
    }

    public BoltProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos firePos) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos);
    }
    
    @Override
    protected TGDamageSource getProjectileDamageSource() {
        TGDamageSource src = TGDamageSource.causeExplosionDamage(this, this.shooter, DeathType.GORE);
        src.goreChance = 1.0f;
        src.knockbackMultiplier = 3.0f;
        return src;
    }

    @Override
    protected void onHitEffect(EntityLivingBase ent, RayTraceResult raytraceResultIn) {
        // 撞击时爆炸
        this.explode(raytraceResultIn.hitVec.x, raytraceResultIn.hitVec.y, raytraceResultIn.hitVec.z);
    }

    @Override
    protected void hitBlock(RayTraceResult raytraceResultIn) {
        // 击中方块时爆炸
        this.explode(raytraceResultIn.hitVec.x, raytraceResultIn.hitVec.y, raytraceResultIn.hitVec.z);
    }
    
    protected void explode(double x, double y, double z) {
        if (!this.world.isRemote) {
            // 发送爆炸粒子效果
            TGPackets.wrapper.sendToAllAround(new PacketSpawnParticle("MiningChargeBlockExplosion", x, y, z), 
                TGPackets.targetPointAroundEnt(this, 100.0f));
            
            // 创建爆炸 - 小型爆炸，不破坏方块
            Explosion exp = new Explosion(world, this, x, y, z, 1.0f, false, false);
            exp.doExplosionA();
            exp.doExplosionB(false);
        } else {
            // 客户端播放闪光效果
            Techguns.proxy.createLightPulse(x, y, z, 5, 15, 3.0f, 0.5f, 1f, 0.9f, 0.5f);
        }
        this.setDead();
    }
    
    public static class Factory implements IProjectileFactory<BoltProjectile> {
        
        protected DamageModifier mod = new DamageModifier().setDmg(1.15f, 0f);
        
        @Override
        public DamageModifier getDamageModifier() {
            return mod;
        }
        
        @Override
        public BoltProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p,
                float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                float penetration, boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity) {
            return new BoltProjectile(world, p, mod.getDamage(damage), speed, TTL, spread, 
                dmgDropStart, dmgDropEnd, mod.getDamage(dmgMin), penetration, blockdamage, firePos);
        }

        @Override
        public DamageType getDamageType() {
            return DamageType.EXPLOSION;
        }
    }
}
