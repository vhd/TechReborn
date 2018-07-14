package techreborn.tunnelbore;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import reborncore.common.BaseTileBlock;

import javax.annotation.Nullable;
import java.util.List;

public class BlockTunnelboreController extends BaseTileBlock {

	public BlockTunnelboreController() {
		super(Material.IRON);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTunnelboreController();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileTunnelboreController tunnelbore = (TileTunnelboreController) worldIn.getTileEntity(pos);
		if(!worldIn.isRemote){
			tunnelbore.boreDirection = facing.getOpposite();
			if (playerIn.isSneaking()) {
				//tunnelbore.currentState = TileTunnelboreController.BoreState.MINING;
			} else {
				tunnelbore.startMoving();
			}
		}
		return true;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state,
	                                  World worldIn,
	                                  BlockPos pos,
	                                  AxisAlignedBB entityBox,
	                                  List<AxisAlignedBB> collidingBoxes,
	                                  @Nullable
		                                  Entity entityIn,
	                                  boolean isActualState) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		TileTunnelboreController controller = null;
		if(tileEntity instanceof TileTunnelboreController){
			controller = (TileTunnelboreController) tileEntity;
		}

		if(controller != null && controller.isMoving()){
			AxisAlignedBB controllerBox = state.getCollisionBoundingBox(worldIn, pos);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, offsetBox(controller.structure.translation, controllerBox));
			for(BlockData blockData : controller.structure.getMovingBlocks()){
				AxisAlignedBB axisAlignedBB = blockData.blockState.getCollisionBoundingBox(worldIn, blockData.oldPos); //TODO save this in the block data?
				addCollisionBoxToList(pos, entityBox, collidingBoxes, offsetBox(controller.structure.translation, axisAlignedBB));
			}
		} else {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
		}
	}



	private AxisAlignedBB offsetBox(Translation translation, AxisAlignedBB axisAlignedBB){
		if(axisAlignedBB == null){
			return null;
		}
		Vec3d offset = translation.getOffset();
		Vec3d min = new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		min.add(offset);
		Vec3d max = new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		max.add(offset);
		return new AxisAlignedBB(min, max);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public boolean isFullCube() {
		return false;
	}
}
