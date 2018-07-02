package techreborn.tunnelbore;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.Validate;
import techreborn.init.ModBlocks;
import techreborn.tiles.TileGenericMachine;

import java.util.HashSet;
import java.util.Set;

public class TileTunnelboreDrill extends TileGenericMachine implements ITunnelBoreWorker {

	public int cooldown = 0;

	public TileTunnelboreDrill() {
		super("Drill", 32, 1000, ModBlocks.TUNNEL_BORE_DRILL, -1);
	}

	@Override
	public void update() {
		cooldown --;
		if (!world.isRemote && cooldown <= 0) {
			Set<BlockPos> targetBlocks = getTargetBlocks(world, getPos().offset(getFacing()));
			for (BlockPos pos : targetBlocks) {
				breakBlock(pos, getWorld());
				cooldown = 10;
				break;
			}
		}
	}

	public Set<BlockPos> getTargetBlocks(World worldIn, BlockPos pos) {
		Set<BlockPos> targetBlocks = new HashSet<BlockPos>();
		EnumFacing enumfacing = getFacing();
		if (enumfacing == EnumFacing.SOUTH || enumfacing == EnumFacing.NORTH) {
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					BlockPos newPos = pos.add(i, j, 0);
					if (shouldBreakBlock(worldIn, pos, newPos)) {
						targetBlocks.add(newPos);
					}
				}
			}
		} else if (enumfacing == EnumFacing.EAST || enumfacing == EnumFacing.WEST) {
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					BlockPos newPos = pos.add(0, j, i);
					if (shouldBreakBlock(worldIn, pos, newPos)) {
						targetBlocks.add(newPos);
					}
				}
			}
		} else if (enumfacing == EnumFacing.DOWN || enumfacing == EnumFacing.UP) {
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					BlockPos newPos = pos.add(j, 0, i);
					if (shouldBreakBlock(worldIn, pos, newPos)) {
						targetBlocks.add(newPos);
					}
				}
			}
		}
		return targetBlocks;
	}

	private boolean shouldBreakBlock(World worldIn, BlockPos originalPos, BlockPos pos) {
		FakePlayer fakePlayer = getFakePlayer();
		IBlockState blockState = worldIn.getBlockState(pos);
		if (blockState.getMaterial() == Material.AIR) {
			return false;
		}
		if (blockState.getMaterial().isLiquid()) {
			return false; //TODO find a better way to handle liquids
		}
//		float blockHardness = blockState.getPlayerRelativeBlockHardness(fakePlayer, worldIn, pos);
//		if (blockHardness == -1.0F) {
//			return false;
//		}
//		float originalHardness = worldIn.getBlockState(originalPos).getPlayerRelativeBlockHardness(fakePlayer, worldIn, originalPos);
//		if ((originalHardness / blockHardness) > 10.0F) {
//			return false;
//		}

		return true;
	}

	public void breakBlock(BlockPos pos, World world) {
		FakePlayer fakePlayer = getFakePlayer();
		IBlockState blockState = world.getBlockState(pos);
		blockState.getBlock().harvestBlock(world, fakePlayer, pos, blockState, world.getTileEntity(pos), new ItemStack(Items.DIAMOND_PICKAXE)); //TODO have a drill in the inv of the machine?
		world.setBlockToAir(pos);
		world.removeTileEntity(pos);
	}

	private FakePlayer getFakePlayer() {
		Validate.isTrue(world instanceof WorldServer); //Fake players do not work on clients
		return FakePlayerFactory.getMinecraft((WorldServer) world);
	}

	@Override
	public boolean isWorking() {
		return !getTargetBlocks(world, getPos().offset(getFacing())).isEmpty();
	}
}
