package com.teamytz.tgceaddon.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.APFSDSShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.HEShellProjectile;
import com.teamytz.tgceaddon.init.ModItems;
import com.teamytz.tgceaddon.init.ModSounds;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;

/**
 * 基础炮塔（资产目录 models/Turret/turret，贴图 turret.png，256x256）。
 *
 * 武器 = **自己的炮管发射 HE / APFSDS 两种炮弹**（0.28.0 实现）：
 * - 弹种**由弹药栏里实际有的东西决定**：有 APFSDS 就打 APFSDS（穿甲、单体高伤、直线弹道），
 *   只有 HE 才打 HE（爆炸、杀伤半径大、有下坠）。两种都没有 → 不开火。
 * - 射击间隔 {@link #FIRE_COOLDOWN} = 80 tick = **4 秒一发**。
 * - 每次射击消耗 1 发炮弹 + {@link #POWER_PER_SHOT} RF（与 BMPT 机炮同口径：
 *   基座没电不开火，见基类 {@code canFire()}）。
 * - 开火时播音效（本模组 cannon_fire，与 BMPT 机炮同一个声）。
 *   **炮塔本身不喷烟**：炮口烟与弹道尾迹由弹丸实体在客户端自己生成，
 *   与 BMPT 机炮炮弹（{@code CannonShellProjectile}）完全同一套做法，
 *   见 {@code TurretShellProjectile}（0.28.3 统一；炮塔侧的喷烟代码已删除）。
 *
 * 炮口位置（由 bbmodel 求出，方法见 TURRET_GUIDE.md §5）：
 * 炮管前端面在俯仰分组 gun 的局部坐标 z=-58、炮膛中心 x=0 / y=-1，
 * gun 的旋转点 Java 绝对坐标 (0, 15, 0) → 炮口 Java 模型空间 = **(0, 14, -58)**（炮管朝 -Z）。
 *
 * 弹道修正：HE 有下坠（{@link HEShellProjectile#GRAVITY}），按飞行时间把瞄准点抬高
 * （弹道超升，否则远距离弹着点偏低）。APFSDS 直线弹道不修正。
 */
public class EntityTurret extends EntityBaseTurret {

    /** 炮塔类型标识（对应卡片 NBT turretType） */
    public static final String TYPE = "turret";

    /** 索敌/开火射程（格）：坦克主炮 */
    public static final double ATTACK_RANGE = 64.0D;

    /** 射击间隔（tick）：**4 秒一发**（20 tps × 4s） */
    public static final int FIRE_COOLDOWN = 80;

    /** 开火前要求"瞄准保持"的 tick 数：对准并可见连续这么久才开炮（去掉甩狙观感） */
    public static final int AIM_SETTLE_TICKS = 5;

    /** 每次射击消耗的 RF（与 BMPT 机炮同口径；基座没电则不开火） */
    public static final int POWER_PER_SHOT = 10;

    /** 炮口（Java 模型空间像素）：炮管前端面 z=-58、炮膛中心相对 gun 旋转点 (0, -1, -58)
     *  → gun 旋转点 Java (0, 15, 0)，故炮口绝对坐标 (0, 14, -58) */
    private static final float[] MUZZLE_PX = {0.0F, 14.0F, -58.0F};
    /** 俯仰旋转中心 = 俯仰分组 gun 的旋转点（Java 模型空间） */
    private static final float[] PIVOT_PX = {0.0F, 15.0F, 0.0F};

    /** 散布：HE 略有散布，APFSDS 精确直射（传 0 表示无散布，命中率优先） */
    private static final float HE_SPREAD = 0.01F;
    private static final float APFSDS_SPREAD = 0.0F;

    /** 伤害不随距离衰减（传 0 = 无衰减，与 GenericProjectile.getDamage 的约定一致） */
    private static final float DROP_START = 0.0F;
    private static final float DROP_END = 0.0F;
    private static final float DROP_MIN = 0.0F;

    /** HE 弹道超升：按飞行时间预估下坠量并把瞄准点抬高（关掉就是"按直线瞄、实际打低"） */
    private static final boolean COMPENSATE_HE_DROP = true;

    /** "不开火"日志的最小间隔（tick）：5 秒最多一条，避免刷屏 */
    private static final int NO_FIRE_LOG_INTERVAL = 100;

    /** 上次打"不开火"日志的 tick（初值 -1000，保证第一次失败就能立刻打出来） */
    private long lastNoFireLogTick = -1000L;

    public EntityTurret(World world) {
        super(world, TYPE);
    }

    public EntityTurret(World world, BlockPos masterPos, EnumFacing facing) {
        super(world, masterPos, facing, TYPE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(ATTACK_RANGE);
    }

    @Override
    protected double getAttackRange() {
        return ATTACK_RANGE;
    }

    /** 4 秒一发 */
    @Override
    protected int getFireCooldown() {
        return FIRE_COOLDOWN;
    }

    /**
     * 开火前要求瞄准保持 {@link #AIM_SETTLE_TICKS} tick（5 tick = 0.25 秒）：
     * 转向是限速的，刚进对准容差时炮口还在动，立刻开火看着像"甩狙"；
     * 蓄满 5 tick 再打，观感是"停稳后开炮"。（其它炮塔默认 0 = 不改变原有手感）
     */
    @Override
    protected int getAimSettleTicks() {
        return AIM_SETTLE_TICKS;
    }

    /** 俯仰限幅（用户给定）：俯角 25°、仰角 50°；俯仰分组 = gun（模型已按 --pitch gun 生成） */
    @Override
    protected float getPitchMaxDown() {
        return 25.0F;
    }

    @Override
    protected float getPitchMaxUp() {
        return 50.0F;
    }

    // ==================================================================
    // 开火
    // ==================================================================

    @Override
    protected boolean tryFire(EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            logNoFire("基座未成形");
            return false;
        }

        // ===== 弹种由弹药栏里实际有的东西决定（先查弹药再扣电，避免没弹药时白耗电）=====
        ItemStack apfsds = new ItemStack(ModItems.apfsdsShell);
        ItemStack he = new ItemStack(ModItems.heShell);
        boolean hasApfsds = master.hasAmmo(apfsds);
        boolean hasHe = master.hasAmmo(he);
        if (!hasApfsds && !hasHe) {
            // 弹药栏里没有本炮塔的弹药 → 不开火（最常见的原因：弹药还没放进基座）
            logNoFire("弹药栏里没有 HE / APFSDS 弹药（把炮弹放进基座弹药槽 0-8）");
            return false;
        }
        boolean useApfsds = hasApfsds;          // 两种都有时优先穿甲弹（单体伤害更高）
        ItemStack chosen = useApfsds ? apfsds : he;

        if (!master.consumeTurretPower(POWER_PER_SHOT)) {
            logNoFire("电量不足（本发需 " + POWER_PER_SHOT + " RF，当前 "
                    + master.getEnergyStorage().getEnergyStored() + " RF）");
            return false;
        }
        if (!master.consumeAmmo(chosen)) {
            logNoFire("扣弹药失败（弹药栏里的炮弹被别的东西占用了？）");
            return false;
        }

        // ===== 炮口 + 从炮口看向瞄准点的角度（逐炮口算角度，命中率才够）=====
        float speed = useApfsds ? APFSDSShellProjectile.RECOMMENDED_SPEED : HEShellProjectile.RECOMMENDED_SPEED;
        int ttl = useApfsds ? APFSDSShellProjectile.RECOMMENDED_TTL : HEShellProjectile.RECOMMENDED_TTL;
        float damage = useApfsds ? APFSDSShellProjectile.RECOMMENDED_DAMAGE : HEShellProjectile.RECOMMENDED_DAMAGE;

        double[] muzzle = muzzlePos(MUZZLE_PX, PIVOT_PX);
        Vec3d aim = aimPointOf(target);
        if (!useApfsds && COMPENSATE_HE_DROP) {
            aim = raiseAimForDrop(aim, muzzle, speed);
        }
        float[] ang = aimAnglesFrom(muzzle[0], muzzle[1], muzzle[2], aim);

        // GenericProjectile 的构造器读 shooter.rotationYawHead / rotationPitch 决定初速方向，
        // 所以构造前临时写回"炮口→瞄准点"的角度，构造后立刻复位（渲染用补间角，别污染本 tick）
        float keepYawHead = this.rotationYawHead;
        float keepPitch = this.rotationPitch;
        this.rotationYawHead = ang[0];
        this.rotationPitch = ang[1];
        GenericProjectile shell = useApfsds
                ? new APFSDSShellProjectile(this.world, this, damage, speed, ttl, APFSDS_SPREAD,
                        DROP_START, DROP_END, DROP_MIN, 0.0F, false, EnumBulletFirePos.CENTER)
                : new HEShellProjectile(this.world, this, damage, speed, ttl, HE_SPREAD,
                        DROP_START, DROP_END, DROP_MIN, 0.0F, false, EnumBulletFirePos.CENTER);
        this.rotationYawHead = keepYawHead;
        this.rotationPitch = keepPitch;

        // 弹丸从炮口出膛（构造器给的起点是射手眼睛位置，这里改成炮口）
        shell.setPosition(muzzle[0], muzzle[1], muzzle[2]);
        this.world.spawnEntity(shell);

        // 炮口烟/尾迹由弹丸自己在客户端生成（与 BMPT 机炮炮弹同一套做法，见 TurretShellProjectile）
        playFireSound();

        if (DEBUG_TURRET) {
            double dist = Math.sqrt(sq(aim.x - muzzle[0]) + sq(aim.y - muzzle[1]) + sq(aim.z - muzzle[2]));
            TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType + " 开火 弹种="
                    + (useApfsds ? "APFSDS" : "HE")
                    + " 目标=" + target.getName()
                    + " 炮口→瞄准点=" + String.format("%.2f", dist));
        }
        return true;
    }

    /**
     * HE 弹道超升：按"炮口到瞄准点的距离 / 弹速"预估飞行时间，
     * 把瞄准点抬高 {@code 0.5 × g × t²}（就是这发弹在下坠作用下会掉的高度）。
     * 不做这一步，HE 在远距离会整体打低（64 格约低 1.3 格）。
     */
    private Vec3d raiseAimForDrop(Vec3d aim, double[] muzzle, float speed) {
        double dist = Math.sqrt(sq(aim.x - muzzle[0]) + sq(aim.y - muzzle[1]) + sq(aim.z - muzzle[2]));
        double t = speed > 0.0F ? dist / speed : 0.0D;
        double drop = 0.5D * HEShellProjectile.GRAVITY * t * t;
        return new Vec3d(aim.x, aim.y + drop, aim.z);
    }

    private static double sq(double v) {
        return v * v;
    }

    /** 炮响：沿用 BMPT 机炮的开火音（本模组自带的 cannon_fire） */
    private void playFireSound() {
        this.world.playSound(null, this.posX, this.posY, this.posZ,
                ModSounds.CANNON_FIRE, SoundCategory.BLOCKS, 5.0F, 0.9F + this.world.rand.nextFloat() * 0.2F);
    }

    /**
     * 打一条"为什么不开火"的日志（每 {@link #NO_FIRE_LOG_INTERVAL} tick 最多一条）。
     * 弹药/电量这两层门控在基类的 canFire() 里看不到，没这行日志时
     * 日志会表现为"锁定了目标但永远不开火"，无从判断原因。
     */
    private void logNoFire(String reason) {
        if (!DEBUG_TURRET) {
            return;
        }
        if (this.ticksExisted - this.lastNoFireLogTick < NO_FIRE_LOG_INTERVAL) {
            return;
        }
        this.lastNoFireLogTick = this.ticksExisted;
        TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType + " 不开火：" + reason);
    }

    /** 心跳日志里的弹药栏内容（看日志就知道"有没有弹药、各多少发"） */
    @Override
    protected String describeAmmo() {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null) {
            return "";
        }
        int he = 0, apfsds = 0;
        for (int i = TurretBaseTileEntMaster.SLOT_INPUT1;
                i < TurretBaseTileEntMaster.SLOT_INPUT1 + TurretBaseTileEntMaster.INPUTS_SIZE; i++) {
            ItemStack stack = master.getInputStack(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ModItems.heShell) {
                he += stack.getCount();
            } else if (stack.getItem() == ModItems.apfsdsShell) {
                apfsds += stack.getCount();
            }
        }
        return " 弹药[HE=" + he + " APFSDS=" + apfsds + "]";
    }
}
