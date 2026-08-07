package com.teamytz.tgceaddon.item.weapon;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.item.ItemPureMandate;
import techguns.TGSounds;
import techguns.api.damagesystem.DamageType;
import techguns.api.guns.GunHandType;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.ChainsawProjectile;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.items.guns.ChargedProjectileSelector;
import techguns.items.guns.EnumCrosshairStyle;
import techguns.items.guns.IChargedProjectileFactory;
import techguns.items.guns.ammo.AmmoTypes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPowerSword extends GenericGunMeleeBase {
    public static ItemPowerSword INSTANCE;

    /**
     * 记录刚通过左键攻击消耗完弹药的玩家（服务端），
     * 用于阻止攻击后的 onEntitySwing 自动触发装填。
     * 攻击消耗弹药到 0 是攻击的副作用，不应触发装填；
     * 只有玩家在弹药=0 时主动左键才装填。
     */
    private static final Set<EntityPlayer> JUST_ATTACKED_TO_EMPTY =
            Collections.newSetFromMap(new WeakHashMap<>());

    public ItemPowerSword(String name) {
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
            true,
            3,
            100,
            200,
            25.0f,
            TGSounds.POWERHAMMER_SWING,
            TGSounds.POWERHAMMER_RELOAD,
            3,
            0.0f
        );
        INSTANCE = this;

        this.setSwingSoundDelay(5);
        this.setHasCustomAnim(true);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);

        this.setHandType(GunHandType.ONE_HANDED);

        this.setMeleeDmg(25.0f, 15.0f);
        this.setShootWithLeftClick(false);
        this.setBulletSpeed(1.0f);
        this.setCrossHair(EnumCrosshairStyle.VANILLA);

        this.setTexture(new ResourceLocation(TGCEAddon.MODID, "textures/guns/powersword"));
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean canDestroyBlockInCreative(World worldIn, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0.0f;
    }

    /**
     * 佩戴纯洁圣印时 35% 概率不消耗近战弹药
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
        // 攻击消耗弹药到 0 后的 swingArm 会触发本方法，这种情况不应装填
        if (entityLiving instanceof EntityPlayer && JUST_ATTACKED_TO_EMPTY.remove(entityLiving)) {
            return super.onEntitySwing(entityLiving, stack);
        }
        // 弹药耗尽时，任意左键（挥动，包括空气/方块）尝试装填
        if (this.getCurrentAmmo(stack) <= 0 && entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            EnumHand hand = player.getHeldItemMainhand() == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
            int ammoBefore = this.getCurrentAmmo(stack);
            // tryForcedReload 内部已处理：fireDelay CD 检查、弹药消耗、音效、动画、网络同步
            this.tryForcedReload(stack, player.world, player, hand);
            int ammoAfter = this.getCurrentAmmo(stack);
            // 装填成功则取消默认挥砍动画，让装填动画独占
            if (ammoAfter > ammoBefore) {
                return this.hasCustomAnim;
            }
            // 物品栏无弹药，允许攻击（伤害=meleeDmgEmpty=15，ammoLeft=0 不显示 lightning 动画）
        }
        return super.onEntitySwing(entityLiving, stack);
    }

    @Override
    public net.minecraft.util.SoundEvent getSwingSound() {
        return TGSounds.POWERHAMMER_SWING;
    }

    @Override
    public net.minecraft.util.SoundEvent getBlockBreakSound() {
        return TGSounds.POWERHAMMER_IMPACT;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity targetEntity) {
        int ammo = this.getCurrentAmmo(stack);
        if (ammo <= 0) {
            // 弹药耗尽时，物品栏有弹药则装填并取消本次攻击
            EnumHand hand = player.getHeldItemMainhand() == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
            int ammoBefore = this.getCurrentAmmo(stack);
            this.tryForcedReload(stack, player.world, player, hand);
            int ammoAfter = this.getCurrentAmmo(stack);
            if (ammoAfter > ammoBefore) {
                // 装填成功，取消攻击（不会触发后续 swingArm/onEntitySwing）
                return true;
            }
            // 物品栏无弹药，允许攻击（伤害=15，无 lightning 动画）
            return super.onLeftClickEntity(stack, player, targetEntity);
        }
        // ammo>0：走父类攻击逻辑（消耗弹药）
        boolean result = super.onLeftClickEntity(stack, player, targetEntity);
        // 攻击后若弹药耗尽，标记以跳过随后的 onEntitySwing 自动装填（服务端）
        if (this.getCurrentAmmo(stack) <= 0 && !player.world.isRemote) {
            JUST_ATTACKED_TO_EMPTY.add(player);
        }
        return result;
    }

    /**
     * 动力剑不触发科技枪默认的剑式横扫群攻
     */
    @Override
    protected boolean hasSwordSweep() {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, 
            new ModelResourceLocation(TGCEAddon.MODID + ":powersword", "inventory"));
    }
}
