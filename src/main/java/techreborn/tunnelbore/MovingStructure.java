package techreborn.tunnelbore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import techreborn.init.ModBlocks;

import java.util.*;
import java.util.stream.Collectors;

public class MovingStructure {

	TileTunnelboreController controller;

	List<BlockData> movingBlocks;

	public Translation translation;

	public MovingStructure(TileTunnelboreController controller) {
		this.controller = controller;
	}

	public List<BlockData> getMovingBlocks(){
		return Collections.unmodifiableList(movingBlocks);
	}

	//Returns true if can move
	public boolean prepareToMove(){
		List<BlockPos> blocksToMove = findBlockToMove();
		if(blocksToMove.isEmpty()){
			return false;
		}
		movingBlocks = new ArrayList<>();
		movingBlocks.addAll(blocksToMove.stream()
			.map(pos -> BlockData.buildBlockData(controller.getWorld(), pos))
			.collect(Collectors.toList()));

		translation = new Translation(controller.boreDirection);

		removeBlocks();

		return true;
	}

	public void finishMoving(){
		replaceBlocks();
	}

	private void removeBlocks(){
		movingBlocks.forEach(blockData -> {
			controller.getWorld().removeTileEntity(blockData.oldPos); //Remove the old tile before breaking to prevent items dropping
			controller.getWorld().setBlockToAir(blockData.oldPos);
		});
	}

	private void replaceBlocks(){
		movingBlocks.forEach(blockData -> {
			BlockPos newPos = blockData.oldPos.offset(controller.boreDirection);
			controller.getWorld().setBlockState(newPos, blockData.blockState);
			if(blockData.tileData != null){
				NBTTagCompound tagCompound = updateTileData(blockData.tileData, newPos);
				TileEntity newTile = controller.getWorld().getTileEntity(newPos);
				newTile.readFromNBT(tagCompound);
			}
		});
	}

	private NBTTagCompound updateTileData(NBTTagCompound tagCompound, BlockPos newPos){
		tagCompound.setInteger("x", newPos.getX());
		tagCompound.setInteger("y", newPos.getY());
		tagCompound.setInteger("z", newPos.getZ());
		//TODO some modded blocks may need help here as well if we plan to move them
		return tagCompound;
	}

	public List<BlockPos> findBlockToMove(){
		BlockPos startPos = controller.getPos().down();
		List<BlockPos> blockToMove = new ArrayList<>();
		Queue<BlockPos> blocksToScan = new PriorityQueue<>();
		if(canMoveBlock(startPos)){
			blocksToScan.add(startPos);
			blockToMove.add(startPos);
		} else {
			return Collections.emptyList();
		}
		while(!blocksToScan.isEmpty()){
			BlockPos pos = blocksToScan.poll();
			for(EnumFacing facing : EnumFacing.VALUES){
				BlockPos checkPos = pos.offset(facing);
				if(canMoveBlock(checkPos) && !blockToMove.contains(checkPos)){
					blockToMove.add(checkPos);
					if(isSupportBlock(checkPos)){
						blocksToScan.add(checkPos);
					}
				}

			}
		}
		return blockToMove;
	}

	public boolean canMoveBlock(BlockPos pos){
		IBlockState state = controller.getWorld().getBlockState(pos);
		if(state.getBlock() == ModBlocks.TUNNEL_BORE_CONTROLLER){
			return false;
		}
		return true;
	}

	public boolean isSupportBlock(BlockPos pos){
		IBlockState state = controller.getWorld().getBlockState(pos);
		return state.getBlock() instanceof BlockTunnelboreSupport;
	}

}
