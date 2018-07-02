package techreborn.tunnelbore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class BlockData implements INBTSerializable<NBTTagCompound> {

	final IBlockState blockState;
	IBlockState renderState;
	final BlockPos oldPos;
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

	public TileEntity getTileEntity() {
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

	//TODO
	@Override
	public NBTTagCompound serializeNBT() {
		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {

	}

	@Override
	public String toString() {
		return "BlockData{" +
			"blockState=" + blockState +
			", tileData=" + tileData +
			'}';
	}
}
