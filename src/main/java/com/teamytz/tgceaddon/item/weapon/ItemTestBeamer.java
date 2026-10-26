package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.ModeBeamProjectile;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.item.weapon.mode.GunMode;

import net.minecraft.util.ResourceLocation;
import techguns.TGSounds;
import techguns.api.guns.GunHandType;
import techguns.items.guns.EnumCrosshairStyle;
import techguns.items.guns.ProjectileSelector;
import techguns.items.guns.ammo.AmmoTypes;

/**
 * 测试用光束枪（套科技枪 Nuclear Death Ray 的模型与贴图）。
 *
 * <p>右键换挡，三档：</p>
 * <ol>
 *   <li><b>击晕</b> —— 失明 + 缓慢 + 挖掘疲劳（II 级，4 秒），每次命中只造成 0.5 伤害</li>
 *   <li><b>击杀</b> —— 每次命中扣除目标最大生命的 20%，当前生命 ≤ 10% 时直接处决</li>
 *   <li><b>挖掘</b> —— 光束持续烧穿方块（不分硬度上限，掉落正常）</li>
 * </ol>
 *
 * <p>左键开火由科技枪的 {@code TGTickHandler} 驱动（{@code setShootWithLeftClick(true)}），
 * 弹丸是 {@link ModeBeamProjectile}：每 tick 重新对准射手并重新射线，
 * 射速 5 tick + 光束寿命 10 tick ⇒ 恒定有两条光束重叠，看起来是一条连续光束。</p>
 */
public class ItemTestBeamer extends GenericGunModeBase {

    public static ItemTestBeamer INSTANCE;

    /** 射速（tick/发）。光束寿命是 NDRProjectile.BEAM_LIFETIME(=10)，两者配合决定"连续性"。 */
    public static final int FIRE_INTERVAL = 5;

    public ItemTestBeamer(String name) {
        super(name,
                new ProjectileSelector<>(AmmoTypes.ENERGY_CELL, new ModeBeamProjectile.Factory()),
                /*semiAuto*/ false,
                /*minFiretime*/ FIRE_INTERVAL,
                /*clipsize*/ 20,
                /*reloadtime*/ 50,
                /*damage*/ 0.0f,
                TGSounds.BEAMGUN_FIRE,
                TGSounds.LASERGUN_RELOAD,
                /*TTL*/ 100,
                /*accuracy*/ 0.0f);
        INSTANCE = this;

        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);

        // 手感/音效/弹道：全部照 NDR 抄，保证"同一把枪"的感觉
        this.setFiresoundStart(TGSounds.BEAMGUN_START);
        this.setMaxLoopDelay(10);
        this.setRecoiltime(10);
        this.setCheckRecoil();
        this.setBulletSpeed(100.0f);   // 光束射线长度 = motion → 100 格
        this.setPenetration(0.5f);
        this.setHandType(GunHandType.TWO_HANDED);
        this.setCrossHair(EnumCrosshairStyle.TRI);

        // 复用科技枪 NDR 的枪体贴图（枪模型由 ClientProxy 的 ItemRenderHack + ModelNDR 渲染）
        this.setTexture(new ResourceLocation("techguns", "textures/guns/ndr"));

        this.setModes(
                // 击晕：0.5 伤害 + 失明/缓慢/挖掘疲劳 II，4 秒（每次命中刷新）
                GunMode.stun("stun", 0.5f, 80, 1),
                // 击杀：每次 20% 最大生命；当前生命 ≤ 10% 直接处决
                GunMode.kill("kill", 0.0f, 0.20f, 0.10f),
                // 挖掘：每 tick 累积 1.5 点，硬度上限不限（-1），掉落正常
                GunMode.mine("mine", 0.0f, 1.5f, -1.0f, true)
        );
    }
}
