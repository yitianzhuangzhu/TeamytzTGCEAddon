package com.teamytz.tgceaddon.item.weapon;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.EntityFlare;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.item.ItemFlare;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * 热诱弹发射器
 *
 * 右键:在玩家当前位置生成一个热诱弹实体(200 红外热值,诱骗红外热追踪导弹),
 * 消耗玩家背包中的一发热诱弹(ItemFlare)。10 tick 冷却。
 */
public class ItemFlareLauncher extends Item {

    /** 右键冷却(tick) */
    private static final int COOLDOWN = 10;

    public ItemFlareLauncher(String name) {
        this.setRegistryName(TGCEAddon.MODID, name);
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (world.isRemote) {
            // 客户端:仅按背包是否还有热诱弹决定是否播放使用动作(真正消耗在服务端)
            return new ActionResult<>(hasFlare(player) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, held);
        }
        // 服务端:消耗一发热诱弹并生成热诱弹实体
        if (!consumeFlare(player)) {
            return new ActionResult<>(EnumActionResult.FAIL, held);
        }
        EntityFlare flare = new EntityFlare(world);
        flare.setPosition(player.posX, player.posY + 1.0D, player.posZ);
        // 小幅上冲 + 继承玩家部分速度(空中移动时诱饵不会瞬间掉队)+ 轻微随机漂移
        flare.motionY = 0.3D;
        flare.motionX = player.motionX * 0.5D + (player.getRNG().nextDouble() - 0.5D) * 0.1D;
        flare.motionZ = player.motionZ * 0.5D + (player.getRNG().nextDouble() - 0.5D) * 0.1D;
        world.spawnEntity(flare);
        player.getCooldownTracker().setCooldown(this, COOLDOWN);
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    /** 玩家背包中是否有热诱弹 */
    private static boolean hasFlare(EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemFlare) {
                return true;
            }
        }
        return false;
    }

    /** 消耗一发热诱弹(从背包任意槽位,优先主手以外的) */
    private static boolean consumeFlare(EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemFlare) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
