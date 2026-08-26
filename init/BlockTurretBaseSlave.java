package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntSlave;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;

/**
 * 炮塔基座 - 从方块（slave）
 * 3x1x3 多方块结构中围绕 master 的防护板方块：
 * - 记录所属 master 位置，被破坏时通知 master 解散
 * - 右键任意从方块也可打开炮塔 GUI（转发到 master）
 * - 形成后（hasMaster=true）隐藏标准方块模型，由 master 的 TESR 渲染底座大模型
 */
public class BlockTurretBaseSlave extends Block {

    public static final PropertyBool FORMED = PropertyBool.create("formed");

    public BlockTurretBaseSlave(String name) {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(TGCEAddon.MODID, name));
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setSoundType(SoundType.METAL);
        this.setHardness(4.0f);
        this.setResistance(30.0f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FORMED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FORMED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FORMED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FORMED, meta == 1);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        // FORMED 状态已由 TE.form/unform 通过 setBlockState 写入实际 blockstate(meta)，
        // 渲染直接使用 meta 值，不依赖客户端 TE 数据时序
        return state;
    }

    // ===== TileEntity 绑定 =====
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TurretBaseTileEntSlave();
    }

    // ===== 右键交互：结构已形成时转发打开 master 的炮塔 GUI =====
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TurretBaseTileEntSlave)) {
            return true;
        }
        TurretBaseTileEntSlave slave = (TurretBaseTileEntSlave) tile;
        if (!slave.hasMaster()) {
            return true;
        }
        if (!world.isRemote) {
            TileEntity masterTile = world.getTileEntity(slave.getMasterPos());
            if (masterTile instanceof TurretBaseTileEntMaster) {
                TurretBaseTileEntMaster master = (TurretBaseTileEntMaster) masterTile;
                if (master.isUseableByPlayer(player)) {
                    BlockPos mp = slave.getMasterPos();
                    player.openGui(TGCEAddon.instance, ModGuiHandler.GUI_TURRET_BASE, world,
                            mp.getX(), mp.getY(), mp.getZ());
                } else {
                    player.sendStatusMessage(new TextComponentTranslation(
                            "techguns.container.security.denied"), true);
                }
            }
        }
        return true;
    }

    // ===== 破坏时通知 master 解散结构 =====
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TurretBaseTileEntSlave) {
            ((TurretBaseTileEntSlave) tile).onBlockBreak();
        }
        super.breakBlock(world, pos, state);
    }

    // ===== 邻居变化：从方块上方的红石信号变化也转发给 master 重新检测 =====
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TurretBaseTileEntSlave && !worldIn.isRemote) {
            TurretBaseTileEntSlave slave = (TurretBaseTileEntSlave) tile;
            if (slave.hasMaster()) {
                TileEntity masterTile = worldIn.getTileEntity(slave.getMasterPos());
                if (masterTile instanceof TurretBaseTileEntMaster) {
                    ((TurretBaseTileEntMaster) masterTile).onNeighborBlockChange();
                }
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    // ===== 渲染属性 =====
    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    // ===== 注册辅助 =====
    public ItemBlock createItemBlock() {
        return new ItemBlock(this);
    }

    public void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(this);
    }
}
