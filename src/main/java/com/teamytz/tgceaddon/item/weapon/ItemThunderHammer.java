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
import java.util.Map;
import java.util.WeakHashMap;

public class ItemThunderHammer extends GenericGunMeleeCharge {
    public static ItemThunderHammer INSTANCE;

    // 地面重击参数
    private static final float GROUND_SLAM_MIN_FALL_DISTANCE = 1.5f;  // 最小掉落高度触发阈值
    private static final float DAMAGE_PER_BLOCK = 5.0f;                // 每格掉落增加的伤害
    private static final float MAX_EXTRA_DAMAGE = 965.0f;              // 最大额外伤害
    private static final double GROUND_SLAM_RADIUS = 8.0D;             // 群攻半径（加大）
    private static final double GROUND_SLAM_HEIGHT = 4.0D;             // 群攻高度范围
    private static final float KNOCKBACK_PER_BLOCK = 0.15f;            // 每格掉落增加的击退力度
    private static final float MAX_KNOCKBACK = 3.0f;                   // 最大击退力度（加大）
    private static final double SHOCKWAVE_RADIUS = 3.0D;              // 右键冲击范围（3格）
    private static final double SHOCKWAVE_HEIGHT = 4.0D;              // 右键冲击高度范围
    private static final float SHOCKWAVE_DAMAGE = 15.0f;            // 右键冲击基础伤害
    private static final float SHOCKWAVE_KNOCKBACK_PER_BLOCK = 0.08f; // 冲击每格击退力度（较小）
    private static final float SHOCKWAVE_MAX_KNOCKBACK = 1.5f;       // 冲击最大击退力度

    // 存储玩家在空中时记录的fallDistance（关键：Minecraft落地时会重置fallDistance）
    private static final Map<EntityPlayer, Float> fallDistanceRecord = new WeakHashMap<>();

    public ItemThunderHammer(String name) {
        super(
            name,
            new ChargedProjectileSelector<>(
                AmmoTypes.ENERGY_CELL,
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
            true, //半自动，也就是只能一次一次点，不能长按
            3,
            20,
            50,
            35.0f,  // 的伤害
            TGSounds.POWERHAMMER_SWING,
            TGSounds.POWERHAMMER_RELOAD,
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
        
        // 更高的近战伤害
        this.setMeleeDmg(35.0f, 10.0f);
        this.setShootWithLeftClick(false);
        this.setBulletSpeed(1.0f);
        this.setCrossHair(EnumCrosshairStyle.VANILLA);
        
        // 设置纹理
        this.setTexture(new ResourceLocation(TGCEAddon.MODID, "textures/guns/thunderhammer"));
    }
    
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        
        // 只有玩家手持该武器且在服务器端时才记录fallDistance
        if (!worldIn.isRemote && isSelected && entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            
            // 玩家在空中时，持续记录fallDistance（关键：Minecraft落地时会重置fallDistance）
            if (!player.onGround) {
                fallDistanceRecord.put(player, player.fallDistance);
            }
        }
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
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity targetEntity) {
        int ammo = this.getCurrentAmmo(stack);
        
        // 弹药耗尽时左键装填
        if (ammo <= 0) {
            this.reloadAmmo(stack);
            return true;
        }
        
        // 获取当前fallDistance和记录的fallDistance（群攻和额外伤害使用相同的判定逻辑）
        float fallDist = player.fallDistance;
        float recordedFallDist = fallDistanceRecord.getOrDefault(player, 0.0f);
        float actualFallDist = Math.max(fallDist, recordedFallDist);
        
        // 群攻触发条件：掉落高度超过阈值且有弹药
        if (actualFallDist > GROUND_SLAM_MIN_FALL_DISTANCE && ammo > 0) {
            // 计算额外伤害
            float extraDamage = actualFallDist * DAMAGE_PER_BLOCK;
            extraDamage = Math.min(extraDamage, MAX_EXTRA_DAMAGE);
            
            // 执行地面重击群攻
            doGroundSlamAttack(player, stack, extraDamage, actualFallDist);
            
            // 免除本次摔落伤害
            player.fallDistance = 0.0f;
            
            // 清除记录的fallDistance
            fallDistanceRecord.remove(player);
        } else if (actualFallDist <= GROUND_SLAM_MIN_FALL_DISTANCE) {
        }
        
        return super.onLeftClickEntity(stack, player, targetEntity);
    }
    
    /**
     * 执行地面重击群攻，包含伤害、击退和死亡动画
     */
    private void doGroundSlamAttack(EntityPlayer player, ItemStack stack, float extraDamage, float fallDistance) {
        World world = player.world;
        
        // 创建群攻检测范围
        AxisAlignedBB aabb = new AxisAlignedBB(
            player.posX - GROUND_SLAM_RADIUS, player.posY - GROUND_SLAM_HEIGHT, player.posZ - GROUND_SLAM_RADIUS,
            player.posX + GROUND_SLAM_RADIUS, player.posY + GROUND_SLAM_HEIGHT, player.posZ + GROUND_SLAM_RADIUS
        );
        
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        
        // 计算击退力度
        float knockbackStrength = fallDistance * KNOCKBACK_PER_BLOCK;
        knockbackStrength = Math.min(knockbackStrength, MAX_KNOCKBACK);
        
        // 获取科技枪伤害源（确保触发死亡动画）
        TGDamageSource src = this.getMeleeDamageSource(player, stack);
        src.goreChance = 1.0f;  // 强制触发死亡动画
        
        int hitCount = 0;
        for (EntityLivingBase target : targets) {
            if (target != player && !player.isOnSameTeam(target) && target.isEntityAlive()) {
                double dx = target.posX - player.posX;
                double dz = target.posZ - player.posZ;
                double distance = Math.sqrt(dx * dx + dz * dz);
                
                if (distance > 0.01) {
                    // 伤害 = 基础伤害 + 额外伤害
                    float totalDamage = 35.0f + extraDamage;
                    
                    // 造成伤害（会触发科技枪死亡动画）
                    target.attackEntityFrom(src, totalDamage);
                    
                    // 应用击退（力度与掉落高度相关）
                    target.addVelocity(
                        dx / distance * knockbackStrength,
                        0.5D, // 向上击退
                        dz / distance * knockbackStrength
                    );
                    
                    hitCount++;
                }
            }
        }
        
        // 播放重击音效
        if (!world.isRemote) {
            world.playSound(null, player.posX, player.posY, player.posZ, 
                TGSounds.POWERHAMMER_IMPACT, player.getSoundCategory(), 2.0F, 0.8F);
        }
    }
    
    @Override
    protected SoundEvent getSwingSound() {
        return TGSounds.POWERHAMMER_SWING;
    }
    
    @Override
    protected SoundEvent getBlockBreakSound() {
        return TGSounds.POWERHAMMER_IMPACT;
    }
    
    // 右键冲击机制：下落时右键地面产生击退效果，弹药耗尽时右键装填
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        int ammo = this.getCurrentAmmo(stack);
        
        // 弹药耗尽时，让父类处理装填（需要背包中有弹药）
        if (ammo <= 0) {
            return super.onItemRightClick(world, player, hand);
        }
        
        // 在服务器端检测右键冲击
        if (!world.isRemote) {
            float fallDist = player.fallDistance;
            float recordedFallDist = fallDistanceRecord.getOrDefault(player, 0.0f);
            float actualFallDist = Math.max(fallDist, recordedFallDist);
            
            // 下落时触发冲击
            if (actualFallDist > GROUND_SLAM_MIN_FALL_DISTANCE) {
                // 执行冲击效果
                doShockwaveAttack(player, actualFallDist);
                
                // 免除本次摔落伤害
                player.fallDistance = 0.0f;
                
                // 清除记录的fallDistance
                fallDistanceRecord.remove(player);
                
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
    
    /**
     * 执行右键冲击效果，对周围生物造成伤害和击退
     */
    private void doShockwaveAttack(EntityPlayer player, float fallDistance) {
        World world = player.world;
        
        // 创建冲击检测范围
        AxisAlignedBB aabb = new AxisAlignedBB(
            player.posX - SHOCKWAVE_RADIUS, player.posY - SHOCKWAVE_HEIGHT, player.posZ - SHOCKWAVE_RADIUS,
            player.posX + SHOCKWAVE_RADIUS, player.posY + SHOCKWAVE_HEIGHT, player.posZ + SHOCKWAVE_RADIUS
        );
        
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        
        // 计算击退力度（较小）
        float knockbackStrength = fallDistance * SHOCKWAVE_KNOCKBACK_PER_BLOCK;
        knockbackStrength = Math.min(knockbackStrength, SHOCKWAVE_MAX_KNOCKBACK);
        
        // 获取科技枪伤害源
        TGDamageSource src = this.getMeleeDamageSource(player, player.getHeldItemMainhand());
        src.goreChance = 1.0f;
        
        int hitCount = 0;
        for (EntityLivingBase target : targets) {
            if (target != player && !player.isOnSameTeam(target) && target.isEntityAlive()) {
                double dx = target.posX - player.posX;
                double dz = target.posZ - player.posZ;
                double distance = Math.sqrt(dx * dx + dz * dz);
                
                if (distance > 0.01) {
                    // 造成15点伤害
                    target.attackEntityFrom(src, SHOCKWAVE_DAMAGE);
                    
                    // 应用击退
                    target.addVelocity(
                        dx / distance * knockbackStrength,
                        0.5D,
                        dz / distance * knockbackStrength
                    );
                    
                    hitCount++;
                }
            }
        }
        
        // 播放冲击音效
        world.playSound(null, player.posX, player.posY, player.posZ, 
            TGSounds.POWERHAMMER_IMPACT, player.getSoundCategory(), 3.0F, 0.6F);
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, 
            new ModelResourceLocation(TGCEAddon.MODID + ":thunderhammer", "inventory"));
    }
}