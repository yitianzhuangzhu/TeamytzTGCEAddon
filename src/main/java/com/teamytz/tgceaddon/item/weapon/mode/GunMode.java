package com.teamytz.tgceaddon.item.weapon.mode;

/**
 * 一个「档位」的全部参数（不可变）。
 *
 * <p>所有字段都是 public final，子类/弹丸直接读取；用静态工厂构造，
 * 避免子类写出只填了一半的档位。</p>
 *
 * <p>三个功能轴各自只用到自己的那几个字段，其余字段保持默认值即可：</p>
 * <ul>
 *   <li>{@link GunModeFunction#STUN}：hitDamage / stunDuration / stunAmplifier</li>
 *   <li>{@link GunModeFunction#KILL}：hitDamage / killHealthPercent / killExecuteThreshold</li>
 *   <li>{@link GunModeFunction#MINE}：hitDamage / burnPower / mineMaxHardness / mineDropBlocks</li>
 * </ul>
 */
public final class GunMode {

    /** 语言键后缀（"stun" → {@code item.tgceaddon.<枪名>.mode.stun}）。 */
    public final String id;

    /** 左键功能类型。 */
    public final GunModeFunction function;

    /** 每次命中造成的固定伤害（0 = 纯效果档，不打伤害）。 */
    public final float hitDamage;

    // ===== STUN 档 =====

    /** 失明/缓慢/挖掘疲劳的持续时间（tick）。 */
    public final int stunDuration;

    /** 药水效果等级（0 = I 级，1 = II 级 …）。 */
    public final int stunAmplifier;

    // ===== KILL 档 =====

    /** 每次命中扣除「目标最大生命」的百分比（0.20 = 每次 20%）。 */
    public final float killHealthPercent;

    /** 目标当前生命 ≤ 最大生命 × 该比例时直接秒杀（0.10 = 低于 10% 处决）。 */
    public final float killExecuteThreshold;

    // ===== MINE 档 =====

    /** 每次命中累积的方块破坏点数（详见 BeamBurnTracker 的数值模型）。 */
    public final float burnPower;

    /** 能烧穿的方块硬度上限（< 0 = 不限，黑曜石这类极硬的也能慢慢烧）。 */
    public final float mineMaxHardness;

    /** 烧穿后是否掉落物品（false = 直接气化）。 */
    public final boolean mineDropBlocks;

    private GunMode(String id, GunModeFunction function, float hitDamage,
                    int stunDuration, int stunAmplifier,
                    float killHealthPercent, float killExecuteThreshold,
                    float burnPower, float mineMaxHardness, boolean mineDropBlocks) {
        this.id = id;
        this.function = function;
        this.hitDamage = hitDamage;
        this.stunDuration = stunDuration;
        this.stunAmplifier = stunAmplifier;
        this.killHealthPercent = killHealthPercent;
        this.killExecuteThreshold = killExecuteThreshold;
        this.burnPower = burnPower;
        this.mineMaxHardness = mineMaxHardness;
        this.mineDropBlocks = mineDropBlocks;
    }

    /** 击晕档：极低伤害 + 失明/缓慢/挖掘疲劳。 */
    public static GunMode stun(String id, float hitDamage, int durationTicks, int amplifier) {
        return new GunMode(id, GunModeFunction.STUN, hitDamage,
                Math.max(1, durationTicks), Math.max(0, amplifier),
                0.0f, 0.0f, 0.0f, -1.0f, false);
    }

    /** 击杀档：按最大生命百分比扣血，低于阈值直接处决。 */
    public static GunMode kill(String id, float hitDamage, float healthPercentPerHit, float executeThreshold) {
        return new GunMode(id, GunModeFunction.KILL, hitDamage,
                0, 0,
                clamp(healthPercentPerHit, 0.0f, 1.0f), clamp(executeThreshold, 0.0f, 1.0f),
                0.0f, -1.0f, false);
    }

    /** 挖掘档：光束烧穿方块。 */
    public static GunMode mine(String id, float hitDamage, float burnPower, float maxHardness, boolean dropBlocks) {
        return new GunMode(id, GunModeFunction.MINE, hitDamage,
                0, 0, 0.0f, 0.0f,
                Math.max(0.0f, burnPower), maxHardness, dropBlocks);
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    @Override
    public String toString() {
        return "GunMode{" + id + ", " + function + ", dmg=" + hitDamage + "}";
    }
}
