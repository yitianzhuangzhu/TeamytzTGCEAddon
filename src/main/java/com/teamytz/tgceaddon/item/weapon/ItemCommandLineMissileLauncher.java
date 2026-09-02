package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.TGSounds;
import techguns.items.guns.GenericGun;
import techguns.items.guns.ProjectileSelector;

/**
 * 指令线制导导弹发射器
 *
 * 直接发射指令线制导导弹(无需锁定):导弹每 tick 跟随射手玩家鼠标准星
 * 指向的目标飞行。参数套用科技枪原版制导导弹(匀速、火箭爆炸)。
 */
public class ItemCommandLineMissileLauncher extends GenericGun {

    public ItemCommandLineMissileLauncher(String name, ProjectileSelector projectile_selector) {
        // 科技枪 guidedmissilelauncher 参数:
        // addToGunList=true, semiAuto=true, minFiretime=10, clipsize=1, reloadtime=40,
        // damage=60, firesound=GUIDEDMISSILE_FIRE, reloadsound=ROCKET_RELOAD, TTL=300, accuracy=0.05
        super(true, name, projectile_selector, true, 10, 1, 40, 60.0f,
                TGSounds.GUIDEDMISSILE_FIRE, TGSounds.ROCKET_RELOAD, 300, 0.05f);

        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);

        // 匀速飞行(无动力段加速);初速 1.0 格/tick(鞘翅速度叠加继承仍生效,总速 ≈ 1.0+滑翔速度;
        // 玩家武器射速不应接近炮塔(罗兰 3.0))
        this.setBulletSpeed(1.0f);
        this.setRecoiltime(10);
        // 爆炸衰减:2 格内满伤害,2~4 格衰减到 50,4 格外无伤害(避免全图爆炸)
        this.setDamageDrop(2f, 4f, 50f);
        // 纹理
        this.setTexture(new ResourceLocation(TGCEAddon.MODID + ":textures/guns/command_line_missile_launcher"));
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
                new ModelResourceLocation(TGCEAddon.MODID + ":command_line_missile_launcher", "inventory"));
    }
}
