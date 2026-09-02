package com.teamytz.tgceaddon.item;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.init.ModCreativeTabs;
import com.teamytz.tgceaddon.network.BoostStateMessage;
import com.teamytz.tgceaddon.network.ElytraStartMessage;
import com.teamytz.tgceaddon.network.ElytraStopMessage;
import com.teamytz.tgceaddon.network.PacketHandler;
import com.teamytz.tgceaddon.tracking.HeatSourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.TGItems;
import techguns.TGPackets;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.player.TGPlayerInventory;
import techguns.keybind.TGKeybindsID;
import techguns.packets.PacketTGKeybindPress;
import techguns.util.InventoryUtil;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * 鞘翅喷气背包
 *
 * 放入科技枪背部槽(BACKSLOT),自带原版鞘翅飞行能力(无需穿戴原版鞘翅):
 * - 空中按空格:启动鞘翅飞行(物理由原版鞘翅处理)
 * - 飞行中按住空格:烟花火箭式加速(每 tick 消耗燃料),ModelJetPack 渲染自动产生火焰粒子
 * - 加速时产生红外热信号(HeatSourceManager,供未来的热追踪导弹锁定)
 * - 燃料:复用科技枪 FUEL_TANK 燃料罐,耗尽自动补充
 *
 * 关键实现:原版 updateElytra() 每 tick 检查胸甲槽是否有原版鞘翅,没有就取消
 * 鞘翅状态(Entity flag 7)。因此本背包维护激活状态,在 PlayerTickEvent.START
 * (早于 travel,即本 tick 鞘翅物理之前)每 tick 重新设置 flag 7,保证本 tick
 * 的 travel 必定读到 true,鞘翅飞行稳定生效;加速/粒子判定不依赖会被取消的
 * isElytraFlying()。
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class ItemElytraJetpack extends Item implements ITGSpecialSlot {

    public static ItemElytraJetpack INSTANCE;

    /** Debug 日志开关:测试期间开启,定位鞘翅飞行问题 */
    public static final boolean DEBUG = true;

    /** 燃料容量(耐久值即燃料,与科技枪喷气背包一致) */
    public static final int FUEL_CAPACITY = 5000;

    /** 激活状态:玩家正在使用本背包鞘翅飞行(两端各自维护) */
    private static final WeakHashMap<EntityPlayer, Boolean> ACTIVE = new WeakHashMap<>();

    /** 加速状态(仅服务端维护,由 BoostStateMessage 同步,不依赖科技枪按键同步) */
    private static final WeakHashMap<EntityPlayer, Boolean> BOOSTING = new WeakHashMap<>();

    /** 反射缓存的 Entity.setFlag 方法(运行时 SRG 名 func_70052_a,描述符 (IZ)V) */
    private static Method SET_FLAG_METHOD;

    public ItemElytraJetpack(String name) {
        setRegistryName(name);
        setUnlocalizedName(TGCEAddon.MODID + "." + name);
        setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        setMaxStackSize(1);
        setMaxDamage(FUEL_CAPACITY);
        setNoRepair();
        INSTANCE = this;
    }

    /** 设置/清除背包鞘翅激活状态。
     *  清除时同时立即清掉 flag 7(不再滑翔),避免"停飞后仍漂浮/残留滑翔"。 */
    public static void setActive(EntityPlayer player, boolean active) {
        if (active) {
            ACTIVE.put(player, true);
        } else {
            if (ACTIVE.remove(player) != null) {
                // 结束鞘翅:本端立即清除鞘翅标志,恢复自由落体(不留残余滑翔)
                setElytraFlag(player, false);
                if (DEBUG) {
                    TGCEAddon.getLogger().info("[Jetpack] {}:停飞,清除 flag7", player.world.isRemote ? "CLIENT" : "SERVER");
                }
            }
        }
    }

    /** 玩家是否正在使用背包鞘翅飞行 */
    public static boolean isActive(EntityPlayer player) {
        return ACTIVE.containsKey(player);
    }

    /** 设置/清除加速状态(由 BoostStateMessage 在服务端调用) */
    public static void setBoosting(EntityPlayer player, boolean boosting) {
        if (boosting) {
            BOOSTING.put(player, true);
        } else {
            BOOSTING.remove(player);
        }
    }

    /** 设置鞘翅飞行标志(Entity flag 7 = isElytraFlying)。
     *  用反射 + 运行时 SRG 名(func_70052_a)+ setAccessible,不依赖 AT 加载。 */
    private static void setElytraFlag(EntityPlayer player, boolean value) {
        try {
            if (SET_FLAG_METHOD == null) {
                SET_FLAG_METHOD = Entity.class.getDeclaredMethod("func_70052_a", int.class, boolean.class);
                SET_FLAG_METHOD.setAccessible(true);
            }
            SET_FLAG_METHOD.invoke(player, 7, value);
            if (DEBUG && player.ticksExisted % 20 == 0) {
                TGCEAddon.getLogger().info("[Jetpack] {}:setFlag(7,{}) 后 isElytraFlying={}",
                        player.world.isRemote ? "CLIENT" : "SERVER", value, player.isElytraFlying());
            }
        } catch (Exception e) {
            TGCEAddon.getLogger().error("鞘翅喷气背包:设置鞘翅状态失败", e);
        }
    }

    @Override
    public TGSlotType getSlot(ItemStack item) {
        return TGSlotType.BACKSLOT;
    }

    /** 右键直接放入科技枪背部槽 */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        TGExtendedPlayer props = TGExtendedPlayer.get(playerIn);
        if (props.tg_inventory.inventory.get(TGPlayerInventory.SLOT_BACK).isEmpty()) {
            props.tg_inventory.inventory.set(TGPlayerInventory.SLOT_BACK, stack.copy());
            stack.setCount(0);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void onPlayerTick(ItemStack item, PlayerTickEvent event) {
        EntityPlayer player = event.player;

        // 双保险:END 阶段也保持 flag 7(对抗 updateElytra 的胸甲检查取消)。
        // 与 START 阶段的保持配合,保证 dataManager 同步时 flag 7 稳定为 true,
        // 避免客户端第三人称"站立/飞行"姿态闪烁、鞘翅物理时灵时不灵
        keepElytraActive(player);

        // 燃料耗尽:尝试用燃料罐补充
        if (item.getItemDamage() >= item.getMaxDamage()) {
            tryReloadFuel(item, player);
            return;
        }

        // Debug:周期性输出飞行相关状态(每 20 tick,两端各自输出)
        if (DEBUG && player.ticksExisted % 20 == 0) {
            TGExtendedPlayer propsDbg = TGExtendedPlayer.get(player);
            TGCEAddon.getLogger().info("[Jetpack] 状态 side={} ground={} water={} riding={} flying={} jump={} active={} fuel={}",
                    player.world.isRemote ? "CLIENT" : "SERVER",
                    player.onGround, player.isInWater(), player.isRiding(),
                    player.isElytraFlying(), propsDbg.isJumpkeyPressed(), isActive(player),
                    item.getItemDamage() < item.getMaxDamage());
        }

        if (player.world.isRemote) {
            // ===== 客户端:输入检测 + 本地鞘翅保持 + 粒子 =====
            if (player == Minecraft.getMinecraft().player) {
                TGExtendedPlayer props = TGExtendedPlayer.get(player);
                boolean jumpPressed = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
                boolean lastState = props.isJumpkeyPressed();

                // ===== 手动结束鞘翅:空中潜行(shift)直接停飞下落 =====
                if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()
                        && isActive(player) && !player.onGround) {
                    setActive(player, false); // 内部立即清除 flag7 → 直接下落
                    PacketHandler.sendToServer(new ElytraStopMessage());
                    if (DEBUG) {
                        TGCEAddon.getLogger().info("[Jetpack] 客户端:潜行手动结束鞘翅飞行");
                    }
                }

                if (jumpPressed && !lastState) {
                    // 空格按下(边缘触发)
                    if (DEBUG) {
                        TGCEAddon.getLogger().info("[Jetpack] 客户端:空格按下 ground={} flying={} motionY={}", player.onGround, player.isElytraFlying(), String.format("%.2f", player.motionY));
                    }
                    // 启动鞘翅:仅"下落过程中按空格"进入滑行(与原版鞘翅一致)。
                    // - 地面按空格 = 正常跳跃(不起飞)
                    // - 跳起上升中(motionY > 0)不启动
                    // - 明显下落(motionY < -0.05)时按空格 = 展开鞘翅滑行
                    // - 已在鞘翅飞行中(!isActive 为假)不重复启动,也不干预空格行为
                    //   (否则会与"双击空格切换创造飞行"打架:飞行中双击空格 = 切创造飞行,
                    //   若这里又去解除创造飞行就会把切换顶回去)
                    // - 创造飞行中悬停/上升不启动;若正在下落(如按住 shift 下降)时按空格,
                    //   自动解除创造飞行并展开鞘翅,否则创造飞行会一直"飘"着无法起飞
                    if (!isActive(player) && !player.onGround && player.motionY < -0.05f) {
                        if (player.capabilities.isFlying) {
                            player.capabilities.isFlying = false;
                            player.sendPlayerAbilities(); // 同步服务端能力(解除创造飞行)
                            if (DEBUG) {
                                TGCEAddon.getLogger().info("[Jetpack] 客户端:解除创造飞行,展开鞘翅");
                            }
                        }
                        setActive(player, true);
                        PacketHandler.sendToServer(new ElytraStartMessage());
                    }
                    // 本地加速状态(供 ModelJetPack 火焰粒子)+ 服务端加速状态(燃料/热信号)
                    props.setJumpkeyPressed(true);
                    TGPackets.wrapper.sendToServer(new PacketTGKeybindPress(TGKeybindsID.JETPACK_BOOST_START));
                    PacketHandler.sendToServer(new BoostStateMessage(true));
                } else if (!jumpPressed && lastState) {
                    // 空格释放
                    if (DEBUG) {
                        TGCEAddon.getLogger().info("[Jetpack] 客户端:空格释放");
                    }
                    props.setJumpkeyPressed(false);
                    TGPackets.wrapper.sendToServer(new PacketTGKeybindPress(TGKeybindsID.JETPACK_BOOST_STOP));
                    PacketHandler.sendToServer(new BoostStateMessage(false));
                }

                // ===== 客户端:鞘翅滑翔 + 烟花式加速 =====
                // 1) flag 7 稳定时,原版 travel 的鞘翅物理自动生效;
                //    被 updateElytra 取消的 tick(flag 7 短暂 false)则用下方兜底滑翔物理,
                //    保证每一 tick 都有滑翔效果,消除"一顿一顿"的顿挫感
                if (isActive(player) && !player.onGround) {
                    // 飞行期间强制重置下落距离:原版鞘翅飞行中 fallDistance 被持续重置,
                    // 但 flag 7 偶尔被 updateElytra 取消会走自由落体累积 fallDistance,
                    // 导致落地伤害异常高。这里每 tick 清零,保证落地安全(同原版鞘翅)
                    player.fallDistance = 0;
                    if (!player.isElytraFlying()) {
                        applyElytraGlide(player);
                    }
                    // 2) 按住空格:烟花式加速(客户端本地 motion,客户端是位置权威)
                    if (props.isJumpkeyPressed()) {
                        applyBoostMotion(player);
                    }
                }
            }
        } else {
            // ===== 服务端:燃料消耗 + 热信号(运动由客户端驱动,此处不改 motion) =====
            // 服务端同样重置 fallDistance,确保服务端掉落伤害结算为 0(鞘翅落地安全)
            if (isActive(player) && !player.onGround) {
                player.fallDistance = 0;
            }
            if (BOOSTING.getOrDefault(player, false) && !player.onGround && isActive(player)) {
                // 燃料消耗(每加速 tick 1 点)
                item.damageItem(1, player);
                // 红外热信号(供热追踪导弹锁定)
                HeatSourceManager.markHeatSource(player);
            }
        }
    }

    /**
     * 客户端:应用 1.12.2 原版烟花火箭式加速(与 EntityFireworkRocket 的公式一致):
     *   motion += look*0.1 + (look*1.5 - motion)*0.5(每 tick,方向朝玩家视线)
     */
    protected void applyBoostMotion(EntityPlayer player) {
        Vec3d look = player.getLookVec();
        player.motionX += look.x * 0.1D + (look.x * 1.5D - player.motionX) * 0.5D;
        player.motionY += look.y * 0.1D + (look.y * 1.5D - player.motionY) * 0.5D;
        player.motionZ += look.z * 0.1D + (look.z * 1.5D - player.motionZ) * 0.5D;
        player.fallDistance = 0;
    }

    /**
     * 客户端兜底:原版鞘翅滑翔物理(复制 1.12.2 EntityLivingBase.travel 的鞘翅分支)。
     * 当 flag 7 被 updateElytra 取消的 tick,由这里施加同样的滑翔物理,
     * 保证飞行每 tick 平滑,不出现自由落体的顿挫。
     */
    protected void applyElytraGlide(EntityPlayer player) {
        if (player.motionY > -0.5D) {
            player.fallDistance = 1.0F;
        }
        player.motionX *= 0.99D;
        player.motionY *= 0.98D;
        player.motionZ *= 0.99D;
        if (!player.onGround) {
            Vec3d vec3d = player.getLookVec();
            float f = player.rotationPitch * 0.017453292F;
            double d1 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
            double d2 = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
            double d3 = vec3d.lengthVector();
            float f2 = (float) Math.cos(f);
            f2 = (float) ((double) f2 * (double) f2 * Math.min(1.0D, d3 / 0.4D));
            player.motionY -= 0.08D + (double) f2 * 0.06D;
            if (d2 > 0.0D) {
                double d4 = (vec3d.x / d1 * d2 - player.motionX) * 0.1D;
                player.motionX += d4;
                double d5 = (vec3d.z / d1 * d2 - player.motionZ) * 0.1D;
                player.motionZ += d5;
            }
            player.motionX *= 0.99D;
            player.motionY *= 0.98D;
            player.motionZ *= 0.99D;
        }
    }

    /**
     * 在 PlayerTickEvent.START(早于本 tick 的 travel 鞘翅物理)保持 flag 7:
     * 保证本 tick 的 travel 一定读到 true,鞘翅飞行稳定生效。
     * 原版 updateElytra 会在更晚的时刻取消 flag 7(胸甲检查),下一 tick START 再恢复。
     */
    @SubscribeEvent
    public static void onPlayerTickStart(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        EntityPlayer player = event.player;
        TGExtendedPlayer props = TGExtendedPlayer.get(player);
        ItemStack back = props.tg_inventory.inventory.get(TGPlayerInventory.SLOT_BACK);
        if (!back.isEmpty() && back.getItem() instanceof ItemElytraJetpack) {
            keepElytraActive(player);
        }
    }

    /**
     * 玩家渲染前强制鞘翅姿态:updateElytra 取消 flag 7 后,服务端同步偶尔
     * 抓到 false,导致客户端 isElytraFlying()=false、玩家渲染成"站立飞行"。
     * 在 RenderPlayerEvent.Pre(渲染前)强制设置 flag 7 = true,保证姿态稳定。
     */
    @SubscribeEvent
    public static void onRenderPlayerPre(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (isActive(player) && !player.onGround) {
            setElytraFlag(player, true);
        }
    }

    /**
     * 保持鞘翅激活:玩家在空中时每 tick 重新设置 flag 7,
     * 对抗原版 updateElytra 的胸甲检查(它要求胸甲槽穿原版鞘翅);
     * 落地/进水/骑乘时清除激活(客户端清除时通知服务端,避免两端状态残留)。
     *
     * 关键:飞行中开启创造飞行(双击空格)→ 立即干净移交:
     * 停止鞘翅、清除 flag7、清掉残余滑翔速度,避免"站姿 + 鞘翅漂浮"的混合飘。
     */
    public static void keepElytraActive(EntityPlayer player) {
        if (isActive(player)) {
            // 创造飞行开启(双击空格等):移交创造飞行,彻底退出鞘翅
            if (player.capabilities.isFlying) {
                setActive(player, false); // 内部清除激活 + flag7
                if (player.world.isRemote) {
                    // 清掉鞘翅滑翔残留的水平速度,避免"站着但还在滑/飘"
                    player.motionX = 0.0D;
                    player.motionZ = 0.0D;
                    // 通知服务端同步清除
                    PacketHandler.sendToServer(new ElytraStopMessage());
                }
                if (DEBUG) {
                    TGCEAddon.getLogger().info("[Jetpack] {}:检测到创造飞行,退出鞘翅(移交创造飞行)",
                            player.world.isRemote ? "CLIENT" : "SERVER");
                }
                return;
            }
            if (player.onGround || player.isInWater() || player.isRiding()) {
                setActive(player, false);
                if (player.world.isRemote) {
                    // 通知服务端同步清除,防止服务端继续保持 flag7 导致"停飞后仍滑翔"
                    PacketHandler.sendToServer(new ElytraStopMessage());
                }
                if (DEBUG) {
                    TGCEAddon.getLogger().info("[Jetpack] {}:清除激活(落地/进水/骑乘)", player.world.isRemote ? "CLIENT" : "SERVER");
                }
            } else {
                setElytraFlag(player, true);
                if (DEBUG && player.ticksExisted % 20 == 0) {
                    TGCEAddon.getLogger().info("[Jetpack] {}:保持鞘翅 flag7=true", player.world.isRemote ? "CLIENT" : "SERVER");
                }
            }
        }
    }

    /** 从玩家背包消耗 FUEL_TANK 补充燃料(与科技枪喷气背包一致) */
    protected void tryReloadFuel(ItemStack item, EntityPlayer player) {
        if (InventoryUtil.consumeAmmoPlayer(player, TGItems.FUEL_TANK)) {
            item.setItemDamage(0);
            int left = InventoryUtil.addAmmoToPlayerInventory(player, TGItems.FUEL_TANK_EMPTY);
            if (left > 0 && !player.world.isRemote) {
                player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ,
                        new ItemStack(TGItems.FUEL_TANK_EMPTY.getItem(), left, TGItems.FUEL_TANK_EMPTY.getItemDamage())));
            }
        }
    }
}
