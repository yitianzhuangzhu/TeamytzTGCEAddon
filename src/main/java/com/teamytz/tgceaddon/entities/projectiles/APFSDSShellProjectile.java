package com.teamytz.tgceaddon.entities.projectiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.damagesystem.DamageSystem;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * APFSDS（尾翼稳定脱壳穿甲弹）—— 普通炮塔的两种弹药之一。
 *
 * 行为：**穿透目标与普通方块，不爆炸**；单体伤害很高，弹道几乎是直线。
 *
 * 与父类 {@link GenericProjectile} 的两处根本差异（不能靠覆盖小钩子实现，必须重写）：
 * 1. **父类的 {@code onHit} 打中什么都会 {@code setDead()}** —— 打中生物立刻消失、
 *    打中方块（不走跳弹分支时）也消失，这跟"穿透"是对立的，所以整个 {@code onHit} 重写：
 *    打中生物只结算伤害、继续飞；打中方块能击穿就破坏并继续飞，击不穿才停下。
 * 2. **父类的 {@code findEntityOnPath} 会一直返回最近的那个生物** ——
 *    已经穿透过的目标会一直挡在弹道上，后面的目标和方块永远轮不到，
 *    所以重写它并**跳过已穿透的目标 id**。
 *
 * 击穿规则：
 * - 方块：硬度 ≤ {@link #MAX_BLOCK_HARDNESS} 且可破坏（硬度 ≥ 0）→ 破坏后继续飞；
 *   每发最多击穿 {@link #MAX_BLOCKS_BROKEN} 个方块（防止一发打穿整座山）。
 * - 生物：每发最多穿透 {@link #MAX_ENTITIES_PIERCED} 个，之后弹丸消失。
 * - 破坏的方块**不掉落物品**（{@link #DROP_BROKEN_BLOCKS}，避免变成刷矿手段）。
 * - 客户端**不做任何世界修改**：命中判定与破坏全部以服务端为准（客户端只负责画）。
 */
public class APFSDSShellProjectile extends TurretShellProjectile {

    // ===== 穿甲能力（调这几个数就能改手感）=====
    /** 能击穿的方块硬度上限：石头 1.5 / 圆石 2.0 / 木板 2.0 可穿；铁块 5.0 / 黑曜石 50 打不穿 */
    public static final float MAX_BLOCK_HARDNESS = 3.0F;
    /** 单发最多击穿的方块数 */
    public static final int MAX_BLOCKS_BROKEN = 6;
    /** 单发最多穿透的生物数 */
    public static final int MAX_ENTITIES_PIERCED = 4;
    /** 护甲穿透：1.0 = 完全无视护甲（穿甲弹的核心手感） */
    public static final float ARMOR_PENETRATION = 1.0F;
    /** 击穿的方块是否掉落物品（false = 不掉落） */
    public static final boolean DROP_BROKEN_BLOCKS = false;

    // ===== 弹道 =====
    /** 下坠（格/tick²）：穿甲弹初速高，默认 0 = 直线弹道；想让它也下坠就调大 */
    public static final double GRAVITY = 0.0D;

    // ===== 炮塔开火时的参考参数（供 EntityTurret 取用，避免两边数值走散）=====
    public static final float RECOMMENDED_DAMAGE = 60.0F;
    public static final float RECOMMENDED_SPEED = 6.0F;
    public static final int RECOMMENDED_TTL = 60;

    /** 本发已经穿透过的生物（实体 id）：不再重复计伤，也不再挡住弹道 */
    private final Set<Integer> piercedEntityIds = new HashSet<>();
    /** 已穿透的生物数 */
    private int piercedEntities = 0;
    /** 已击穿的方块数 */
    private int blocksBroken = 0;

    public APFSDSShellProjectile(World worldIn) {
        super(worldIn);
    }

    public APFSDSShellProjectile(World worldIn, double posX, double posY, double posZ, float yaw, float pitch,
            float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
            float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(worldIn, posX, posY, posZ, yaw, pitch, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin,
                penetration, blockdamage, firePos);
    }

    public APFSDSShellProjectile(World par2World, EntityLivingBase p, float damage, float speed, int TTL,
            float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockdamage,
            EnumBulletFirePos firePos) {
        super(par2World, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockdamage, firePos);
    }

    /** 直线弹道（穿甲弹初速高；下坠由父类统一施加，返回 0 即不下坠） */
    @Override
    protected double getShellGravity() {
        return GRAVITY;
    }

    /** 穿甲弹：伤害类型仍是子弹，但**护甲穿透拉满**、击退很小 */
    @Override
    protected TGDamageSource getProjectileDamageSource() {
        TGDamageSource src = TGDamageSource.causeBulletDamage(this, this.shooter, DeathType.GORE);
        src.armorPenetration = ARMOR_PENETRATION;
        src.goreChance = 1.0F;
        src.knockbackMultiplier = 0.5F;
        return src;
    }

    /**
     * 找弹道上的最近生物，但**跳过已经穿透过的**（否则它们会一直挡在前面）。
     * 逻辑照抄父类，只多一个 piercedEntityIds 过滤。
     */
    @Override
    @Nullable
    protected RayTraceResult findEntityOnPath(Vec3d start, Vec3d end) {
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this,
                this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D),
                BULLET_TARGETS::test);

        Entity hitEntity = null;
        double best = 0.0D;
        Vec3d hitPos = null;

        for (Entity candidate : list) {
            if (candidate == this.shooter) {
                continue;
            }
            if (this.piercedEntityIds.contains(candidate.getEntityId())) {
                continue;
            }
            RayTraceResult intercept = candidate.getEntityBoundingBox().grow(0.3D).calculateIntercept(start, end);
            if (intercept != null) {
                double dist = start.squareDistanceTo(intercept.hitVec);
                if (dist < best || best == 0.0D) {
                    hitEntity = candidate;
                    best = dist;
                    hitPos = intercept.hitVec;
                }
            }
        }

        return hitEntity != null ? new RayTraceResult(hitEntity, hitPos) : null;
    }

    /**
     * 命中处理（父类的实现会 setDead，本类要"继续飞"，所以全部重写）。
     * 客户端直接返回：世界修改只由服务端做，客户端靠同步跟上。
     */
    @Override
    protected void onHit(RayTraceResult raytraceResultIn) {
        if (this.world.isRemote) {
            return;
        }

        // ===== 打中生物：结算伤害后继续飞 =====
        if (raytraceResultIn.entityHit != null) {
            Entity victim = raytraceResultIn.entityHit;
            if (this.piercedEntityIds.contains(victim.getEntityId())) {
                return;
            }
            this.piercedEntityIds.add(victim.getEntityId());
            this.piercedEntities++;

            TGDamageSource src = this.getProjectileDamageSource();
            if (victim instanceof EntityLivingBase) {
                float dmg = DamageSystem.getDamageFactor(this.shooter, (EntityLivingBase) victim) * this.getDamage();
                if (dmg > 0.0F) {
                    victim.attackEntityFrom(src, dmg);
                }
            } else {
                victim.attackEntityFrom(src, this.getDamage());
            }

            if (this.piercedEntities >= MAX_ENTITIES_PIERCED) {
                this.setDead();
            }
            return;
        }

        // ===== 打中方块：能击穿就破坏并继续飞，击不穿就停下 =====
        if (raytraceResultIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = raytraceResultIn.getBlockPos();
            IBlockState state = this.world.getBlockState(pos);
            float hardness = state.getBlockHardness(this.world, pos);

            boolean canPierce = hardness >= 0.0F
                    && hardness <= MAX_BLOCK_HARDNESS
                    && !state.getMaterial().isLiquid()
                    && this.blocksBroken < MAX_BLOCKS_BROKEN;

            if (canPierce) {
                this.blocksBroken++;
                // 破坏方块（不掉落）：方块变成空气后，下一 tick 的射线自然继续往深处走
                this.world.destroyBlock(pos, DROP_BROKEN_BLOCKS);
                return;
            }

            // 打不穿：留个撞击特效后停止（父类的 hitBlock 只做特效与音效，不改方块）
            this.hitBlock(raytraceResultIn);
            this.setDead();
        }
    }

    /** 已穿透的生物数（调试用） */
    public int getPiercedEntities() {
        return this.piercedEntities;
    }

    /** 已击穿的方块数（调试用） */
    public int getBlocksBroken() {
        return this.blocksBroken;
    }
}
