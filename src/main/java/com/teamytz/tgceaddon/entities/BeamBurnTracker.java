package com.teamytz.tgceaddon.entities;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * 光束「烧穿方块」的进度账本（服务端）。
 *
 * <p>为什么不用原版工具那套：{@code getDestroySpeed / canHarvestBlock} 只决定「按住左键时原版裂纹
 * 涨多快」，观感和节奏都被原版公式锁死。这里走的是另一条路——光束每次命中给目标方块累积一个点数，
 * 攒够 {@code 硬度 × 30} 就把方块打碎，速度由档位的 {@code burnPower} 控制。</p>
 *
 * <p>数值模型：</p>
 * <pre>
 *   needed = max(1, 硬度 × 30)          // ≈ 原版手挖所需 tick 数
 *   每次命中 += burnPower
 *   打碎条件 = 点数攒够 且 已照射 ≥ MIN_BURN_TICKS
 * </pre>
 * <p>第二条是必需的：光束每 tick 有 2 条并发命中（射速 5 tick + 光束寿命 10 tick），
 * 泥土这类 0.5 硬度的方块只要 5 tick 就攒够点数，玩家看到的是「一照就没」。
 * 加上 10 tick 下限后，任何方块都有至少半秒的可感知时长，同时石头/黑曜石这类
 * 高硬度方块仍然由点数主导（不受下限影响）。</p>
 *
 * <p>裂纹显示走自己发的 {@link SPacketBlockBreakAnim}，**不能用
 * {@code World.sendBlockBreakProgress}**——1.12.2 里那个方法只通知 {@code IWorldEventListener}，
 * 而服务端世界没有这类监听器（{@code WorldServer} 也没覆写），在服务端调用是空转。</p>
 *
 * <p>进度按「射手」记账（{@link WeakHashMap}，玩家退出即自动回收），
 * 换目标或 8 tick 没再照到就清空并撤掉裂纹显示。</p>
 */
public final class BeamBurnTracker {

    /** 多久没再照到同一方块就判定为「松开/移开准星」（tick）。 */
    private static final int TARGET_TIMEOUT = 8;

    /** 每 tick 有多少次「原版手挖 tick」被折算。硬度 × 30 = 手挖所需 tick 数。 */
    private static final float HARDNESS_TICKS = 30.0f;

    /** 单个方块至少要照这么多 tick 才碎（约 0.6 秒），防止低硬度方块一照就没。 */
    private static final int MIN_BURN_TICKS = 12;

    /** 裂纹同步半径（格）。 */
    private static final double SYNC_RADIUS_SQ = 128.0 * 128.0;

    private static final Map<EntityPlayer, Burn> BURNS = new WeakHashMap<>();

    private BeamBurnTracker() {
    }

    private static final class Burn {
        BlockPos pos;
        int dim;
        int lastTick;
        /** 开始照当前方块的 tick（用于 MIN_BURN_TICKS 下限）。 */
        int startTick;
        float progress;
        float needed;
        Block block;
    }

    /**
     * 光束命中一个方块（每 tick 调用一次，服务端）。
     *
     * @param hardness   {@code state.getBlockHardness(world, pos)}，调用方已保证 >= 0
     * @param burnPower  本次命中贡献的点数
     * @param dropBlocks 打碎后是否掉落
     */
    public static void hit(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                           float hardness, float burnPower, boolean dropBlocks) {
        if (world.isRemote || !(player instanceof EntityPlayerMP) || pos == null) {
            return;
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;
        int now = mp.ticksExisted;

        Burn burn = BURNS.get(mp);
        if (burn == null) {
            burn = new Burn();
            BURNS.put(mp, burn);
        }

        boolean retarget = burn.pos == null
                || !burn.pos.equals(pos)
                || burn.dim != world.provider.getDimension()
                || burn.block != state.getBlock()
                || now - burn.lastTick > TARGET_TIMEOUT;
        if (retarget) {
            sendOverlay(world, mp, burn.pos, -1);   // 撤掉上一个目标的裂纹
            burn.pos = pos.toImmutable();
            burn.dim = world.provider.getDimension();
            burn.block = state.getBlock();
            burn.progress = 0.0f;
            burn.startTick = now;
        }

        // 每次命中都按当前方块重算，杜绝「沿用上一个方块 needed」的状态残留
        burn.needed = Math.max(1.0f, hardness * HARDNESS_TICKS);
        burn.lastTick = now;
        burn.progress += burnPower;

        int elapsed = now - burn.startTick;
        boolean pointsDone = burn.progress >= burn.needed;
        boolean timeDone = elapsed >= MIN_BURN_TICKS;

        if (pointsDone && timeDone) {
            breakBlock(world, mp, pos, state, dropBlocks);
            sendOverlay(world, mp, pos, -1);
            burn.pos = null;
            BURNS.remove(mp);
            return;
        }

        // 裂纹进度：取「点数进度」和「时间进度」中更慢的那个，保证石块与时间下限都反映在视觉上
        float ratioByPoints = burn.progress / burn.needed;
        float ratioByTime = (float) elapsed / (float) MIN_BURN_TICKS;
        int stage = (int) Math.min(9.0f, Math.min(ratioByPoints, ratioByTime) * 10.0f);
        sendOverlay(world, mp, pos, stage);
    }

    /** 主动清空某个射手的进度（换枪/放下枪时可选调用）。 */
    public static void clear(EntityPlayer player, World world) {
        Burn burn = BURNS.remove(player);
        if (burn != null && player instanceof EntityPlayerMP) {
            sendOverlay(world, (EntityPlayerMP) player, burn.pos, -1);
        }
    }

    /**
     * 把裂纹进度发给附近玩家（含挖的人自己）。
     *
     * <p>{@code stage} 取 0..9，-1 表示清除。</p>
     */
    private static void sendOverlay(World world, EntityPlayerMP breaker, BlockPos pos, int stage) {
        if (pos == null || breaker.connection == null) {
            return;
        }
        int breakerId = breaker.getEntityId();
        for (EntityPlayer entity : world.playerEntities) {
            if (!(entity instanceof EntityPlayerMP)) {
                continue;
            }
            EntityPlayerMP viewer = (EntityPlayerMP) entity;
            if (viewer.connection == null) {
                continue;
            }
            if (viewer.getDistanceSq(pos) > SYNC_RADIUS_SQ) {
                continue;
            }
            viewer.connection.sendPacket(new SPacketBlockBreakAnim(breakerId, pos, stage));
        }
    }

    /**
     * 打碎方块：整套走「像玩家挖掉的一样」的流程。
     *
     * <p>顺序照抄科技枪已经跑通的 {@code GenericGunMeleeCharge.onBlockStartBreak}，
     * 这样领地插件（{@code ForgeHooks.onBlockBreakEvent} 返回 -1）、掉落、经验、
     * 方块碎裂粒子与客户端同步全部正确。</p>
     */
    private static void breakBlock(World world, EntityPlayerMP player, BlockPos pos, IBlockState state,
                                   boolean dropBlocks) {
        int xp = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
        if (xp < 0) {
            return;   // 被保护插件拒绝
        }
        Block block = state.getBlock();
        block.onBlockHarvested(world, pos, state, player);
        TileEntity tile = world.getTileEntity(pos);
        if (block.removedByPlayer(state, world, pos, player, true)) {
            block.onBlockDestroyedByPlayer(world, pos, state);
            if (dropBlocks && !player.capabilities.isCreativeMode) {
                block.harvestBlock(world, player, pos, state, tile, player.getHeldItemMainhand());
                block.dropXpOnBlockBreak(world, pos, xp);
            }
            world.playEvent(2001, pos, Block.getStateId(state));   // 碎裂粒子
            player.connection.sendPacket(new SPacketBlockChange(world, pos));
        }
    }
}
