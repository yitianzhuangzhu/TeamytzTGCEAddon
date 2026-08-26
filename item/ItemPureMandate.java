package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 纯洁圣印 - Baubles 饰品（CHARM 槽）
 * <p>佩戴效果：
 * <br>1. 链锯剑/动力剑/爆弹枪/雷霆锤攻击时 25% 概率不消耗弹药
 * <br>2. 生命值 ≤ 4 点时触发附魔金苹果效果（60秒冷却）
 * <p>通过 Capability 注册为饰品，软依赖 Baubles。
 */
public class ItemPureMandate extends Item {

    public static ItemPureMandate INSTANCE;

    /** 射击武器弹药节省标志（ThreadLocal 保证线程隔离） */
    public static final ThreadLocal<Boolean> AMMO_SAVE_FLAG = ThreadLocal.withInitial(() -> false);

    public ItemPureMandate(String name) {
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setRegistryName(TGCEAddon.MODID, name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation("tgceaddon:pure_mandate", "inventory"));
    }

    /**
     * 检测玩家是否佩戴了纯洁圣印
     */
    public static boolean isEquipped(EntityPlayer player) {
        if (player == null || !Loader.isModLoaded("baubles")) return false;
        return baubles.api.BaublesApi.isBaubleEquipped(player, INSTANCE) != -1;
    }

    /**
     * 尝试弹药节省：佩戴圣印时 35% 概率返回 true（表示本次攻击不消耗弹药）
     */
    public static boolean tryAmmoSave(EntityPlayer player) {
        return isEquipped(player) && ThreadLocalRandom.current().nextFloat() < 0.35f;
    }

    /**
     * 佩戴时每 tick 调用：生命值 ≤ 4 时触发附魔金苹果效果（60秒冷却）
     */
    public static void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (entity.world.isRemote || !(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;
        // 每秒检查一次
        if (player.ticksExisted % 20 != 0) return;
        // 生命值 ≤ 4 点（2颗心）
        if (player.getHealth() > 4.0f) return;
        // 冷却检查（60秒 = 1200 ticks）
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) {
            tags = new NBTTagCompound();
            stack.setTagCompound(tags);
        }
        long now = player.world.getTotalWorldTime();
        long last = tags.getLong("lastRescue");
        if (now - last < 1200L) return;
        // 给予附魔金苹果效果
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));   // 5秒 生命恢复 II
        player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 400, 3));     // 20秒 伤害吸收 IV
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 200, 4));     // 10秒 抗性提升 V
        player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 200, 0)); // 10秒 防火
        tags.setLong("lastRescue", now);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        if (Loader.isModLoaded("baubles")) {
            return new BaubleProvider();
        }
        return null;
    }

    /**
     * Baubles 饰品 Capability 提供者。
     * 此类引用 baubles.api 类，仅在 Baubles 加载时才会被 JVM 加载（懒加载机制）。
     */
    private static class BaubleProvider implements ICapabilitySerializable<NBTBase> {

        private final baubles.api.IBauble bauble = new baubles.api.IBauble() {
            @Override
            public baubles.api.BaubleType getBaubleType(ItemStack stack) {
                return baubles.api.BaubleType.CHARM;
            }

            @Override
            public void onWornTick(ItemStack stack, EntityLivingBase entity) {
                ItemPureMandate.onWornTick(stack, entity);
            }
        };

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == baubles.api.cap.BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == baubles.api.cap.BaublesCapabilities.CAPABILITY_ITEM_BAUBLE) {
                return baubles.api.cap.BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(this.bauble);
            }
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
        }
    }
}
