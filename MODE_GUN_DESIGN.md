# 枪械模式基类设计 v2（TGCEAddon / MC 1.12.2 / Forge）

> 目标：一批「右键切档、左键功能随之改变」的枪械。
> 语义：右键 = 功率/功能切档；左键 = 开火，光束**持续照射烧穿方块**（不是原版工具挖掘）。
>
> v2 相对 v1 的变化：v1 假设「挖掘」= 原版工具路径（`getDestroySpeed` / `IGenericGunMelee`）。
> 按你的澄清，挖掘改为**光束破坏能力**，因此父类从 `GenericGunMeleeBase` 改成 `GenericGun`，
> 并新增「光束弹丸 + 烧穿进度」这条链路。
>
> 所有 techguns 结论来自 `libs/Techguns2-CE-master` 源码（带行号），所有原版签名用 `javap`
> 对 `forge-1.12.2-14.23.5.2860_mapped_snapshot_20171003-1.12.jar` 核对过。

---

## 实现状态（0.29.0 已落地，与下文 v2 的差异以本节为准）

0.29.0 实际实现采用的是 **NDR 式「持久光束」**（见 `NDRProjectile`：每 tick 重新对准射手 +
重新射线），而不是下文 §5 描述的「每发独立射线」。下文 §5.1/§5.3 的思路仍然成立，
但**射线的发起位置从「枪的 shootGun」搬到了「弹丸的 onHit」**——因为 NDR 的光束本身就是
每 tick 命中的实体，挂在它身上比另做一次射线更准，也顺带解决了 §10 风险表第 9 条。

已落地的文件：

| 文件 | 作用 |
|---|---|
| `item/weapon/mode/GunModeFunction.java` | `STUN` / `KILL` / `MINE` |
| `item/weapon/mode/GunMode.java` | 档位参数（伤害 / 药水时长 / 百分比 / 烧穿点数） |
| `item/weapon/mode/IModeSwitchable.java` | 切档全部 default 逻辑（NBT `gm`、右键循环、冷却、音效、显示名） |
| `item/weapon/GenericGunModeBase.java` | 基类：右键两个钩子 + tooltip + `setShootWithLeftClick(true)` |
| `item/weapon/ItemTestBeamer.java` | 测试枪（套 NDR 模型与贴图，三档 击晕/击杀/挖掘） |
| `entities/projectiles/ModeBeamProjectile.java` | NDRProjectile 子类：按档位结算，不调父类通用弹伤 |
| `entities/BeamBurnTracker.java` | 烧穿进度账本 + 破块序列 |

两处与下文不同、**以代码为准**的细节：

1. 烧穿由 `ModeBeamProjectile.onHit`（服务端）做，不走 §5.1 的 `burnAlongRay`。
2. 弹丸寿命固定用 `NDRProjectile.BEAM_LIFETIME`(=10)，**不能**用 `getScaledTTL()`
   ——后者算的是 `ticksToLive / speed`，光束武器会算成 1。

### 0.29.1 修复（裂纹 + 挖掘时长）

* **裂纹不显示**：不能调 `World.sendBlockBreakProgress`。1.12.2 里它只遍历
  `IWorldEventListener`（客户端世界才有），`WorldServer` 也**没有覆写**它 —— 服务端调用等于空转。
  正解是自己遍历 `world.playerEntities` 发 `SPacketBlockBreakAnim(breakerId, pos, stage)`
  （stage 0..9，-1 = 清除）。已封装成 `BeamBurnTracker.sendOverlay(...)`。
* **低硬度方块一照就没**：射速 5 tick + 光束寿命 10 tick ⇒ 每 tick 有 **2 条光束同时命中**，
  泥土（needed = 0.5×30 = 15 点）只要 5 tick 就碎，肉眼就是"没有挖掘时长"；石头
  （45 点 → 15 tick）才有可感知的延迟。修法是给每个方块加 `MIN_BURN_TICKS = 12` 的下限
  （点数够了还要等满 12 tick），裂纹进度取「点数进度」与「时间进度」里更慢的那个，
  两者一致增长。此外 `needed` 改为**每次命中都按当前方块重算**，杜绝沿用上一块的可能。

---

## 0. 结论速览

```
GenericGun（科技枪）
 └─ GenericGunModeBase            ← 新基类：模式切档 + 光束开火参数化
      implements IModeSwitchable  ← 切档逻辑做成接口 default 方法，将来近战系基类可复用
 └─ 具体枪械（例：ItemLaserDrill）
```

配套 **5** 个新文件（不需要新实体类，见 §5.2）：

| 文件 | 作用 |
|---|---|
| `item/weapon/mode/IModeSwitchable.java` | 模式表 + 读写 NBT + 切档（default 方法承载全部逻辑） |
| `item/weapon/mode/GunMode.java` | 一档的描述数据（伤害/烧穿/弹药/表现） |
| `item/weapon/mode/GunModeFunction.java` | `ATTACK`（打实体） / `BURN`（烧方块） |
| `item/weapon/GenericGunModeBase.java` | 新基类（约 180 行，含服务端权威射线） |
| `entities/BeamBurnTracker.java` | 烧穿进度与破块（照 `ItemLockOnWeapon.STATES` 的静态表 + 清理模式） |

**核心结论三条**：

1. **左键开火不用自己写输入层**。科技枪 `TGTickHandler` L91-108 已经在做「按住左键 → 每隔
   `minFiretime` tick 双端各调一次 `shootGunPrimary`」，客户端还会 `PacketShootGun` 通知服务端
   （`PacketShootGun` L62 → 服务端 `shootGunPrimary`）。我们只要 `setShootWithLeftClick(true)` 就白拿。
2. **原版挖掘已经被科技枪自己屏蔽**：`TGTickHandler` L84-89 强制关掉 `keyBindAttack` 的按键状态，
   `TGEventHandler` L132-134 取消攻击键的 `MouseEvent`、L143-147 取消 `PlayerInteractEvent.LeftClickBlock`。
   → 手不会挥、方块不会按原版进度破，左键纯粹是「开火」。
3. **光束外观直接复用科技枪的激光渲染**：`LaserProjectile` + `RenderLaserProjectile`
   （叠加混合、`techguns:textures/fx/laser3.png`、宽度随寿命按 `sin(sqrt(prog)*π)` 收放）。
   本仓库的 `TurretLaserBeamProjectile` 已经用这条路跑通过（含破坏方块的海关判定）。

---

## 1. 调研：谁在什么时候调用什么（开火链）

```
[客户端] 按住左键
  └ TGKeybinds / TGEventHandler 设置 ClientProxy.keyFirePressedMainhand = true
      └ TGTickHandler.onPlayerTick (L91-108)
           ├ 条件：isShootWithLeftClick() && keyFirePressedMainhand
           │        && props.getFireDelay(hand) <= 0     ← 射速节流在这
           ├ TGPackets.sendToServer(new PacketShootGun(isZooming, hand))
           └ gun.shootGunPrimary(stack, world, player, zooming, hand, null)   ← 客户端特效分支
[服务端] PacketShootGun.Handler (L62)
  └ gun.shootGunPrimary(...)                                                  ← 服务端权威分支
       ├ 弹药检查：getCurrentAmmo(stack) > 0
       ├ 射速：extendedPlayer.getFireDelay(hand) <= 0 → setFireDelay(hand, minFiretime)
       ├ 扣弹：useAmmo(stack, 1)（创造模式不扣）
       ├ if (!world.isRemote) → shootGun(...)          ← 生成弹丸（权威）
       └ if (world.isRemote)  → 后坐力 / 枪口闪光 / 音效
```

关键细节：

* `shootGun(world, player, itemstack, accuracybonus, damagebonus, attackType, hand, firePos, target)`
  是 `protected`（`GenericGun` L696），**带 ItemStack**，并且把 `damagebonus` 一路传给弹丸
  （`spawnProjectile` L419-424：`damage * damagebonus`）。
  → **这是我们唯一能按栈注入「模式倍率」的官方缝**：覆写 `shootGun`，把 `damagebonus` 乘上模式倍率。
* `spawnProjectile` 是 `protected` 但内部读的全是**包私有字段**（`damage`/`speed`/`penetration`/
  `ticksToLive`/`radius`/`gravity`…），我们包外读不到 → **不要覆写 `spawnProjectile`**，
  走 `shootGun` 的 `damagebonus` 才是干净做法。
* 弹丸工厂签名（`IProjectileFactory`，见 `LaserProjectile.Factory` L67-79）：
  ```java
  createProjectile(GenericGun gun, World world, EntityLivingBase p, float damage, float speed, int TTL,
                   float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
                   float penetration, boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity)
  ```
  工厂拿得到 **gun 和 shooter**，所以能反查射手手里的栈 → 把**模式索引**当构造参数塞进弹丸。
* 光束是「构造即命中」：`LaserProjectile` 构造器末尾调 `trace()`（`AbstractBeamProjectile` L42-84），
  射线命中后立刻回调 `onHit`，并把 `distance/laserPitch/laserYaw/maxTicks` 写进 spawn data
  （L96-112）供客户端画长度。**所以子类构造器里不能依赖尚未初始化的实例字段**
  （`TurretLaserBeamProjectile` 的注释已踩过这个坑）。
* 弹丸的 `blockdamage` 标志是**死代码**：`GenericProjectile` L66/L161 只存不用
  → 科技枪的弹丸**从不破坏方块**，破坏逻辑必须我们自己写。

---

## 2. 右键选型（沿用 v1 的结论）

| 路线 | 双端 | 双持 | 与开镜冲突 | 需要发包 | 结论 |
|---|---|---|---|---|---|
| `onItemRightClick` + `onItemUseFirst` | ✅ | ✅ | ❌ | ❌ | **采用** |
| `hasSecondaryAction` + `gunSecondaryAction` | ❌ 仅客户端 | ❌ 被副手分支劫持（`TGEventHandler` L180） | ✅ 冲突（它=开镜） | ✅ 必须 | 不用 |
| 自挂 `PlayerInteractEvent` | ✅ | ✅ | ✅ | ❌ | 不必要 |

1.12.2 的右键分流（必须两个钩子都实现，否则「对着方块右键」不触发）：

* 空指/空气 → `Item.onItemRightClick(World, EntityPlayer, EnumHand)`
* 指着方块 → `ForgeHooks.onRightClickBlock` 先调
  `Item.onItemUseFirst(EntityPlayer, World, BlockPos, EnumFacing, float,float,float, EnumHand)`，
  返回 `SUCCESS` 可吞掉方块交互（箱子/熔炉不会被打开）

**按你的决定：不做潜行分支。** 右键永远切档，并吞掉方块交互。
如果之后想保留「拿着枪也能开箱子」，把 `sneakToInteract` 开关打开即可（基类留了这个布尔）。

---

## 3. 模式模型

```java
public enum GunModeFunction { ATTACK, BURN }
```

一档 = 一组数值：

```java
public final class GunMode {
    public final String id;              // lang 后缀
    public final GunModeFunction function;
    public final int power;              // 1..N，仅用于显示与排序

    // —— 通用开火轴 ——
    public final float damageMultiplier; // 乘到弹丸伤害上（经 shootGun 的 damagebonus）
    public final float accuracyMultiplier;
    public final int   ammoPerShot;      // 每发消耗（1 = 科技枪默认；>1 需自己补扣）

    // —— BURN（光束烧穿）轴 ——
    public final float burnPower;        // 每发命中折算的「原版手挖 tick 数」
    public final boolean dropBlocks;     // 烧穿后是否掉落（false = 直接气化）
    public final boolean instantSmash;   // true = 按硬度阈值瞬破（炮塔过载激光那套）
    public final float smashMaxHardness; // instantSmash 时的硬度上限

    // —— 表现 ——
    public final SoundEvent switchSound; // null = 用默认
    public final String impactParticle;  // 命中粒子名（techguns PacketSpawnParticle）
}
```

工厂方法：

```java
GunMode.attack(int power, float dmgMult, float accMult, int ammoPerShot)
GunMode.burn  (int power, float burnPower, float dmgMult, int ammoPerShot, boolean dropBlocks)
GunMode.smash (int power, float maxHardness, float dmgMult, int ammoPerShot)   // 瞬破
```

切档规则：**右键 → 下一档（循环）**，6 tick `CooldownTracker` 防连点。

---

## 4. 切档实现（`IModeSwitchable` 的 default 方法）

```java
public interface IModeSwitchable<T extends GenericGun> {

    String MODE_NBT = "gm";      // 物品 NBT 键

    GunMode[] getModes();
    int getDefaultMode();

    default GunMode getMode(ItemStack stack) {
        GunMode[] modes = getModes();
        if (modes == null || modes.length == 0) return null;
        int i = getModeIndex(stack);
        return modes[Math.floorMod(i, modes.length)];
    }

    default int getModeIndex(ItemStack stack) {
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) {
            ((GenericGun) this).onCreated(stack, null, null);   // 与父类 getMiningHeadLevel 同款兜底
            tags = stack.getTagCompound();
        }
        return tags == null ? getDefaultMode() : tags.getInteger(MODE_NBT);
    }

    default void setModeIndex(ItemStack stack, int index) {
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) { ((GenericGun) this).onCreated(stack, null, null); tags = stack.getTagCompound(); }
        if (tags != null) tags.setInteger(MODE_NBT, index);
    }

    /** 右键入口；返回 true 表示这次点击被我们吃掉 */
    default boolean trySwitchMode(World world, EntityPlayer player, EnumHand hand) {
        GunMode[] modes = getModes();
        if (modes == null || modes.length < 2) return false;
        if (player.getCooldownTracker().hasCooldown((Item) this)) return false;

        ItemStack stack = player.getHeldItem(hand);
        int next = Math.floorMod(getModeIndex(stack) + 1, modes.length);
        setModeIndex(stack, next);
        player.getCooldownTracker().setCooldown((Item) this, 6);
        playSwitchFeedback(world, player, modes[next]);
        return true;
    }

    default void playSwitchFeedback(World world, EntityPlayer player, GunMode mode) { /* 音效，见 §4.1 */ }
}
```

### 4.1 反馈

* **音效**：客户端 `player.playSound(...)` 立即响；服务端用 techguns
  `PacketPlaySound(SoundEvent, Entity, vol, pitch, repeat, moving, gunPosition, playOnOwnPlayer, TGSoundCategory)`
  （9 参构造器，本仓库 `GenericGunMeleeBase` L234 已在用，`playOnOwnPlayer=false`）广播给周围。
  传进去的必须是**已注册的 SoundEvent**（`techguns.TGSounds` 现成音，或按 `init/ModSounds.createSoundEvent`
  新注册一个——`PacketPlaySound` 内部取 `getRegistryName()`）。
* **防连点**：`EntityPlayer.getCooldownTracker()`（已验证）+ `setCooldown(Item, int)`。别用
  `TGExtendedPlayer.setFireDelay`（那是射击冷却，会把开火锁死）。

### 4.2 右键钩子

```java
@Override
public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    if (trySwitchMode(world, player, hand))
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    return super.onItemRightClick(world, player, hand);             // 父类 = PASS
}

@Override
public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
                                      float hitX, float hitY, float hitZ, EnumHand hand) {
    if (trySwitchMode(world, player, hand)) return EnumActionResult.SUCCESS;   // 吞掉，不开箱子
    return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
}
```

双端各自执行，**不需要网络包**：两端从同一份 NBT 算出同一个 next。

---

## 5. 左键 = 光束烧穿（核心章节）

### 5.1 基类需要覆写的只有两个方法

```java
public abstract class GenericGunModeBase extends GenericGun implements IModeSwitchable<GenericGunModeBase> {

    public GenericGunModeBase(String name, ProjectileSelector sel, boolean semiAuto, int minFiretime, int clipsize,
                              int reloadtime, float damage, SoundEvent firesound, SoundEvent reloadsound,
                              int TTL, float accuracy) {
        super(name, sel, semiAuto, minFiretime, clipsize, reloadtime, damage, firesound, reloadsound, TTL, accuracy);
        this.setShootWithLeftClick(true);    // ← 关键：左键变开火，科技枪自动屏蔽原版挖掘
    }

    /**
     * 每发调用一次（双端各一次）。做三件事：
     * ① 按模式放大伤害/精度；② 服务端做权威射线并累积烧穿；③ 交回科技枪生成光束弹丸。
     */
    @Override
    protected void shootGun(World world, EntityLivingBase shooter, ItemStack stack, float accuracybonus,
                            float damagebonus, int attackType, EnumHand hand,
                            EnumBulletFirePos firePos, Entity target) {
        GunMode m = getMode(stack);
        float acc = accuracybonus * (m == null ? 1f : m.accuracyMultiplier);
        float dmg = damagebonus * (m == null ? 1f : m.damageMultiplier);

        if (m != null && shooter instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) shooter;
            // ② 服务端权威：先算这一发照到哪块方块，累积烧穿
            if (!world.isRemote) {
                this.burnAlongRay(world, player, stack, m);
                // 高功率多耗弹：父类已扣 1，这里补扣 (ammoPerShot-1)；每发恰好一次，不会重复计费
                if (m.ammoPerShot > 1 && !player.capabilities.isCreativeMode) {
                    this.useAmmo(stack, m.ammoPerShot - 1);
                }
            }
        }

        // ③ 科技枪生成光束弹丸：视觉 + 实体能量伤害全部白拿
        super.shootGun(world, shooter, stack, acc, dmg, attackType, hand, firePos, target);
    }

    /** 服务端权威射线：照到方块就累积烧穿，没照到就清进度 */
    protected void burnAlongRay(World world, EntityPlayer player, ItemStack stack, GunMode mode) {
        if (mode.function != GunModeFunction.BURN && !mode.instantSmash) return;

        Vec3d eyes = player.getPositionEyes(1.0f);
        Vec3d end = eyes.add(player.getLookVec().scale(this.beamRange));
        RayTraceResult hit = world.rayTraceBlocks(eyes, end, false, true, false);

        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            BeamBurnTracker.clear(player, world);         // 准星离开方块 → 清裂纹
            return;
        }
        BlockPos pos = hit.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        float hardness = state.getBlockHardness(world, pos);
        if (hardness < 0f || state.getMaterial().isLiquid()) {
            BeamBurnTracker.clear(player, world);
            return;
        }
        BeamBurnTracker.hit(world, player, pos, state, hardness, mode, stack);
    }
}
```

> 注意 `useAmmo` 会被装填/换弹等多条路径调用，**不要覆写 `useAmmo` 做倍率**——
> 只在 `shootGun`（每发恰好一次、双端各一次）里补扣，才不会重复计费。
>
> 可选加固：若准星前方先命中实体（光束被挡），可以用 `world.getEntitiesWithinAABB` 粗筛后跳过烧方块，
> 避免「隔着一个怪物把后面的墙烧了」。第一版可以不做。

### 5.2 光束外观：直接用科技枪现成的激光，**不新建实体**

`LaserProjectile` 已经注册好（`TGEntities` L90）且已有渲染器（techguns `ClientProxy` L1073 注册
`RenderLaserProjectile`）。所以基类只要在构造时传一个激光弹丸选择器：

```java
// 基类构造时传进去即可（GenericGun 的字段名就是 projectile_selector，protected 可覆盖）：
// 科技枪现成写法：TGuns.LASERGUN_PROJECTILES = new ProjectileSelector<>(AmmoTypes.ENERGY_CELL, new LaserProjectile.Factory())
new ProjectileSelector<>(AmmoTypes.ENERGY_CELL, new BeamFactory(minFiretime + 1))
```

**为什么不新建弹丸子类**（v2 草案最初的想法，已否决）：

* 弹丸的命中判定发生在**构造器里**（`LaserProjectile` 构造器末尾调 `trace()` →
  `AbstractBeamProjectile` L42-84）。要按模式烧方块，就得把模式字段传进构造器，
  但 `final` 字段在父类构造器执行期间还是默认值 0/null → **烧穿参数永远拿不到**。
  本仓库 `TurretLaserBeamProjectile` 之所以能这么干，是因为它的参数是**编译期常量**
  （`protected float getHitExplosionStrength()` 返回常量），我们的是每发不同的模式，套不上。
* 把烧穿放到服务端的 `shootGun`（§5.1）里，顺序、参数、双端一致性全都直白，且少一个类、
  少一次实体注册、少一次渲染器注册。

光束弹丸工厂（20 行，只为了控制光束长度）：

```java
public static class BeamFactory implements IProjectileFactory<LaserProjectile> {
    private final int beamTTL;                          // = minFiretime + 1
    public BeamFactory(int beamTTL) { this.beamTTL = beamTTL; }

    @Override
    public LaserProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p, float damage,
            float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin,
            float penetration, boolean blockdamage, EnumBulletFirePos firePos, float radius, double gravity) {
        return new LaserProjectile(world, p, damage, speed, beamTTL, spread, dmgDropStart, dmgDropEnd,
                dmgMin, penetration, blockdamage, firePos);
    }

    @Override
    public DamageType getDamageType() { return DamageType.ENERGY; }
}
```

* 科技枪自带的 `LaserProjectile.Factory` 把 TTL 硬编码成 7（`LaserProjectile` L72）；
  我们换成 `minFiretime + 1`，相邻两发光束首尾相接 → 看起来是一条不断的光束。
* `RenderLaserProjectile` 用 `ticksExisted/maxTicks` 驱动宽度与淡出（`sin(sqrt(prog)*π)`），
  TTL 越小光束越「短命」，观感越像持续照射。
* 实体伤害白拿：`LaserProjectile.getProjectileDamageSource()` = 科技枪能量伤害，带 `penetration`。

### 5.3 服务端烧穿进度（`BeamBurnTracker`）

本仓库已有同款模式：`ItemLockOnWeapon` 的静态 `STATES` map（L362-388 有清理逻辑），照抄即可。

```java
public final class BeamBurnTracker {
    private static final Map<EntityPlayer, Burn> BURNS = new HashMap<>();

    static final class Burn {
        int dim; BlockPos pos; float progress; int lastTick;
    }

    /** 每发命中方块调用一次（服务端）：累积进度，够了就破块 */
    public static void hit(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                           float hardness, GunMode mode, ItemStack heldStack) {
        if (!(player instanceof EntityPlayerMP)) return;           // 只对玩家生效
        EntityPlayerMP p = (EntityPlayerMP) player;
        int now = p.ticksExisted;
        Burn b = BURNS.computeIfAbsent(p, k -> new Burn());

        // 换目标 / 超时（8 tick 没再照到，约等于"松手/移开准星"）→ 清进度 + 清裂纹
        if (b.pos == null || !b.pos.equals(pos) || b.dim != world.provider.getDimension() || now - b.lastTick > 8) {
            clear(p, world);
            b.pos = pos;
            b.progress = 0f;
            b.dim = world.provider.getDimension();
        }
        b.lastTick = now;

        // 瞬破档：硬度阈值内直接没
        if (mode.instantSmash) {
            if (hardness <= mode.smashMaxHardness) {
                breakBlock(world, p, pos, state, heldStack, mode.dropBlocks);
                clear(p, world);
            }
            return;
        }

        float needed = Math.max(1.0f, hardness * 30.0f);           // 原版手挖 tick 数 ≈ 硬度×30
        b.progress += mode.burnPower;

        // 裂纹进度（0..9），服务端调用即全客户端可见
        world.sendBlockBreakProgress(p.getEntityId(), pos, (int) Math.min(9, b.progress / needed * 10));

        if (b.progress >= needed) {
            breakBlock(world, p, pos, state, heldStack, mode.dropBlocks);
            clear(p, world);
        }
    }

    /** 清进度与裂纹（准星离开方块、超时、破块后都要调） */
    public static void clear(EntityPlayer player, World world) {
        Burn b = BURNS.remove(player);
        if (b != null && b.pos != null && player instanceof EntityPlayerMP) {
            world.sendBlockBreakProgress(player.getEntityId(), b.pos, -1);
        }
    }

    /** 玩家下线/死亡/换手时清理，防泄漏（照 ItemLockOnWeapon.cleanupStates 的做法） */
    public static void cleanup(EntityPlayer current) { /* 遍历 BURNS，移除 isDead / 不持有本类枪的玩家 */ }
}
```

**破块序列**：直接照抄科技枪已经跑通的 `GenericGunMeleeCharge.onBlockStartBreak`（L328-345），
它把「保护 mod / 经验 / 掉落 / 客户端同步」全套都做对了：

```java
private static void breakBlock(World world, EntityPlayerMP player, BlockPos pos, IBlockState state,
                               ItemStack heldStack, boolean dropBlocks) {
    int xp = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos); // <0 = 被取消
    if (xp < 0) return;                                   // 领地/保护插件拒绝，什么都不做
    Block block = state.getBlock();
    block.onBlockHarvested(world, pos, state, player);
    TileEntity tile = world.getTileEntity(pos);
    if (block.removedByPlayer(state, world, pos, player, true)) {
        block.onPlayerDestroy(world, pos, state);
        if (dropBlocks && !player.capabilities.isCreativeMode) {
            block.harvestBlock(world, player, pos, state, tile, heldStack);
            block.dropXpOnBlockBreak(world, pos, xp);
        }
        world.playEvent(2001, pos, Block.getStateId(state));           // 方块碎裂粒子（就是"炸碎"的观感）
        player.connection.sendPacket(new SPacketBlockChange(world, pos));
    }
}
```

> `dropBlocks = false` 时也可以直接用 `world.destroyBlock(pos, false)`（本仓库
> `TurretLaserBeamProjectile.smashSoftBlocks` L98 就在用），但那样绕过保护事件；
> **推荐一律走上面的序列，`dropBlocks` 只控制掉不掉落**。

### 5.4 三种「挖掘」方案的对照（你让我研究的这部分）

| 方案 | 实现量 | 手感 | 掉落/保护/同步 | 适用 |
|---|---|---|---|---|
| **A. 真光束 + 进度表**（本文采用） | 中：1 个 `BeamBurnTracker` + 基类 2 个覆写（`shootGun`/`burnAlongRay`）+ 20 行弹丸工厂 | 持续照射、可逐档调快、可做「气化无掉落」 | 自己走 `ForgeHooks.onBlockBreakEvent` + `harvestBlock` 序列，全套可控 | 激光枪 / 光束钻 |
| **B. 假工具**（`IGenericGunMelee` 那套） | 小：本仓库 `GenericGunMeleeBase` 已经完整实现，只需加 v1 的右键切档 | 就是**原版**挖掘：裂纹进度、`getDestroySpeed` 决定快慢、看着镜头挖 | 原版全包 | 想先快速验证切档，或要「工具感」的枪 |
| **C. 瞬破**（硬度阈值内直接没） | 最小：抄 `TurretLaserBeamProjectile.smashSoftBlocks`（L80-100） | 像炮塔过载激光：软方块一照就没 | 用 `destroyBlock(pos,false)` = 无掉落 | 过载/大招档 |

**建议**：模式表里同时提供 A 与 C —— 低档 `burn`（慢慢烧，有掉落），最高档
`smash`（阈值内瞬破、无掉落，爽感）。两者共用同一个弹丸与渲染，只是 `GunMode` 的字段不同。

**为什么不用 B**：你说「不是原版那种工具的挖掘」。B 的进度是原版公式
（`state.getPlayerRelativeBlockHardness`），改不动观感；而且它要求物品实现 `IGenericGunMelee`，
会被 `isEffectiveToolForState` 的「有弹药才能挖」门禁绑住，反而更容易出怪现象。

### 5.5 数值模型（调参起点）

```
needed（烧穿一个方块需要的点数） = max(1, 硬度 × 30)
每发贡献（burnPower）           = 该档的 burnPower
烧穿时间（tick）                = needed / burnPower × minFiretime
```

参考硬度（`state.getBlockHardness`，本仓库 `SMASH_MAX_HARDNESS` 注释里也列过同类数值）：
泥土/沙子 0.5、玻璃 0.3、树叶 0.2、石头 1.5、原木 2.0、黑曜石 50。

例：`burnPower = 15`、`minFiretime = 2`
→ 泥土(0.5) 15/15=1 发；石头(45) 3 发 ≈ 6 tick；黑曜石(1500) 100 发 ≈ 10 秒（合理，黑曜石就该烧很久）。
高档 `burnPower = 40` → 石头 2 发、黑曜石 38 发 ≈ 4 秒。

---

## 6. NBT 与双端一致性

| 项 | 方案 |
|---|---|
| 键名 | `gm`（int，默认 0）。避开科技枪的 `ammo` / `ammovariant` / `miningHead` / `camo` |
| 写入 | `addInitialTags(NBTTagCompound)` 里 `super` 后 `tags.setInteger("gm", getDefaultMode())` |
| 读取 | 无 tag 时先 `onCreated`，再 `Math.floorMod(index, modes.length)` 钳制 |
| 网络 | 切档不需要包（双端各算一次）；模式随 NBT 走背包同步 |
| 烧穿时怎么拿到模式 | `shootGun` 的回调**带 ItemStack**，直接 `getMode(stack)`，不需要任何反查或临时字段 |
| 越界/老物品 | 钳制 + 默认档兜底 |

主副手各一把同款枪 → 各自独立 NBT，互不影响。

---

## 7. 落地模板（示例：激光钻）

```java
public class ItemLaserDrill extends GenericGunModeBase {

    public ItemLaserDrill(String name, SoundEvent fire, SoundEvent reload) {
        // 弹丸选择器：能量弹药 + 我们自己的光束工厂（TTL 由工厂控制，束长见 §5.2）
        super(name,
              new ProjectileSelector<>(AmmoTypes.ENERGY_CELL, new GenericGunModeBase.BeamFactory(3)),
              /*semiAuto*/ false, /*minFiretime*/ 2, /*clipsize*/ 60, /*reloadtime*/ 60,
              /*damage*/ 4.0f, fire, reload, /*TTL*/ 3, /*accuracy*/ 0.0f);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setCrossHair(EnumCrosshairStyle.VANILLA);
        this.setTexture(new ResourceLocation(TGCEAddon.MODID, "textures/guns/laser_drill"));   // 科技枪会自动加 .png
        this.setModes(
            GunMode.burn (1, 10f, 1.0f, 1, true ),   // 低功率：慢烧、有掉落
            GunMode.burn (2, 20f, 1.5f, 2, true ),
            GunMode.burn (3, 35f, 2.0f, 3, false),   // 高功率：快烧、气化无掉落
            GunMode.smash(4, 2.0f, 2.5f, 4 )         // 过载：硬度≤2.0 瞬破
        );
    }
}
```

注册两处（照现有武器）：`init/ModItems.java` 字段 + `registerItem` + `registerModels`
（模型走科技枪的 `ItemRenderHack` + `RenderGunBase90` 或普通 inventory 模型）；
两份 lang 加武器名 + 每档名称（`item.tgceaddon.laser_drill.gm.1` 之类）。
**不用注册新实体、不用注册新渲染器**——光束复用科技枪的 `LaserProjectile`。

---

## 8. 改动清单 / 版本

| 类型 | 文件 | 说明 |
|---|---|---|
| 新增 | `item/weapon/mode/GunModeFunction.java` | 枚举 |
| 新增 | `item/weapon/mode/GunMode.java` | 模式数据 + 工厂 |
| 新增 | `item/weapon/mode/IModeSwitchable.java` | 切档 default 逻辑 |
| 新增 | `item/weapon/GenericGunModeBase.java` | 基类（约 180 行：切档钩子 + `shootGun` + `burnAlongRay` + `BeamFactory`） |
| 新增 | `entities/projectiles/BeamBurnTracker.java` | 烧穿进度、裂纹显示、破块序列 |
| 修改 | `init/ModItems.java`、两份 lang | 实际枪械与文案 |
| 修改 | `README.md` 同级文档 | 把本文升级成使用手册（照 `TURRET_GUIDE.md` 的先例） |
| 新增（可选） | `init/ModSounds` 加 `mode_switch` 音效 | 切档音 |

**不新增**：实体类、实体注册、渲染器注册（光束复用科技枪 `LaserProjectile`）。

版本：`0.28.6 → 0.29.0`（成体系新增），同步 `build.gradle` 与 `TGCEAddon.VERSION`。

---

## 9. 验证步骤

构建：`mc_gradle` → `projectDir = TeamytzTGCEAddon`、`task = build`（wrapper 已接 `.tools/jdk8u504-b01`），
产物 `build/libs/tgceaddon-0.29.0.jar`（reobf），同步到 `G:\MC\.minecraft\versions\tgaddontest\mods`（删旧留新）。

进游戏逐条过：

1. 空指右键 → 切档、音效、快捷栏冷却遮罩、tooltip 变化；
2. **对着方块右键** → 切档，且不会打开箱子/熔炉；
3. 左键按住对石头 → 出现裂纹进度、按档位速度烧穿、掉落正常（低档）；**手不会挥、没有原版挖掘音**；
4. 高档/过载档 → 瞬破或气化，无掉落；
5. 对准基岩/黑曜石 → 基岩不动（硬度 <0 跳过），黑曜石耗时明显变长；
6. 领地插件（若有）拒绝时 → 方块不破，且不刷掉落；
7. 放开左键或移开准星 → 裂纹进度在 ~8 tick 内消失，不会残留；
8. 光束外观：相邻两发首尾相接（TTL = minFiretime + 1），且有淡出；
9. 多人（≥2 客户端）：另一名玩家能看见光束与方块碎裂粒子；
10. 打实体 → 能量伤害随档位变化；消耗弹药按 `ammoPerShot`；
11. 主副手各一把 → 各切各的；
12. 退出重进 → 模式保留。

---

## 10. 风险与待实测项

| # | 风险 | 处理 |
|---|---|---|
| 1 | `onItemUseFirst` 是否真的**双端**各调一次 | 第一版打日志确认；若只有服务端，则模式写入放服务端，客户端靠背包同步（一帧延迟可接受） |
| 2 | `shootGun` 在**服务端**是否真被调用（依赖 `PacketShootGun` 链路） | 已在源码确认：`TGTickHandler` L100 发 `PacketShootGun`，`PacketShootGun` L62 调 `shootGunPrimary` → 服务端分支 L528 调 `shootGun`。第一版仍建议在 `burnAlongRay` 里打一行「服务端射线命中」日志确认 |
| 3 | `sendBlockBreakProgress` 的 `breakerId` 用玩家 entityId，多个玩家同时烧同一块会互相覆盖裂纹 | 第一版接受；需要的话改成按 (世界,坐标) 汇总进度、用固定 breakerId |
| 4 | 高射速下每发一个光束实体 → 实体数量 | `BeamFactory` 把 TTL 压到 `minFiretime + 1`（一般 3~5 tick），视野内通常 1~3 个实体 |
| 5 | `ForgeHooks.onBlockBreakEvent` 需要 `EntityPlayerMP` | `BeamBurnTracker` 只对 `EntityPlayerMP` 生效；其它射手（NPC/炮塔）走 `world.destroyBlock(pos,false)` |
| 6 | 科技枪的 `handleGunKey` 在双持枪时会劫持右键（L180） | 我们的切档走 `onItemRightClick`/`onItemUseFirst`，不经那条路，不受影响 |
| 7 | 光束先命中实体时，独立射线仍会烧到后面的方块 | 第一版接受；要修就在 `burnAlongRay` 里加一次 `getEntitiesWithinAABB` 粗筛（射线前方 1 格内命中实体则跳过烧穿） |
| 8 | 客户端也会调 `shootGun`（特效分支），若在客户端也跑 `burnAlongRay` 会双份进度 | `burnAlongRay` 只在 `!world.isRemote` 调用（§5.1 已如此），且 `BeamBurnTracker` 内部再挡一次 `EntityPlayerMP` |
| 9 | 弹丸 `LaserProjectile` 的命中判定与我们的服务端射线是**两次独立射线**，极端情况下可能差一格 | 两次都用 `player.getLookVec()`/弹丸 motion，正常一致；若实测有偏差，把 `burnAlongRay` 改成用弹丸的 `onHit` 结果（但要回到「构造期字段未就绪」的老问题，不推荐） |

---

## 11. 附：v1 的「工具路径」方案还留着吗

留着，但降级为**备选**。本仓库 `GenericGunMeleeBase` + v1 的右键切档（`onItemRightClick` +
`onItemUseFirst` + `isEffectiveToolForState` 覆写）就是完整的「假工具」实现，
如果想先用一天时间验证「右键切档」这件事本身，直接拿它 + `ItemChainsword` 改造最快。
确认手感后再切到本 v2 的光束方案，切档部分的代码（`IModeSwitchable`）一行都不用改。
