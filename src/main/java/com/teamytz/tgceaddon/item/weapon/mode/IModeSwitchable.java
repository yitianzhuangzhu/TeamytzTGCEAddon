package com.teamytz.tgceaddon.item.weapon.mode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import techguns.TGPackets;
import techguns.TGSounds;
import techguns.client.audio.TGSoundCategory;
import techguns.items.guns.GenericGun;
import techguns.packets.PacketPlaySound;
import techguns.util.TextUtil;

/**
 * 「右键换挡」的通用实现（全部逻辑走 default 方法，任何 {@link GenericGun} 子类实现即用）。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>档位存在物品 NBT 的 {@link #MODE_NBT_KEY} 整数里，**不用玩家侧字段**——
 *       所以主副手各持一把同款枪时互不干扰，退出重进也保留。</li>
 *   <li>右键的处理点放在物品自身（{@code onItemRightClick} + {@code onItemUseFirst}），
 *       **两端各自执行**，因此不需要自定义网络包：两个 ItemStack 从同一份 NBT 算出同一个下一档。</li>
 *   <li>不走科技枪的 {@code hasSecondaryAction()/gunSecondaryAction()}：那条路是「开镜」专用、
 *       仅主手生效、双持时还会被副手分支劫持。</li>
 * </ul>
 */
public interface IModeSwitchable {

    /** 档位在物品 NBT 里的键名。避开科技枪的 ammo / ammovariant / miningHead / camo。 */
    String MODE_NBT_KEY = "gm";

    /** 换挡冷却（tick），防止连点刷屏。 */
    int SWITCH_COOLDOWN_TICKS = 6;

    /** 全部档位（顺序即右键循环顺序）。 */
    GunMode[] getModes();

    /** 老物品/新造物品的默认档位下标。 */
    int getDefaultMode();

    /** 空数组兜底，避免每个实现都写判空。 */
    default GunMode[] allModes() {
        GunMode[] modes = this.getModes();
        return modes == null ? new GunMode[0] : modes;
    }

    /** 当前档位；没有档位表时返回 null。 */
    default GunMode getMode(ItemStack stack) {
        GunMode[] modes = this.allModes();
        if (modes.length == 0) {
            return null;
        }
        int index = Math.floorMod(this.getModeIndex(stack), modes.length);
        return modes[index];
    }

    /** 读档位下标（越界钳制由调用方做 floorMod）。 */
    default int getModeIndex(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return this.getDefaultMode();
        }
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) {
            // 与科技枪 getMiningHeadLevel 同款兜底：没有 NBT 就先按初始标签建一份
            ((GenericGun) this).onCreated(stack, null, null);
            tags = stack.getTagCompound();
        }
        return tags == null ? this.getDefaultMode() : tags.getInteger(MODE_NBT_KEY);
    }

    /** 写档位下标。 */
    default void setModeIndex(ItemStack stack, int index) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null) {
            ((GenericGun) this).onCreated(stack, null, null);
            tags = stack.getTagCompound();
        }
        if (tags != null) {
            tags.setInteger(MODE_NBT_KEY, index);
        }
    }

    /**
     * 右键换挡（下一档，循环）。
     *
     * @return true 表示这次点击已被消费（调用方应吞掉本次右键，别再让方块交互跑）
     */
    default boolean cycleMode(World world, EntityPlayer player, EnumHand hand) {
        GunMode[] modes = this.allModes();
        if (modes.length < 2) {
            return false;   // 单档枪不该吞右键
        }
        Item item = (Item) this;
        if (player.getCooldownTracker().hasCooldown(item)) {
            return false;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || stack.getItem() != item) {
            return false;
        }

        int next = Math.floorMod(this.getModeIndex(stack) + 1, modes.length);
        this.setModeIndex(stack, next);
        player.getCooldownTracker().setCooldown(item, SWITCH_COOLDOWN_TICKS);

        this.playSwitchFeedback(world, player, modes[next]);
        return true;
    }

    /** 换挡音效：客户端本地立即响，服务端广播给周围（playOnOwnPlayer=false，免得自己听两遍）。 */
    default void playSwitchFeedback(World world, EntityPlayer player, GunMode mode) {
        if (world.isRemote) {
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    TGSounds.SWITCH_AMMO_TYPE, SoundCategory.PLAYERS, 0.8f, 1.25f);
        } else {
            TGPackets.wrapper.sendToAllAround(
                    new PacketPlaySound(TGSounds.SWITCH_AMMO_TYPE, player, 0.8f, 1.25f,
                            false, false, false, false, TGSoundCategory.GUN_FIRE),
                    TGPackets.targetPointAroundEnt(player, 24.0f));
        }
    }

    /** 档位显示名（语言键 {@code <物品 unlocalizedName>.mode.<档位 id>}）。 */
    default String getModeDisplayName(GunMode mode) {
        if (mode == null) {
            return "";
        }
        return TextUtil.trans(((GenericGun) this).getUnlocalizedName() + ".mode." + mode.id);
    }
}
