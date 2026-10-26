package com.teamytz.tgceaddon.entities.projectiles;

import com.teamytz.tgceaddon.entities.BeamBurnTracker;
import com.teamytz.tgceaddon.item.weapon.mode.GunMode;
import com.teamytz.tgceaddon.item.weapon.mode.GunModeFunction;
import com.teamytz.tgceaddon.item.weapon.mode.IModeSwitchable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import techguns.api.damagesystem.DamageType;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.NDRProjectile;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;

/**
 * 带档位的光束弹丸（{@link NDRProjectile} 子类：每 tick 重新对准射手并重新射线，见其 onUpdate）。
 *
 * <p>三档效果全部由 {@link GunMode#function} 分支决定，伤害完全自己算
 * ——**不调用父类的 onHit**，因此科技枪那套通用弹伤（以及 NDR 的辐射伤害）都不会发生。</p>
 *
 * <p>命中方块的路径仍走父类（{@code hitBlock -> doImpactEffects}，为了保留命中粒子/音效），
 * 父类在那里会 {@code setDead()}，但 {@code AbstractBeamProjectile.trace()} 会紧接着把
 * {@code isDead} 复位，所以光束不会提前消失。</p>
 */
public class ModeBeamProjectile extends NDRProjectile {

    /** 本次射击携带的档位。客户端（无 spawn data 携带）可能为 null，效果只在服务端结算。 */
    protected GunMode mode;

    /**
     * 破甲值。父类 {@code GenericProjectile.penetration} 是**包私有**字段，跨包读不到，
     * 所以自己存一份（工厂会把弹丸的 penetration 参数传进来）。
     */
    protected float armorPenetration;

    public ModeBeamProjectile(World worldIn) {
        super(worldIn);
    }

    public ModeBeamProjectile(World world, EntityLivingBase shooter, float damage, float speed, int TTL,
                              float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                              float penetration, boolean blockdamage, EnumBulletFirePos firePos, GunMode mode) {
        super(world, shooter, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration,
                blockdamage, firePos);
        this.mode = mode;
        this.armorPenetration = penetration;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (result == null) {
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            if (!this.world.isRemote && result.entityHit instanceof EntityLivingBase) {
                this.applyModeToEntity((EntityLivingBase) result.entityHit);
            }
            return;   // 不打科技枪的通用弹伤；也不 setDead —— 光束要继续跟着准星
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (!this.world.isRemote) {
                this.burnBlock(result.getBlockPos());
            }
            super.onHit(result);   // 命中特效（父类内部会 setDead，trace() 立刻复位）
        }
    }

    // ===== 实体 =====

    private void applyModeToEntity(EntityLivingBase target) {
        if (this.mode == null || this.shooter == null || target == this.shooter) {
            return;
        }
        if (target.isDead) {
            return;
        }

        TGDamageSource src = TGDamageSource.causeEnergyDamage(this, this.shooter, DeathType.LASER);
        src.armorPenetration = this.armorPenetration;
        src.setNoKnockback();

        switch (this.mode.function) {
            case STUN:
                if (this.mode.hitDamage > 0.0f) {
                    target.attackEntityFrom(src, this.mode.hitDamage);
                }
                // 三个效果每次命中都刷新时长，所以离开光束后还会残留一小段
                target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,
                        this.mode.stunDuration, this.mode.stunAmplifier, false, true));
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,
                        this.mode.stunDuration, this.mode.stunAmplifier, false, true));
                target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,
                        this.mode.stunDuration, this.mode.stunAmplifier, false, true));
                break;

            case KILL: {
                float max = target.getMaxHealth();
                if (max <= 0.0f) {
                    return;
                }
                float health = target.getHealth();
                if (health <= max * this.mode.killExecuteThreshold) {
                    // 处决：打一发远超其生命的伤害（护甲减伤上限 80%，100 倍生命必然溢出致死）。
                    // 不去动受击无敌帧——1.12.2 的 MCP 映射里没有 hurtResistantTime 这个字段名，
                    // 而且光束每 tick 都在命中，被无敌帧吃掉一次下一 tick 还会再来。
                    target.attackEntityFrom(src, health * 100.0f + 1000.0f);
                } else {
                    float dmg = max * this.mode.killHealthPercent + this.mode.hitDamage;
                    if (dmg > 0.0f) {
                        target.attackEntityFrom(src, dmg);
                    }
                }
                break;
            }

            case MINE:
            default:
                if (this.mode.hitDamage > 0.0f) {
                    target.attackEntityFrom(src, this.mode.hitDamage);
                }
                break;
        }
    }

    // ===== 方块 =====

    private void burnBlock(BlockPos pos) {
        if (this.mode == null || this.mode.function != GunModeFunction.MINE) {
            return;
        }
        if (!(this.shooter instanceof EntityPlayer)) {
            return;   // 只给玩家记账（破块需要 PlayerMP，见 BeamBurnTracker）
        }
        IBlockState state = this.world.getBlockState(pos);
        if (state.getMaterial().isLiquid()) {
            return;
        }
        float hardness = state.getBlockHardness(this.world, pos);
        if (hardness < 0.0f) {
            return;   // 不可破坏（基岩等）
        }
        if (this.mode.mineMaxHardness >= 0.0f && hardness > this.mode.mineMaxHardness) {
            return;   // 超过本档硬度上限
        }
        BeamBurnTracker.hit(this.world, (EntityPlayer) this.shooter, pos, state,
                hardness, this.mode.burnPower, this.mode.mineDropBlocks);
    }

    // ===== 工厂：创建时从射手手里的枪反查当前档位 =====

    public static class Factory implements IProjectileFactory<ModeBeamProjectile> {

        @Override
        public ModeBeamProjectile createProjectile(GenericGun gun, World world, EntityLivingBase shooter,
                                                   float damage, float speed, int TTL, float spread,
                                                   float dmgDropStart, float dmgDropEnd, float dmgMin,
                                                   float penetration, boolean blockdamage,
                                                   EnumBulletFirePos firePos, float radius, double gravity) {
            GunMode mode = resolveMode(gun, shooter);
            // 注意：不要用传进来的 TTL —— 它是 getScaledTTL()（ticksToLive/speed，光束武器会被算成 1）。
            // 光束寿命必须用固定值，才能「每 tick 重新对准射手」并在射速间隔内保持不断。
            return new ModeBeamProjectile(world, shooter, damage, speed, NDRProjectile.BEAM_LIFETIME,
                    spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos, mode);
        }

        @Override
        public DamageType getDamageType() {
            return DamageType.ENERGY;
        }
    }

    /**
     * 反查射手手里这把枪的当前档位。
     *
     * <p>弹丸工厂拿不到「哪只手开的枪」，所以主手优先、再看副手；
     * 手上没有这把枪时返回 null（效果档位缺省 → 只飞一条无副作用的光束）。</p>
     */
    public static GunMode resolveMode(GenericGun gun, EntityLivingBase shooter) {
        if (!(shooter instanceof EntityPlayer) || !(gun instanceof IModeSwitchable)) {
            return null;
        }
        EntityPlayer player = (EntityPlayer) shooter;
        IModeSwitchable switchable = (IModeSwitchable) gun;

        ItemStack main = player.getHeldItemMainhand();
        if (!main.isEmpty() && main.getItem() == gun) {
            return switchable.getMode(main);
        }
        ItemStack off = player.getHeldItemOffhand();
        if (!off.isEmpty() && off.getItem() == gun) {
            return switchable.getMode(off);
        }
        return null;
    }
}
