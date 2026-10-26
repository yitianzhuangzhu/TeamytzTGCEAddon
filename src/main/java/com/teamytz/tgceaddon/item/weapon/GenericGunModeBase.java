package com.teamytz.tgceaddon.item.weapon;

import java.util.List;

import com.teamytz.tgceaddon.item.weapon.mode.GunMode;
import com.teamytz.tgceaddon.item.weapon.mode.IModeSwitchable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import techguns.items.guns.GenericGun;
import techguns.items.guns.ProjectileSelector;
import techguns.util.TextUtil;

/**
 * 「右键换挡」枪械的基类（科技枪 {@link GenericGun} 之上只加切档能力）。
 *
 * <p>为什么继承 {@link GenericGun} 而不是本仓库的 {@link GenericGunMeleeBase}：
 * 这类枪的左键是「开火」（光束），不能实现 {@code IGenericGunMelee}——那会把物品变成原版工具，
 * 挖掘速度/收获等级全走原版公式，与「光束烧穿方块」是两条路。</p>
 *
 * <p>左键链路是白拿的：构造里 {@code setShootWithLeftClick(true)} 之后，
 * 科技枪的 {@code TGTickHandler} 会按住左键每 {@code minFiretime} tick 双端各调一次
 * {@code shootGunPrimary}，并顺带把原版挖掘/挥手全部屏蔽掉。</p>
 */
public abstract class GenericGunModeBase extends GenericGun implements IModeSwitchable {

    /** 档位表，顺序即右键循环顺序。 */
    protected GunMode[] modes = new GunMode[0];

    public GenericGunModeBase(String name, ProjectileSelector projectileSelector, boolean semiAuto, int minFiretime,
                              int clipsize, int reloadtime, float damage, SoundEvent firesound, SoundEvent reloadsound,
                              int TTL, float accuracy) {
        super(name, projectileSelector, semiAuto, minFiretime, clipsize, reloadtime, damage, firesound, reloadsound,
                TTL, accuracy);
        // 左键 = 开火：科技枪会自动屏蔽原版挖掘与挥手（TGTickHandler / TGEventHandler）
        this.setShootWithLeftClick(true);
    }

    /** 配置档位表（可变参数，顺序即切换顺序）。 */
    public GenericGunModeBase setModes(GunMode... modes) {
        this.modes = modes == null ? new GunMode[0] : modes;
        return this;
    }

    @Override
    public GunMode[] getModes() {
        return this.modes;
    }

    @Override
    public int getDefaultMode() {
        return 0;
    }

    @Override
    protected void addInitialTags(NBTTagCompound tags) {
        super.addInitialTags(tags);
        tags.setInteger(IModeSwitchable.MODE_NBT_KEY, this.getDefaultMode());
    }

    // ===== 右键：空指/空气 =====

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (this.cycleMode(worldIn, playerIn, handIn)) {
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    // ===== 右键：指着方块 =====
    // 1.12.2 里对着方块右键走的是另一条路径（ForgeHooks.onRightClickBlock -> onItemUseFirst），
    // 不覆写这里的话，「准星在方块上右键」不会换挡——而玩家激光指着的通常正是方块。

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
                                           float hitX, float hitY, float hitZ, EnumHand hand) {
        if (this.cycleMode(world, player, hand)) {
            return EnumActionResult.SUCCESS;   // 吞掉，别顺手把箱子/熔炉打开
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    // ===== tooltip =====

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        GunMode mode = this.getMode(stack);
        if (mode != null) {
            list.add(TextUtil.trans("item.tgceaddon.mode.current") + ": §b" + this.getModeDisplayName(mode));
            list.add("§8" + TextUtil.trans("item.tgceaddon.mode.hint"));
        }
    }
}
