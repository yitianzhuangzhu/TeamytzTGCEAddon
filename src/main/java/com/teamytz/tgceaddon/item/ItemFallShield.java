package com.teamytz.tgceaddon.item;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 坠落护身符(饰品)
 *
 * 佩戴在 Baubles 饰品栏后,减免掉落伤害(含鞘翅高速飞行落地/撞击的伤害,
 * 掉落伤害由原版 fallDistance 计算,减免 90%)。
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class ItemFallShield extends Item implements IBauble {

    public static ItemFallShield INSTANCE;

    /** 掉落伤害倍率:0.1 = 减免 90% */
    public static final float FALL_DAMAGE_MULTIPLIER = 0.1f;

    public ItemFallShield(String name) {
        setRegistryName(name);
        setUnlocalizedName(TGCEAddon.MODID + "." + name);
        setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        setMaxStackSize(1);
        INSTANCE = this;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.CHARM;
    }

    /** 掉落伤害减免:佩戴本饰品时,掉落/鞘翅撞击落地伤害降为 10% */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (INSTANCE != null && BaublesApi.isBaubleEquipped(player, INSTANCE) != -1) {
                event.setDamageMultiplier(FALL_DAMAGE_MULTIPLIER);
            }
        }
    }
}
