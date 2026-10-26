package com.teamytz.tgceaddon.item.weapon.mode;

/**
 * 枪械「左键功能」档位的类型。
 *
 * <p>档位的具体数值在 {@link GunMode} 里，效果由光束弹丸
 * ({@code com.teamytz.tgceaddon.entities.projectiles.ModeBeamProjectile}) 按本枚举分支消费。</p>
 */
public enum GunModeFunction {

    /** 击晕：给实体失明 / 缓慢 / 挖掘疲劳，只造成极低伤害。 */
    STUN,

    /** 击杀：按目标最大生命的百分比扣血，血量压到阈值以下时直接秒杀。 */
    KILL,

    /** 挖掘：光束持续烧穿方块（不按原版工具那套算，见 BeamBurnTracker）。 */
    MINE
}
