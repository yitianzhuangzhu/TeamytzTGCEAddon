package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.TGSounds;
import techguns.api.damagesystem.DamageType;
import techguns.client.ShooterValues;
import techguns.entities.projectiles.GenericProjectile;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.items.guns.GenericGun;
import techguns.items.guns.IProjectileFactory;
import techguns.items.guns.ProjectileSelector;
import techguns.items.guns.ammo.AmmoTypes;

/**
 * 测试手枪 - 修复缩放问题
 */
public class ItemTestPistol extends GenericGun {

    public ItemTestPistol(String name) {
        super(
            true,  // addToGunList
            name,
            new ProjectileSelector<>(
                AmmoTypes.PISTOL_ROUNDS,
                new IProjectileFactory<GenericProjectile>() {
                    @Override
                    public GenericProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p, 
                        float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, 
                        float dmgMin, float penetration, boolean blockdamage, 
                        EnumBulletFirePos firePos, float radius, double gravity) {
                        return new GenericProjectile(
                            world, p, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, 
                            penetration, blockdamage, firePos
                        );
                    }
                    @Override
                    public DamageType getDamageType() {
                        return DamageType.PROJECTILE;
                    }
                },
                new IProjectileFactory<GenericProjectile>() {
                    @Override
                    public GenericProjectile createProjectile(GenericGun gun, World world, EntityLivingBase p, 
                        float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, 
                        float dmgMin, float penetration, boolean blockdamage, 
                        EnumBulletFirePos firePos, float radius, double gravity) {
                        return new GenericProjectile(
                            world, p, damage * 1.2f, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, 
                            penetration, blockdamage, firePos
                        );
                    }
                    @Override
                    public DamageType getDamageType() {
                        return DamageType.PROJECTILE;
                    }
                }
            ),
            false,   // semiAuto
            6,       // minFiretime (ticks)
            12,      // clipsize
            40,      // reloadtime (ticks)
            8.0f,    // damage
            TGSounds.PISTOL_FIRE,
            TGSounds.PISTOL_RELOAD,
            40,      // TTL
            0.02f    // accuracy
        );
        
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(null); // 从创造模式物品栏移除
        
        this.setZoom(0.25f, true, 0.5f, true);
        this.setBulletSpeed(3.5f);
        this.setPenetration(1.0f);
        
        // 设置纹理
        this.setTexture(new ResourceLocation("techguns:textures/guns/pistol"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void gunSecondaryAction(EntityPlayer player, ItemStack stack) {
        // 检查是否应该允许缩放
        // reloadTime 是一个时间戳（System.currentTimeMillis() + duration）
        // 如果当前时间已经超过 reloadTime，说明装填已经完成
        if (player.world.isRemote) {
            long reloadTime = ShooterValues.getReloadtime(player, false);
            long currentTime = System.currentTimeMillis();

            // 如果 reloadTime 不是 0 且当前时间已经超过 reloadTime，说明装填已完成
            // 但 isReloading() 仍返回 true，需要强制重置
            if (reloadTime > 0 && currentTime > reloadTime) {
                // 装填已完成但状态未重置，强制重置
                ShooterValues.setReloadtime(player, false, 0, 0, (byte) 0);
            }
        }

        super.gunSecondaryAction(player, stack);
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation("techguns:pistol", "inventory"));
    }
}