package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import com.teamytz.tgceaddon.init.ModAmmoTypes;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.item.ItemPureMandate;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import techguns.TGSounds;
import techguns.api.damagesystem.DamageType;
import techguns.client.ShooterValues;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.items.guns.ProjectileSelector;

/**
 * 爆弹枪 - 发射爆炸弹的半自动步枪
 */
public class ItemBolter extends GenericGun {

    public ItemBolter(String name) {
        super(
            true,   // addToGunList - 添加到枪械列表
            name,
            new ProjectileSelector<>(
                ModAmmoTypes.BOLT_MAGAZINE,  // 使用爆弹弹夹
                new IProjectileFactory<BoltProjectile>() {
                    @Override
                    public BoltProjectile createProjectile(
                            GenericGun gun, World world, EntityLivingBase p, 
                            float damage, float speed, int TTL, float spread, 
                            float dmgDropStart, float dmgDropEnd, float dmgMin, 
                            float penetration, boolean blockdamage, 
                            EnumBulletFirePos firePos, float radius, double gravity) {
                        return new BoltProjectile(
                            world, p, damage * 1.15f, speed, TTL, spread, 
                            dmgDropStart, dmgDropEnd, dmgMin, penetration, 
                            blockdamage, firePos
                        );
                    }
                    @Override
                    public DamageType getDamageType() {
                        return DamageType.EXPLOSION;
                    }
                }
            ),
            false,  // semiAuto - 不是半自动（自动步枪）
            3,      // minFiretime - 最小射击间隔（3 ticks，加快射速）
            30,     // clipsize - 弹夹容量30发
            60,     // reloadtime - 装填时间（60 ticks = 3秒）
            25.0f,  // damage - 基础伤害25
            TGSounds.BOLT_ACTION_FIRE,   // 射击音效
            TGSounds.BOLT_ACTION_RELOAD,  // 装填音效
            60,     // TTL - 投射物生存时间
            0.01f   // accuracy - 精度（非常精准）
        );
        
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        
        // 设置缩放功能 - 右键瞄准
        this.setZoom(0.25f, true, 0.5f, true);
        
        // 设置后坐力时间
        this.setRecoiltime(4);
        
        // 设置弹药消耗（每发消耗1个弹药）
        this.setAmmoCount(1);
        
        // 设置投射物速度
        this.setBulletSpeed(5.0f);
        
        // 设置穿透力
        this.setPenetration(0.5f);
        
        // 设置纹理
        this.setTexture(new ResourceLocation("tgceaddon:textures/guns/bolter"));
    }

    /**
     * 佩戴纯洁圣印时 25% 概率不消耗射击弹药。
     * shootGunPrimary 内部调用 useAmmo，通过 ThreadLocal 传递标志。
     */
    @Override
    public void shootGunPrimary(ItemStack stack, World world, EntityPlayer player, boolean zooming, EnumHand hand, Entity target) {
        ItemPureMandate.AMMO_SAVE_FLAG.set(ItemPureMandate.isEquipped(player));
        super.shootGunPrimary(stack, world, player, zooming, hand, target);
        ItemPureMandate.AMMO_SAVE_FLAG.remove();
    }

    @Override
    public int useAmmo(ItemStack stack, int amount) {
        if (amount == 1 && ItemPureMandate.AMMO_SAVE_FLAG.get()
                && new java.util.Random().nextFloat() < 0.35f) {
            return 0;
        }
        return super.useAmmo(stack, amount);
    }

    @Override
    public void gunSecondaryAction(EntityPlayer player, ItemStack stack) {
        // 修复更换弹药后缩放失效的问题
        if (player.world.isRemote) {
            long reloadTime = ShooterValues.getReloadtime(player, false);
            long currentTime = System.currentTimeMillis();
            
            // 如果 reloadTime 不是 0 且当前时间已经超过 reloadTime，说明装填已完成
            // 但状态未正确重置，需要强制重置以恢复缩放功能
            if (reloadTime > 0 && currentTime > reloadTime) {
                ShooterValues.setReloadtime(player, false, 0, 0, (byte) 0);
            }
        }
        
        super.gunSecondaryAction(player, stack);
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(TGCEAddon.MODID + ":bolter", "inventory"));
    }
}
