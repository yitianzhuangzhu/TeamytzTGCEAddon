package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;

/**
 * 炮塔基座（多方块结构） - 炮塔控制器（master，核心方块）
 * 3x1x3 多方块结构的中心控制方块：
 * - 直接右键形成多方块结构（无需扳手，与科技枪一致）
 * - 已形成后右键打开炮塔 GUI（弹药存储 + 电力 + 红石控制 + 卡片槽）
 * - 扳手(wrench/撬棍)右键已形成结构：旋转朝向；潜行+扳手：解散结构
 * - 破坏时自动解散并掉落物品栏
 * - 形成后标准方块模型隐藏，由 TESR 渲染炮塔底座大模型
 */
public class BlockTurretBase extends Block {

    public static final PropertyBool FORMED = PropertyBool.create("formed");
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockTurretBase(String name) {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(TGCEAddon.MODID, name));
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setSoundType(SoundType.METAL);
        this.setHardness(4.0f);
        this.setResistance(30.0f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FORMED, false).withProperty(FACING, EnumFacing.SOUTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FORMED, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(FORMED) ? 8 : 0) + state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FORMED, (meta & 8) != 0)
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 7));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TurretBaseTileEntMaster) {
            TurretBaseTileEntMaster master = (TurretBaseTileEntMaster) tile;
            if (master.isFormed()) {
                return state.withProperty(FORMED, true).withProperty(FACING, master.getMultiblockDirection());
            }
        }
        return state.withProperty(FORMED, false).withProperty(FACING, EnumFacing.SOUTH);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    // ===== TileEntity 绑定 =====
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TurretBaseTileEntMaster();
    }

    // ===== 右键交互：未形成-形成；已形成-打开GUI/扳手旋转拆卸 =====
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TurretBaseTileEntMaster)) {
            return true;
        }
        TurretBaseTileEntMaster master = (TurretBaseTileEntMaster) tile;

        ItemStack held = player.getHeldItem(hand);
        // 科技枪的撬棍(crowbar)工具类包含 "wrench"，即扳手（仅用于已形成结构的旋转/拆卸）
        boolean isWrench = !held.isEmpty() && held.getItem().getToolClasses(held).contains("wrench");

        if (!world.isRemote) {
            if (player.isSneaking() && isWrench) {
                // 潜行+扳手：解散结构
                if (master.isFormed()) {
                    master.onMultiBlockBreak();
                    player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                            "message.tgceaddon.turret_unformed"), true);
                }
            } else if (isWrench && master.isFormed()) {
                // 扳手右键已形成结构：旋转朝向
                master.rotateStructure(side);
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                        "message.tgceaddon.turret_rotated"), true);
            } else if (!master.isFormed()) {
                // 未形成：直接右键尝试形成（不需要扳手，与科技枪一致）
                if (master.checkAndForm(player, side)) {
                    player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                            "message.tgceaddon.turret_formed"), true);
                } else {
                    player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                            "message.tgceaddon.turret_form_invalid"), true);
                }
            } else if (master.isFormed()) {
                // 已形成：右键打开炮塔 GUI
                if (master.isUseableByPlayer(player)) {
                    player.openGui(TGCEAddon.instance, ModGuiHandler.GUI_TURRET_BASE, world,
                            pos.getX(), pos.getY(), pos.getZ());
                } else {
                    player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                            "techguns.container.security.denied"), true);
                }
            }
        }
        return true;
    }

    // ===== 放置时设置所有者（安全/所有者系统）+ 玩家放置标记（区分结构生成）=====
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TurretBaseTileEntMaster && placer instanceof EntityPlayer) {
            ((TurretBaseTileEntMaster) tile).setOwner((EntityPlayer) placer);
            ((TurretBaseTileEntMaster) tile).setPlayerPlaced(true);
        }
    }

    // ===== 邻居方块变化：通知 TE 刷新红石信号 =====
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TurretBaseTileEntMaster) {
            ((TurretBaseTileEntMaster) tile).onNeighborBlockChange();
        }
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    // ===== 破坏时解散结构并掉落物品栏 =====
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TurretBaseTileEntMaster) {
            TurretBaseTileEntMaster master = (TurretBaseTileEntMaster) tile;
            master.onMultiBlockBreak();
            master.onBlockBreak(); // 掉落物品栏物品（弹药/卡片）
        }
        super.breakBlock(world, pos, state);
    }

    // ===== 渲染属性（形成后由 TESR 渲染大模型，不参与方块渲染） =====
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
