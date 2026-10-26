package com.teamytz.tgceaddon.entities.projectiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.LaserProjectile;

/**
 * 会爆炸的激光光束（激光炮塔 Mk.1 专用，0.25.3 新增）。
 *
 * 与科技枪原版激光弹完全相同（贴图/特效/渲染全部沿用科技枪 {@link LaserProjectile}），
 * 唯一区别：**命中方块或实体时，在命中点产生一个小型爆炸**。
 *
 * 爆炸参数：强度 {@link #HIT_EXPLOSION_STRENGTH}，**不破坏方块**（isSmashing=false）、不点火；
 * 伤害与击退由原版爆炸结算，爆炸源传入射手（炮塔）以保证击杀归属正确。
 * 炮塔实体本身是 invulnerable 的，不会被自己的爆炸伤到。
 *
 * ⚠ 实现注意：科技枪激光是"构造即命中"（{@code AbstractBeamProjectile.trace()} 在
 * {@code GenericProjectile} 的构造器里就执行并回调 {@link #onHit}），
 * **因此本方法会在本类字段初始化之前被调用** —— 只允许使用 super 已就绪的状态
 * （{@code world} / {@code shooter} / {@code posX..}）与 static 常量，不要读实例字段。
 */
public class TurretLaserBeamProjectile extends LaserProjectile {

    /** 命中爆炸强度（原版语义：半径约 2×strength 格的小爆炸；不破坏方块） */
    public static final float HIT_EXPLOSION_STRENGTH = 1.5F;

    /** 破坏方块时的最大硬度阈值：只破坏"徒手就能挖"的软方块（泥土 0.5 / 沙子 0.5 / 树叶 0.2 / 玻璃 0.3 …） */
    public static final float SMASH_MAX_HARDNESS = 1.0F;

    public TurretLaserBeamProjectile(World worldIn) {
        super(worldIn);
    }

    public TurretLaserBeamProjectile(World world, EntityLivingBase shooter, float damage, float speed, int TTL,
                                     float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                                     float penetration, boolean blockdamage, EnumBulletFirePos firePos) {
        super(world, shooter, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration,
                blockdamage, firePos);
    }

    /** 命中爆炸强度（子类覆写返回常量；构造期调用安全） */
    protected float getHitExplosionStrength() {
        return HIT_EXPLOSION_STRENGTH;
    }

    /** 命中爆炸是否破坏软方块（子类覆写返回常量；构造期调用安全） */
    protected boolean shouldSmashBlocks() {
        return false;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        // 先走科技枪原逻辑：直击伤害（能量伤害）+ 命中特效
        super.onHit(result);

        if (this.world == null || this.world.isRemote || result == null) {
            return;
        }
        Vec3d hit = result.hitVec != null
                ? result.hitVec
                : new Vec3d(this.posX, this.posY, this.posZ);
        float strength = getHitExplosionStrength();
        // 原版爆炸：不点火；是否破坏方块由本类按硬度自行筛选（isSmashing 传 false）
        this.world.newExplosion(this.shooter, hit.x, hit.y, hit.z, strength, false, false);
        if (shouldSmashBlocks()) {
            smashSoftBlocks(hit, strength);
        }
    }

    /**
     * 破坏命中点附近"徒手可挖"的软方块（硬度 ≤ {@link #SMASH_MAX_HARDNESS}）。
     * 不用原版 isSmashing=true 的原因：那会按爆炸抗性破坏石头等硬方块，无法限制成"只破软方块"。
     * 破坏不掉落物品（与爆炸一致，避免被当成刷资源手段）。
     */
    protected void smashSoftBlocks(Vec3d hit, float strength) {
        int r = Math.max(1, (int) Math.ceil(strength));
        BlockPos center = new BlockPos(hit.x, hit.y, hit.z);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-r, -r, -r), center.add(r, r, r))) {
            double dx = pos.getX() + 0.5D - hit.x;
            double dy = pos.getY() + 0.5D - hit.y;
            double dz = pos.getZ() + 0.5D - hit.z;
            if (dx * dx + dy * dy + dz * dz > (double) r * r) {
                continue;
            }
            IBlockState state = this.world.getBlockState(pos);
            float hardness = state.getBlockHardness(this.world, pos);
            if (hardness < 0.0F || hardness > SMASH_MAX_HARDNESS) {
                continue;                 // 不可破坏 或 太硬（石头 1.5 / 原木 2.0 …）→ 跳过
            }
            if (state.getMaterial().isLiquid()) {
                continue;
            }
            this.world.destroyBlock(pos, false);
        }
    }
}
