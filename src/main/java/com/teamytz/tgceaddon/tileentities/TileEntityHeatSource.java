package com.teamytz.tgceaddon.tileentities;

import com.teamytz.tgceaddon.tracking.HeatSourceManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

/**
 * 热源测试方块 TileEntity
 *
 * 服务端每 5 tick 向 HeatSourceManager 刷新自身坐标,
 * 使红外导弹可以锁定该方块(测试/验证 IR 系统用)。
 * 方块破坏、区块卸载时主动注销,防止泄漏。
 */
public class TileEntityHeatSource extends TileEntity implements ITickable {

    /** 注册刷新间隔(tick) */
    private static final int REGISTER_INTERVAL = 5;

    @Override
    public void update() {
        if (!world.isRemote && world.getTotalWorldTime() % REGISTER_INTERVAL == 0) {
            HeatSourceManager.registerHeatBlock(world, pos);
        }
    }

    @Override
    public void invalidate() {
        if (world != null && !world.isRemote) {
            HeatSourceManager.unregisterHeatBlock(world, pos);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote) {
            HeatSourceManager.unregisterHeatBlock(world, pos);
        }
        super.onChunkUnload();
    }
}
