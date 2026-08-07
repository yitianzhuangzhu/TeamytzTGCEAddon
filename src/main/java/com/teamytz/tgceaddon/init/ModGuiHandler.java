package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.gui.UniversalCopierContainer;
import com.teamytz.tgceaddon.gui.UniversalCopierGui;
import com.teamytz.tgceaddon.tileentities.UniversalCopierTileEnt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * 通用复制机 GUI 处理器
 */
public class ModGuiHandler implements IGuiHandler {

    // ===== GUI ID 常量 =====
    public static final int GUI_UNIVERSAL_COPIER = 1;

    // ===== 注册：在 FML 初始化阶段挂接到 Forge 网络注册表 =====
    public static void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(TGCEAddon.MODID, new ModGuiHandler());
    }

    // ===== 服务端：返回 Container（物品槽同步逻辑）=====
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile instanceof UniversalCopierTileEnt) {
            return new UniversalCopierContainer(player.inventory, (UniversalCopierTileEnt) tile);
        }
        return null;
    }

    // ===== 客户端：返回 GuiScreen（界面绘制）=====
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile instanceof UniversalCopierTileEnt) {
            return new UniversalCopierGui(player.inventory, (UniversalCopierTileEnt) tile);
        }
        return null;
    }
}
