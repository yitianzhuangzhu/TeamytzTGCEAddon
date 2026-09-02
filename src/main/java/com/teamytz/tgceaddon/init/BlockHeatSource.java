package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.tileentities.TileEntityHeatSource;
import com.teamytz.tgceaddon.tracking.HeatSourceManager;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;

/**
 * 热源测试方块(红外测试用)
 *
 * 放置后持续产生红外热信号(由 TileEntityHeatSource 注册到 HeatSourceManager),
 * 红外热追踪导弹可锁定其坐标。用于验证 IR 系统(尤其是太阳锁定方向)的测试方块。
 */
public class BlockHeatSource extends Block {

    public BlockHeatSource(String name) {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(TGCEAddon.MODID, name));
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setSoundType(SoundType.METAL);
        this.setHardness(4.0f);
        this.setResistance(30.0f);
        this.setLightLevel(0.8f); // 微微发光,便于在黑暗中发现
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityHeatSource();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            HeatSourceManager.unregisterHeatBlock(world, pos);
        }
        super.breakBlock(world, pos, state);
    }

    public ItemBlock createItemBlock() {
        return new ItemBlock(this);
    }

    public void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(this);
    }
}
