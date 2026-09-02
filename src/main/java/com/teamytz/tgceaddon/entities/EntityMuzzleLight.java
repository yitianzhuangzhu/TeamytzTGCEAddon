package com.teamytz.tgceaddon.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.world.World;

/**
 * 动态光源实体(仅客户端生成,服务端永远不生成实例)
 *
 * 原理:OptiFine 的动态光源每 50ms 遍历世界实体,按实体手持物品的光值
 * (ItemBlock 取方块光值)计算光强并重算周围方块亮度。本实体:
 * - 隐藏、无碰撞、无重力,只作为一个"移动光源载体"
 * - 通过切换主手手持物品来改变光强(15=萤石 / 14=火把 / 11=下界传送门 / 7=红石火把)
 * - 一次性闪光(lifetime>0):高亮数 tick 后快速衰减消失(枪口闪光)
 * - 持续光源(lifetime=-1):跟随导弹移动,由外部 kill() 结束(动力段尾焰)
 *
 * 没有 OptiFine 时这些实体不产生任何光效(降级为无光),不影响其他功能。
 */
public class EntityMuzzleLight extends EntityLivingBase {

    /** 光强档位 → 手持方块物品(OptiFine getLightLevel(ItemStack) 取方块光值) */
    private static final ItemStack[] LIGHT_ITEM = new ItemStack[16];

    static {
        LIGHT_ITEM[0] = ItemStack.EMPTY;
        LIGHT_ITEM[7] = new ItemStack(Blocks.REDSTONE_TORCH);
        LIGHT_ITEM[11] = new ItemStack(Blocks.PORTAL);
        LIGHT_ITEM[14] = new ItemStack(Blocks.TORCH);
        LIGHT_ITEM[15] = new ItemStack(Blocks.GLOWSTONE);
        // 中间档位就近取整到可用光值
        for (int i = 4; i < 7; i++) {
            LIGHT_ITEM[i] = LIGHT_ITEM[7];
        }
        for (int i = 8; i < 11; i++) {
            LIGHT_ITEM[i] = LIGHT_ITEM[11];
        }
        for (int i = 12; i < 14; i++) {
            LIGHT_ITEM[i] = LIGHT_ITEM[14];
        }
    }

    /** 持续光源的默认保活时长(tick):调用方每 tick renew() 续期,停止续期后自动消亡 */
    private static final int KEEP_ALIVE = 40;

    /** 剩余存活 tick;>0 倒计时(闪烁/续期),<=0 消亡 */
    private int lifetime = 0;
    /** 生成时的总时长(闪烁衰减仅对较长闪光生效;2 tick 短闪保持满光直至消失) */
    private int spawnLifetime = 0;
    /** 当前光强 0..15 */
    private int lightLevel = 0;
    /** 主手手持物品(自行存储;OptiFine 通过 getHeldItemMainhand() 读取) */
    private ItemStack heldItem = ItemStack.EMPTY;

    public EntityMuzzleLight(World worldIn) {
        super(worldIn);
        this.setSize(0.1F, 0.1F);
        this.noClip = true;
        this.setNoGravity(true);
        this.setInvisible(true);
        this.setSilent(true);
    }

    /**
     * 一次性闪烁光源(枪口闪光):光强 light 持续 ticks 后快速衰减消失。
     * 受配置文件"光效.muzzleFlash"开关控制。仅客户端生成;服务端调用返回 null。
     */
    public static EntityMuzzleLight spawnFlash(World world, double x, double y, double z, int light, int ticks) {
        if (world == null || world.isRemote == false || !com.teamytz.tgceaddon.config.ConfigHandler.muzzleFlashEnabled) {
            return null;
        }
        EntityMuzzleLight e = new EntityMuzzleLight(world);
        e.setPosition(x, y, z);
        e.lifetime = Math.max(1, ticks);
        e.spawnLifetime = e.lifetime;
        e.setLight(light);
        world.spawnEntity(e);
        return e;
    }

    /**
     * 持续光源(导弹动力段尾焰):初始保活 KEEP_ALIVE tick,由调用方每 tick 移动位置并 renew() 续期;
     * 停止续期(导弹死亡/被移除)后自动消亡。
     * 受配置文件"光效.missileLight"开关控制。仅客户端生成;服务端调用返回 null。
     */
    public static EntityMuzzleLight spawnContinuous(World world, double x, double y, double z, int light) {
        if (world == null || world.isRemote == false || !com.teamytz.tgceaddon.config.ConfigHandler.missileLightEnabled) {
            return null;
        }
        EntityMuzzleLight e = new EntityMuzzleLight(world);
        e.setPosition(x, y, z);
        e.lifetime = KEEP_ALIVE;
        e.setLight(light);
        world.spawnEntity(e);
        return e;
    }

    /** 持续光源续期:调用方(导弹)每 tick 调用,防止光源提前消亡 */
    public void renew(int ticks) {
        this.lifetime = Math.max(this.lifetime, ticks);
    }

    /** 设置当前光强(换主手手持物品;OptiFine 每 50ms 重算光强,换物品即更新光) */
    public void setLight(int level) {
        this.lightLevel = Math.max(0, Math.min(15, level));
        ItemStack item = LIGHT_ITEM[this.lightLevel];
        this.heldItem = item != null ? item : ItemStack.EMPTY;
    }

    /** 结束持续光源 */
    public void kill() {
        this.setDead();
    }

    @Override
    public void onUpdate() {
        if (this.world.isRemote && this.lifetime > 0) {
            this.lifetime--;
            // 闪烁衰减:仅较长闪光(>2 tick)做最后 2 tick 降光;2 tick 短闪全程满光直接消失
            if (this.spawnLifetime > 2) {
                if (this.lifetime == 2 && this.lightLevel > 7) {
                    this.setLight(11);
                } else if (this.lifetime == 1) {
                    this.setLight(7);
                }
            }
            if (this.lifetime <= 0) {
                this.setDead();
                return;
            }
        }
        super.onUpdate();
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
    }

    /** EntityLivingBase 抽象方法实现:主手手持物品(OptiFine 动态光源读取) */
    @Override
    public ItemStack getHeldItemMainhand() {
        return this.heldItem;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            this.heldItem = stack;
        }
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            return this.heldItem;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return java.util.Collections.emptyList();
    }
}
