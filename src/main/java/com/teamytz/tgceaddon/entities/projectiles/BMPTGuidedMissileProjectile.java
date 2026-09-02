package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GuidedMissileProjectile;

/**
 * BMPT 炮塔制导导弹(科技枪 GuidedMissileProjectile 的定制子类)
 *
 * 与父类完全相同的飞行/引导逻辑,额外增加动态光源:
 * - 客户端首 tick 在炮口生成短促高亮闪光(开火光效)
 * - 全程匀速飞行(无动力段/滑行段之分)→ 全程携带持续尾焰光源
 *   (OptiFine 动态光源;未装 OptiFine 时无光效,不影响飞行)
 */
public class BMPTGuidedMissileProjectile extends GuidedMissileProjectile {

    /** 持续尾焰光强(14=火把级,明显但不刺眼) */
    private static final int LIGHT_MOTOR = 14;

    /** 客户端:是否已生成过炮口闪光 */
    private boolean muzzleFlashSpawned = false;
    /** 客户端:跟随本导弹的持续光源 */
    private EntityMuzzleLight motorLight = null;

    public BMPTGuidedMissileProjectile(World worldIn) {
        super(worldIn);
    }

    public BMPTGuidedMissileProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos firePos, float radius, Entity target) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage,
                firePos, radius, target);
    }

    @Override
    public void onUpdate() {
        if (this.world.isRemote && !this.isDead) {
            // 首 tick:炮口闪光(短促高亮)
            if (!this.muzzleFlashSpawned) {
                this.muzzleFlashSpawned = true;
                EntityMuzzleLight.spawnFlash(this.world, this.posX, this.posY, this.posZ, 15, 4);
            }
            // 全程动力段:持续尾焰光源跟随导弹
            updateMotorLight();
        }
        super.onUpdate();
        if (this.world.isRemote && this.isDead) {
            killMotorLight();
        }
    }

    private void updateMotorLight() {
        if (this.motorLight == null || this.motorLight.isDead) {
            this.motorLight = EntityMuzzleLight.spawnContinuous(this.world, this.posX, this.posY, this.posZ, LIGHT_MOTOR);
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
}
