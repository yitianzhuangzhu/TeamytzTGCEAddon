package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.item.ItemPureMandate;
import techguns.TGSounds;
import techguns.api.damagesystem.DamageType;
import techguns.entities.projectiles.ChainsawProjectile;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.items.guns.ChargedProjectileSelector;
import techguns.items.guns.EnumCrosshairStyle;
import techguns.items.guns.GenericGunMeleeCharge;
import techguns.items.guns.IChargedProjectileFactory;
import techguns.items.guns.ammo.AmmoTypes;
import techguns.TGItems;
import techguns.damagesystem.TGDamageSource;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemChainsword extends GenericGunMeleeCharge {
    public static ItemChainsword INSTANCE;

    private static final double SWEEP_RADIUS = 2.0D;
    private static final double SWEEP_HEIGHT = 1.0D;

    public ItemChainsword(String name) {
        super(
            name,
            new ChargedProjectileSelector<>(
                AmmoTypes.FUEL_TANK,
                new IChargedProjectileFactory<ChainsawProjectile>() {
                    @Override
                    public ChainsawProjectile createProjectile(techguns.items.guns.GenericGun gun, World world, EntityLivingBase entity, float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockDamage, EnumBulletFirePos firePos, float radius, double gravity) {
                        return new ChainsawProjectile(world, entity, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockDamage, firePos);
                    }
                    
                    @Override
                    public ChainsawProjectile createChargedProjectile(World world, EntityLivingBase entity, float damage, float speed, int TTL, float spread, float dmgDropStart, float dmgDropEnd, float dmgMin, float penetration, boolean blockDamage, EnumBulletFirePos firePos, float radius, double gravity, float chargeProgress, int ammoConsumed) {
                        return new ChainsawProjectile(world, entity, damage, speed, TTL, spread, dmgDropStart, dmgDropEnd, dmgMin, penetration, blockDamage, firePos);
                    }
                    
                    @Override
                    public DamageType getDamageType() {
                        return DamageType.PHYSICAL;
                    }
                }
            ),
            false,
            3,
            20,
            200,
            20.0f,
            TGSounds.CHAINSAW_LOOP,
            TGSounds.PISTOL_RELOAD,
            3,
            0.0f,
            0.0f,
            1
        );
        INSTANCE = this;
        
        this.setMiningHeads(
            TGItems.CHAINSAWBLADES_OBSIDIAN, 
            TGItems.CHAINSAWBLADES_CARBON
        );
        
        this.setMiningRadius(0);
        this.setSwingSoundDelay(5);
        this.setHasCustomAnim(true);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        
        this.setMeleeDmg(20.0f, 5.0f);
        this.setShootWithLeftClick(false);
        this.setBulletSpeed(1.0f);
        this.setCrossHair(EnumCrosshairStyle.VANILLA);
        
        // 使用 ResourceLocation 设置纹理，科技枪会自动添加 .png 后缀
        this.setTexture(new ResourceLocation(TGCEAddon.MODID, "textures/guns/chainsword"));
    }
    
    @Override
    public float getExtraDigSpeed(ItemStack stack) {
        int headlevel = this.getMiningHeadLevel(stack);
        return 3.0f * headlevel;
    }

    /**
     * 佩戴纯洁圣印时 25% 概率不消耗近战弹药
     */
    @Override
    protected void consumeAmmoOnMeleeHit(EntityLivingBase elb, ItemStack stack) {
        if (elb instanceof EntityPlayer && ItemPureMandate.tryAmmoSave((EntityPlayer) elb)) {
            return;
        }
        super.consumeAmmoOnMeleeHit(elb, stack);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (this.getCurrentAmmo(stack) > 0) {
            return super.onEntitySwing(entityLiving, stack);
        }
        return this.hasCustomAnim;
    }
    
    @Override
    protected SoundEvent getSwingSound() {
        return TGSounds.CHAINSAW_LOOP;
    }
    
    @Override
    protected SoundEvent getBlockBreakSound() {
        return TGSounds.CHAINSAW_HIT;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        this.shootGunPrimary(stack, world, player, false, hand, null);
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }
    
    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity targetEntity) {
        boolean result = super.onLeftClickEntity(stack, player, targetEntity);
        
        // 只要有弹药就能触发横扫
        if (this.getCurrentAmmo(stack) > 0 && targetEntity instanceof EntityLivingBase) {
            doSweepAttack(player, stack, 20.0f);
            
            // 触发横扫动画
            if (player.world.isRemote) {
                // 设置后坐力时间来触发动画播放
                techguns.client.ShooterValues.setRecoiltime(player, false, System.currentTimeMillis() + 150, 150, (byte) 1);
            }
        }
        
        return result;
    }
    
    private void doSweepAttack(EntityPlayer player, ItemStack stack, float damage) {
        World world = player.world;
        float sweepDamage = damage * 0.5F;
        
        AxisAlignedBB sweepBox = new AxisAlignedBB(
            player.posX - SWEEP_RADIUS, player.posY, player.posZ - SWEEP_RADIUS,
            player.posX + SWEEP_RADIUS, player.posY + SWEEP_HEIGHT, player.posZ + SWEEP_RADIUS
        );
        
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, sweepBox);
        
        for (EntityLivingBase target : targets) {
            if (target != player && !player.isOnSameTeam(target) && target.isEntityAlive()) {
                double dx = target.posX - player.posX;
                double dz = target.posZ - player.posZ;
                double distanceSq = dx * dx + dz * dz;
                
                if (distanceSq <= SWEEP_RADIUS * SWEEP_RADIUS) {
                    float pushX = (float)(dx / Math.sqrt(distanceSq));
                    float pushZ = (float)(dz / Math.sqrt(distanceSq));
                    
                    TGDamageSource src = this.getMeleeDamageSource(player, stack);
                    target.attackEntityFrom(src, sweepDamage);
                    target.addVelocity(pushX * 0.5D, 0.1D, pushZ * 0.5D);
                }
            }
        }
        
        if (!world.isRemote && !targets.isEmpty()) {
            world.playSound(null, player.posX, player.posY, player.posZ, 
                    TGSounds.CHAINSAW_HIT, player.getSoundCategory(), 0.8F, 1.0F);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, 
            new ModelResourceLocation(TGCEAddon.MODID + ":chainsword", "inventory"));
    }
}