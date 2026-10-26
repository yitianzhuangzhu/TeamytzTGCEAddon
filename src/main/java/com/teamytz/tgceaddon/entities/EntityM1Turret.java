package com.teamytz.tgceaddon.entities;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.APFSDSShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.M1HEShellProjectile;
import com.teamytz.tgceaddon.init.ModItems;
import com.teamytz.tgceaddon.init.ModSounds;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import techguns.TGItems;
import techguns.TGSounds;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;

/**
 * M1 主战坦克炮塔（0.30.0）。
 *
 * <p><b>两套武器，各自独立瞄准，同一个目标，直接开火（无先后排序）</b>：</p>
 * <table border="1" summary="weapons">
 *   <tr><th></th><th>主炮（mainweapon 组）</th><th>机枪（machinegun 方向机 + gun_main 高低）</th></tr>
 *   <tr><td>方向机</td><td>整个炮塔（模型整体 yaw）</td><td>machinegun 组自转（相对炮塔）</td></tr>
 *   <tr><td>高低</td><td>±15°</td><td>仰 50° / 俯 15°</td></tr>
 *   <tr><td>弹药</td><td>HE / APFSDS（与基础炮塔同一套判定）</td><td>科技枪步枪子弹：1 发子弹 = 5 发弹丸</td></tr>
 *   <tr><td>伤害</td><td>APFSDS 200（继承穿甲机制）<br>HE 中心 80 → 最外圈 25</td><td>10</td></tr>
 *   <tr><td>冷却</td><td>4 秒（80 tick）</td><td>2 tick（无装填机制）</td></tr>
 *   <tr><td>特效</td><td>继承基础炮塔（炮口烟/尾迹由弹丸自绘，爆炸沿用 HE 那套）</td><td>LMG 枪声</td></tr>
 * </table>
 *
 * <p>主炮开火时播后坐力动画：{@code cannon_barrel} 组沿炮管轴后退再回正（{@link #RECOIL_TICKS} tick）。</p>
 *
 * <p>炮口位置（由 bbmodel 求出，方法见 TURRET_GUIDE.md §5，Java 模型空间像素；
 * 数值由「旋转感知」脚本从最新模型算出——注意旋转元素的包围盒必须按角度变换，否则会算错）：</p>
 * <pre>
 * 主炮：PIVOT  = mainweapon 绝对旋转点 (0, 16.15, -32)    MUZZLE = (0, 15.15, -91)
 * 机枪：高低轴 = gun_main   绝对旋转点 (-5, 1.1429, -8.7857)
 *       方向轴 = machinegun 绝对旋转点 (-7.6667, 1.7, -9.6)
 *       MUZZLE = (-7.5, 0.5, -38)
 * </pre>
 *
 * <p>模型底部（Java 模型空间最大 y，含旋转）= <b>24</b>，渲染上移沿用
 * {@code EntityBaseTurret.RENDER_TRANSLATE_PX}(25)，故锚点 = 1 − (25−24)/16 = <b>0.9375</b>，
 * 见 {@link #getAnchorHeight()}。</p>
 */
public class EntityM1Turret extends EntityBaseTurret {

    /** 炮塔类型标识（对应卡片 NBT turretType） */
    public static final String TYPE = "m1";

    // ==================================================================
    // 主炮参数
    // ==================================================================

    /** 索敌/开火射程（格） */
    public static final double ATTACK_RANGE = 72.0D;

    /** 主炮射击间隔（tick）：4 秒一发 */
    public static final int MAIN_COOLDOWN = 80;

    /** 主炮每发消耗 RF */
    public static final int MAIN_POWER = 20;

    /** 主炮仰角上限（度） */
    public static final float MAIN_PITCH_UP = 15.0F;
    /** 主炮俯角上限（度） */
    public static final float MAIN_PITCH_DOWN = 15.0F;

    /** APFSDS 伤害（穿甲机制与基础炮塔一致，只是数值提高） */
    public static final float APFSDS_DAMAGE = 200.0F;

    /** 开火前"瞄准保持"tick（车体转向慢，停稳再打） */
    public static final int AIM_SETTLE_TICKS = 6;

    /** 主炮后坐力动画时长（tick） */
    public static final int RECOIL_TICKS = 6;

    // ==================================================================
    // 机枪参数
    // ==================================================================

    /** 机枪仰角上限（度） */
    public static final float MG_PITCH_UP = 50.0F;
    /** 机枪俯角上限（度） */
    public static final float MG_PITCH_DOWN = 15.0F;

    /** 机枪射击间隔（tick）：无装填机制，2 tick 一发（≈600 发/分） */
    public static final int MG_COOLDOWN = 2;

    /** 单发弹丸伤害 */
    public static final float MG_DAMAGE = 10.0F;

    /** 每消耗 1 个步枪子弹能打多少发 */
    public static final int MG_SHOTS_PER_ROUND = 5;

    /** 机枪弹丸速度/寿命/散布 */
    public static final float MG_SPEED = 6.0F;
    public static final int MG_TTL = 40;
    public static final float MG_SPREAD = 0.012F;

    /** 机枪方向机/高低机转速（度/tick） */
    public static final float MG_YAW_TURN_SPEED = 12.0F;
    public static final float MG_PITCH_TURN_SPEED = 10.0F;

    /** 机枪每发消耗 RF（比主炮便宜得多；0 = 不耗电，只耗弹药） */
    public static final int MG_POWER = 2;

    // ==================================================================
    // 炮口/俯仰轴（Java 模型空间像素）
    // ==================================================================

    private static final float[] MUZZLE_MAIN_PX = {0.0F, 15.15F, -91.0F};
    private static final float[] PIVOT_MAIN_PX = {0.0F, 16.15F, -32.0F};

    private static final float[] MUZZLE_MG_PX = {-7.5F, 0.5F, -38.0F};
    /** 机枪高低轴：gun_main 组的绝对旋转点 */
    private static final float[] PIVOT_MG_PX = {-5.0F, 1.1429F, -8.7857F};
    /** 机枪方向轴：machinegun 组的绝对旋转点 */
    private static final float[] PIVOT_MG_YAW_PX = {-7.6667F, 1.7F, -9.6F};

    // ==================================================================
    // 弹药/弹道常量
    // ==================================================================

    private static final float HE_SPREAD = 0.01F;
    private static final float APFSDS_SPREAD = 0.0F;
    private static final float DROP_START = 0.0F;
    private static final float DROP_END = 0.0F;
    private static final float DROP_MIN = 0.0F;
    private static final boolean COMPENSATE_HE_DROP = true;

    private static final int NO_FIRE_LOG_INTERVAL = 100;

    // ==================================================================
    // 同步参数
    // ==================================================================

    private static final DataParameter<Float> MG_YAW =
            EntityDataManager.createKey(EntityM1Turret.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> MG_PITCH =
            EntityDataManager.createKey(EntityM1Turret.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> RECOIL =
            EntityDataManager.createKey(EntityM1Turret.class, DataSerializers.FLOAT);

    // ==================================================================
    // 状态
    // ==================================================================

    /** 服务端：机枪水平角（世界 yaw，度） */
    protected float mgYaw = 0.0F;
    /** 服务端：机枪俯仰角（度，正=低头） */
    protected float mgPitch = 0.0F;
    /** 机枪射击冷却 */
    protected int mgCooldown = 0;
    /** 后坐力进度 0..1 */
    protected float recoil = 0.0F;

    private long lastNoFireLogTick = -1000L;

    // 客户端帧级补间状态（渲染器实例按实体类共享，状态必须放实体上，见 TURRET_GUIDE §4）
    public float renderMgYaw = Float.NaN;
    public float renderMgPitch = Float.NaN;
    public float renderRecoil = Float.NaN;

    public EntityM1Turret(World world) {
        super(world, TYPE);
    }

    public EntityM1Turret(World world, BlockPos masterPos, EnumFacing facing) {
        super(world, masterPos, facing, TYPE);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MG_YAW, 0.0F);
        this.dataManager.register(MG_PITCH, 0.0F);
        this.dataManager.register(RECOIL, 0.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(ATTACK_RANGE);
    }

    // ==================================================================
    // 基类钩子
    // ==================================================================

    @Override
    protected double getAttackRange() {
        return ATTACK_RANGE;
    }

    /**
     * 实体锚点高度（格）。
     *
     * <p>配对关系：{@code anchor + (RENDER_TRANSLATE_PX - 模型底部y)/16 = 1}
     * （1 = 基座上表面，模型底部正好坐在上面；其它炮塔都是底部 26 + 锚点 17/16 + 上移 25）。</p>
     *
     * <p>M1 模型底部（含旋转的包围盒最大 y）= <b>24</b>，而 {@code RENDER_TRANSLATE_PX}(25)
     * 在基类炮口换算里写死，所以锚点 = {@code 1 - (25-24)/16 = 0.9375}。</p>
     *
     * <p><b>注意</b>：不要把「未旋转的包围盒」当底部——M1 里 {@code cube_r17} 三轴都带旋转，
     * 未旋转时它伸到 y=29，旋转后其实只到 24 以上。按 29 算锚点会让整台车浮空 5 像素（0.31 格）。</p>
     */
    @Override
    protected double getAnchorHeight() {
        return 0.9375D;
    }

    @Override
    protected int getFireCooldown() {
        return MAIN_COOLDOWN;
    }

    @Override
    protected int getAimSettleTicks() {
        return AIM_SETTLE_TICKS;
    }

    /** 主炮俯仰限幅（±15°） */
    @Override
    protected float getPitchMaxUp() {
        return MAIN_PITCH_UP;
    }

    @Override
    protected float getPitchMaxDown() {
        return MAIN_PITCH_DOWN;
    }

    /** 索敌俯仰筛选放宽到机枪范围，否则主炮打不到的高空目标根本不会被锁定 */
    @Override
    protected float getTargetPitchMaxUp() {
        return MG_PITCH_UP;
    }

    @Override
    protected float getTargetPitchMaxDown() {
        return Math.max(MG_PITCH_DOWN, MAIN_PITCH_DOWN);
    }

    // ==================================================================
    // 每 tick：机枪瞄准 + 机枪开火 + 后坐力推进 + 同步
    // ==================================================================

    @Override
    public void onUpdate() {
        super.onUpdate();          // 主炮瞄准/转向/TURRET_PITCH 同步/心跳日志

        if (!this.world.isRemote) {
            this.updateMachineGunAim();
            this.updateMachineGunFire();
            if (this.recoil > 0.0F) {
                this.recoil = Math.max(0.0F, this.recoil - 1.0F / (float) RECOIL_TICKS);
            }
            this.dataManager.set(MG_YAW, this.mgYaw);
            this.dataManager.set(MG_PITCH, this.mgPitch);
            this.dataManager.set(RECOIL, this.recoil);
        }
        if (this.mgCooldown > 0) {
            --this.mgCooldown;
        }
    }

    /** 机枪方向机/高低机跟随同一个目标（没目标或不可开火时回正） */
    protected void updateMachineGunAim() {
        EntityLivingBase target = this.turretTarget;
        if (target != null && canFire()) {
            this.mgYaw = approachAngle(this.mgYaw, aimYawTo(target), MG_YAW_TURN_SPEED);
            float aimPitch = aimPitchTo(aimPointOf(target));
            this.mgPitch = MathHelper.clamp(
                    approachAngle(this.mgPitch, aimPitch, MG_PITCH_TURN_SPEED),
                    -MG_PITCH_UP, MG_PITCH_DOWN);
        } else {
            this.mgYaw = lerpAngle(this.mgYaw, this.turretYaw, 0.2F);
            this.mgPitch = lerpFloat(this.mgPitch, 0.0F, 0.2F);
        }
    }

    /** 机枪开火：对准 + 可见 + 弹道通视 + 有子弹，就哒哒哒（与主炮互不等待） */
    protected void updateMachineGunFire() {
        if (this.mgCooldown > 0) {
            return;
        }
        EntityLivingBase target = this.turretTarget;
        if (target == null || !canFire()) {
            return;
        }
        if (Math.abs(MathHelper.wrapDegrees(this.mgYaw - aimYawTo(target))) > YAW_ALIGN_TOLERANCE) {
            return;
        }
        float wantPitch = MathHelper.clamp(aimPitchTo(aimPointOf(target)), -MG_PITCH_UP, MG_PITCH_DOWN);
        if (Math.abs(MathHelper.wrapDegrees(this.mgPitch - wantPitch)) > PITCH_ALIGN_TOLERANCE) {
            return;
        }
        if (!canEntityBeSeen(target) || isShotBlocked(target)) {
            return;
        }
        if (this.tryFireMachineGun(target)) {
            this.mgCooldown = MG_COOLDOWN;
        }
    }

    // ==================================================================
    // 武器：机枪
    // ==================================================================

    /**
     * 机枪开火：每 {@link #MG_SHOTS_PER_ROUND} 发消耗 1 个科技枪步枪子弹。
     *
     * @return true = 打出了一发
     */
    protected boolean tryFireMachineGun(EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            return false;
        }
        // 先查弹药（每 5 发扣 1 个），再扣电——顺序不能反，否则没子弹时白耗能量
        ItemStack round = TGItems.RIFLE_ROUNDS.copy();
        if (!this.consumeAmmoEvery(MG_SHOTS_PER_ROUND, round)) {
            logNoFire("机枪没有步枪子弹了（弹药栏放科技枪 Rifle Rounds，1 个打 5 发）");
            return false;
        }
        if (MG_POWER > 0 && !master.consumeTurretPower(MG_POWER)) {
            logNoFire("机枪电量不足（本发需 " + MG_POWER + " RF）");
            return false;
        }

        double[] muzzle = this.muzzlePosMachineGun();
        Vec3d aim = aimPointOf(target);
        float[] ang = aimAnglesFrom(muzzle[0], muzzle[1], muzzle[2], aim);

        float keepYawHead = this.rotationYawHead;
        float keepPitch = this.rotationPitch;
        this.rotationYawHead = ang[0];
        this.rotationPitch = ang[1];
        GenericProjectile bullet = new GenericProjectile(this.world, this, MG_DAMAGE, MG_SPEED, MG_TTL,
                MG_SPREAD, DROP_START, DROP_END, DROP_MIN, 0.0F, false, EnumBulletFirePos.CENTER);
        this.rotationYawHead = keepYawHead;
        this.rotationPitch = keepPitch;

        bullet.setPosition(muzzle[0], muzzle[1], muzzle[2]);
        this.world.spawnEntity(bullet);

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                TGSounds.LMG_FIRE, SoundCategory.BLOCKS, 1.6F, 1.0F + this.world.rand.nextFloat() * 0.1F);
        return true;
    }

    // ==================================================================
    // 武器：主炮（HE / APFSDS）
    // ==================================================================

    @Override
    protected boolean tryFire(EntityLivingBase target) {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null || !master.isFormed()) {
            logNoFire("基座未成形");
            return false;
        }

        // 弹种由弹药栏里实际有的东西决定（先查弹药再扣电）
        ItemStack apfsds = new ItemStack(ModItems.apfsdsShell);
        ItemStack he = new ItemStack(ModItems.heShell);
        boolean hasApfsds = master.hasAmmo(apfsds);
        boolean hasHe = master.hasAmmo(he);
        if (!hasApfsds && !hasHe) {
            logNoFire("主炮没有 HE / APFSDS 弹药（把炮弹放进基座弹药槽 0-8）");
            return false;
        }
        boolean useApfsds = hasApfsds;
        ItemStack chosen = useApfsds ? apfsds : he;

        if (!master.consumeTurretPower(MAIN_POWER)) {
            logNoFire("主炮电量不足（本发需 " + MAIN_POWER + " RF）");
            return false;
        }
        if (!master.consumeAmmo(chosen)) {
            logNoFire("主炮扣弹药失败");
            return false;
        }

        float speed = useApfsds ? APFSDSShellProjectile.RECOMMENDED_SPEED : M1HEShellProjectile.RECOMMENDED_SPEED;
        int ttl = useApfsds ? APFSDSShellProjectile.RECOMMENDED_TTL : M1HEShellProjectile.RECOMMENDED_TTL;
        float damage = useApfsds ? APFSDS_DAMAGE : M1HEShellProjectile.RECOMMENDED_DAMAGE;

        double[] muzzle = muzzlePos(MUZZLE_MAIN_PX, PIVOT_MAIN_PX);
        Vec3d aim = aimPointOf(target);
        if (!useApfsds && COMPENSATE_HE_DROP) {
            aim = raiseAimForDrop(aim, muzzle, speed);
        }
        float[] ang = aimAnglesFrom(muzzle[0], muzzle[1], muzzle[2], aim);

        float keepYawHead = this.rotationYawHead;
        float keepPitch = this.rotationPitch;
        this.rotationYawHead = ang[0];
        this.rotationPitch = ang[1];
        GenericProjectile shell = useApfsds
                ? new APFSDSShellProjectile(this.world, this, damage, speed, ttl, APFSDS_SPREAD,
                        DROP_START, DROP_END, DROP_MIN, 0.0F, false, EnumBulletFirePos.CENTER)
                : new M1HEShellProjectile(this.world, this, damage, speed, ttl, HE_SPREAD,
                        DROP_START, DROP_END, DROP_MIN, 0.0F, false, EnumBulletFirePos.CENTER);
        this.rotationYawHead = keepYawHead;
        this.rotationPitch = keepPitch;

        shell.setPosition(muzzle[0], muzzle[1], muzzle[2]);
        this.world.spawnEntity(shell);

        // 后坐力动画（cannon_barrel 后退再回正）
        this.recoil = 1.0F;

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                ModSounds.CANNON_FIRE, SoundCategory.BLOCKS, 5.0F, 0.9F + this.world.rand.nextFloat() * 0.2F);

        if (DEBUG_TURRET) {
            double dist = Math.sqrt(sq(aim.x - muzzle[0]) + sq(aim.y - muzzle[1]) + sq(aim.z - muzzle[2]));
            TGCEAddon.getLogger().info("[debug][炮塔] " + this.turretType + " 主炮开火 弹种="
                    + (useApfsds ? "APFSDS(200)" : "HE(80→25)") + " 目标=" + target.getName()
                    + " 炮口→瞄准点=" + String.format("%.2f", dist));
        }
        return true;
    }

    /** HE 弹道超升：按飞行时间预估下坠量并抬高瞄准点 */
    private Vec3d raiseAimForDrop(Vec3d aim, double[] muzzle, float speed) {
        double dist = Math.sqrt(sq(aim.x - muzzle[0]) + sq(aim.y - muzzle[1]) + sq(aim.z - muzzle[2]));
        double t = speed > 0.0F ? dist / speed : 0.0D;
        double drop = 0.5D * M1HEShellProjectile.GRAVITY * t * t;
        return new Vec3d(aim.x, aim.y + drop, aim.z);
    }

    private static double sq(double v) {
        return v * v;
    }

    // ==================================================================
    // 机枪炮口坐标（模型像素 → 世界坐标）
    // ==================================================================

    /**
     * 机枪炮口。比基类的 {@code muzzlePos} 多两步：
     * 先绕 {@code gun_main} 的高低轴俯仰，再绕 {@code machinegun} 的方向轴转
     * {@code mgYaw - turretYaw}，最后套用与渲染器一致的「炮塔偏航 + 镜像」变换。
     */
    protected double[] muzzlePosMachineGun() {
        // ① 机枪高低（绕 gun_main 旋转点）
        float p = (float) Math.toRadians(this.mgPitch);
        float cosP = MathHelper.cos(p);
        float sinP = MathHelper.sin(p);
        float ly = PIVOT_MG_PX[1];
        float lz = PIVOT_MG_PX[2];
        float dy = MUZZLE_MG_PX[1] - ly;
        float dz = MUZZLE_MG_PX[2] - lz;
        float y3 = ly + (dy * cosP - dz * sinP);
        float z3 = lz + (dy * sinP + dz * cosP);
        float x3 = MUZZLE_MG_PX[0];

        // ② 机枪方向机（绕 machinegun 旋转点，相对炮塔）
        float d = (float) Math.toRadians(this.mgYaw - this.turretYaw);
        float cosD = MathHelper.cos(d);
        float sinD = MathHelper.sin(d);
        float dxp = x3 - PIVOT_MG_YAW_PX[0];
        float dzp = z3 - PIVOT_MG_YAW_PX[2];
        float x4 = PIVOT_MG_YAW_PX[0] + dxp * cosD + dzp * sinD;
        float z4 = PIVOT_MG_YAW_PX[2] - dxp * sinD + dzp * cosD;

        // ③ 炮塔偏航 + 渲染镜像（与基类 muzzlePos 同式）
        float angle = (float) Math.toRadians(this.turretYaw - 180.0F);
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        double wx = -((x4 / 16.0D) * cos + (z4 / 16.0D) * sin);
        double wz = -((x4 / 16.0D) * sin - (z4 / 16.0D) * cos);
        double wy = (RENDER_TRANSLATE_PX - y3) / 16.0D;
        return new double[]{this.posX + wx, this.posY + wy, this.posZ + wz};
    }

    // ==================================================================
    // 客户端读取（渲染器用）
    // ==================================================================

    public float getMgYaw() {
        return this.dataManager.get(MG_YAW);
    }

    public float getMgPitch() {
        return this.dataManager.get(MG_PITCH);
    }

    public float getRecoil() {
        return this.dataManager.get(RECOIL);
    }

    // ==================================================================
    // 日志
    // ==================================================================

    @Override
    protected String describeAmmo() {
        TurretBaseTileEntMaster master = getMasterTile();
        if (master == null) {
            return "";
        }
        int he = 0, apfsds = 0, rounds = 0;
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
            } else if (stack.getItem() == TGItems.RIFLE_ROUNDS.getItem()) {
                rounds += stack.getCount();
            }
        }
        return " 弹药[HE=" + he + " APFSDS=" + apfsds + " 步枪子弹=" + rounds
                + "(" + (rounds * MG_SHOTS_PER_ROUND) + "发)]";
    }

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
}
