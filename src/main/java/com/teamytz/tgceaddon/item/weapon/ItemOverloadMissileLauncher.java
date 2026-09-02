package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.TGSounds;
import techguns.items.guns.ChargedProjectileSelector;
import techguns.items.guns.GuidedMissileLauncher;

/**
 * 过载制导导弹发射器
 *
 * 复用科技枪 GuidedMissileLauncher 的锁定/充能/发射机制(长按右键锁定目标,
 * 锁定完成后发射过载限制制导导弹 OverloadGuidedMissileProjectile)。
 * 与科技枪原版导弹不同,发射的导弹机动受过载限制,可以被玩家/生物躲避。
 */
public class ItemOverloadMissileLauncher extends GuidedMissileLauncher {

    public ItemOverloadMissileLauncher(String name, ChargedProjectileSelector projectile_selector) {
        // 参数参照科技枪 guidedmissilelauncher:
        // semiAuto=true, minFiretime=10, clipsize=1, reloadtime=40, damage=60,
        // firesound=GUIDEDMISSILE_FIRE, reloadsound=ROCKET_RELOAD, TTL=300(15秒,支持远程目标),
        // accuracy=0.05, fullChargeTime=200, ammoConsumedOnFullCharge=1
        super(name, projectile_selector, true, 10, 1, 40, 60.0f, TGSounds.GUIDEDMISSILE_FIRE, TGSounds.ROCKET_RELOAD,
                300, 0.05f, 200, 1);

        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);

        // 充能期间持续发射(与科技枪原版一致)
        this.setFireWhileCharging(true);
        this.setChargeFireAnims(false);
        // 初速度 1.5 格/tick(高于玩家冲刺速度),动力段线性加速到约 3.5
        this.setBulletSpeed(1.5f);
        this.setRecoiltime(10);
        // 锁定:20 ticks 完成锁定,锁定完成后额外保持 80 ticks
        this.setLockOn(20, 80);
        // 爆炸衰减:2 格内满伤害,2~4 格衰减到 50,4 格外无伤害
        // 必须显式设置!GenericGun 构造会把 damageDropStart/End 默认设为 TTL(=100),
        // 导致 TGExplosion 爆炸半径 = 100 格,单次爆炸波及全图实体并造成服务器卡顿
        this.setDamageDrop(2f, 4f, 50f);
        // 纹理
        this.setTexture(new ResourceLocation(TGCEAddon.MODID + ":textures/guns/overload_missile_launcher"));
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
                new ModelResourceLocation(TGCEAddon.MODID + ":overload_missile_launcher", "inventory"));
    }
}
