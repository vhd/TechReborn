package techreborn.tunnelbore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class BlockData implements INBTSerializable<NBTTagCompound> {

	IBlockState blockState;
	IBlockState renderState;
	BlockPos oldPos;
	TileEntity tileEntity;
	NBTTagCompound tileData;


	public BlockData(NBTTagCompound tileData, IBlockState blockState, BlockPos oldPos) {
		this.tileData = tileData;
		this.blockState = blockState;
		this.oldPos = oldPos;
	}

	public BlockData(IBlockState blockState, BlockPos oldPos) {
		this.blockState = blockState;
		this.oldPos = oldPos;
	}

	public BlockData(NBTTagCompound nbt){
		deserializeNBT(nbt);
	}

	public static BlockData buildBlockData(World world, BlockPos pos) {
		BlockData blockData = new BlockData(world.getBlockState(pos), pos);
		TileEntity tile = world.getTileEntity(pos);
		IBlockState state = world.getBlockState(pos);
		blockData.renderState = state.getBlock().getActualState(state, world, pos);
		if (tile != null) {
			blockData.tileData = tile.writeToNBT(new NBTTagCompound());
			blockData.tileEntity = tile;
		}
		return blockData;
	}

	public NBTTagCompound getTileData() {
		return tileData;
	}

	public void setTileData(NBTTagCompound tileData) {
		this.tileData = tileData;
	}

	public TileEntity getTileEntity(World world) {
		if(tileEntity == null && tileData != null){
			tileEntity = blockState.getBlock().createTileEntity(world, blockState);
			tileEntity.setWorld(world);
			tileEntity.readFromNBT(tileData);
		}
		return tileEntity;
	}

	public IBlockState getBlockState() {
		return blockState;
	}

	public IBlockState getRenderState() {
		return renderState;
	}

	public BlockPos getOldPos() {
		return oldPos;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		tagCompound.setTag("blockState", NBTUtil.writeBlockState(new NBTTagCompound(), blockState));
		tagCompound.setTag("renderState", NBTUtil.writeBlockState(new NBTTagCompound(), renderState));
		tagCompound.setLong("oldPos", oldPos.toLong());
		if(tileData != null){
			tagCompound.setTag("tileData", tileData);
		}
		return tagCompound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		blockState = NBTUtil.readBlockState(nbt.getCompoundTag("blockState"));
		renderState = NBTUtil.readBlockState(nbt.getCompoundTag("renderState"));
		oldPos = BlockPos.fromLong(nbt.getLong("oldPos"));
		if(nbt.hasKey("tileData")){
			tileData = (NBTTagCompound) nbt.getTag("tileData");
			//TODO create a new tile and load its data?
		}
	}

	@Override
	public String toString() {
		return "BlockData{" +
			"blockState=" + blockState +
			", tileData=" + tileData +
			'}';
	}
}
