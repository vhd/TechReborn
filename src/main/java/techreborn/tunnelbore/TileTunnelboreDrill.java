package techreborn.tunnelbore;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.Validate;
import reborncore.common.RebornCoreConfig;
import reborncore.common.util.Inventory;
import reborncore.common.util.InventoryHelper;
import techreborn.client.container.IContainerProvider;
import techreborn.client.container.builder.BuiltContainer;
import techreborn.client.container.builder.ContainerBuilder;
import techreborn.compat.CompatManager;
import techreborn.init.ModBlocks;
import techreborn.items.tools.ItemDrill;
import techreborn.tiles.TileGenericMachine;
import techreborn.utils.IC2ItemCharger;

import javax.annotation.Nullable;
import java.util.*;

public class TileTunnelboreDrill extends TileGenericMachine implements ITunnelBoreWorker, IContainerProvider {

	public int cooldown = 0;

	public TileTunnelboreDrill() {
		super("Drill", 32, 1000, ModBlocks.TUNNEL_BORE_DRILL, -1);
		this.inventory = new Inventory(10, "TileTunnelboreDrill", 64, this);
	}

	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("tunnelboredrill").player(player.inventory).inventory().hotbar().addInventory().tile(this)
			.slot(0, 80, 20)
			.outputSlot(1, 8, 50)
			.outputSlot(2, 26, 50)
			.outputSlot(3, 44, 50)
			.outputSlot(4, 62, 50)
			.outputSlot(5, 80, 50)
			.outputSlot(6, 98, 50)
			.outputSlot(7, 116, 50)
			.outputSlot(8, 134, 50)
			.outputSlot(9, 152, 50)
			.syncEnergyValue().addInventory().create(this);
	}

	@Override
	public void update() {
		cooldown--;
		if (!world.isRemote && cooldown <= 0 && !getStackInSlot(0).isEmpty()) {
			cooldown = 0;
			Set<BlockPos> targetBlocks = getTargetBlocks(world, getPos().offset(getFacing()));
			for (BlockPos pos : targetBlocks) {
				cooldown = breakBlock(pos, getWorld());
				break;
			}
		}

		if (!world.isRemote) {
			if (!inventory.getStackInSlot(0).isEmpty()) {
				final ItemStack stack = inventory.getStackInSlot(0);
				if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
					IEnergyStorage powerItem = stack.getCapability(CapabilityEnergy.ENERGY, null);
					int maxReceive = powerItem.receiveEnergy((int) getMaxInput() * RebornCoreConfig.euPerFU, true);
					double maxUse = Math.min((double) (maxReceive / RebornCoreConfig.euPerFU), getMaxInput());
					if (getEnergy() >= 0.0 && maxReceive > 0) {
						powerItem.receiveEnergy((int) useEnergy(maxUse) * RebornCoreConfig.euPerFU, false);
					}
				} else if (CompatManager.isIC2Loaded) {
					IC2ItemCharger.chargeIc2Item(this, stack);
				}
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
		float blockHardness = blockState.getPlayerRelativeBlockHardness(fakePlayer, worldIn, pos);
		if (blockHardness == -1.0F) {
			return false;
		}
		ItemStack toolStack = getStackInSlot(0);
//		if(toolStack.getItem() instanceof ItemPickaxe){
//			if(!toolStack.canHarvestBlock(blockState)){
//				return false;
//			}
//		}
		if(toolStack.hasCapability(CapabilityEnergy.ENERGY, null)){
			if (toolStack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(1000, true) != 1000 ){
				return false;
			}
		}
		return true;
	}

	public int breakBlock(BlockPos pos, World world) {
		FakePlayer fakePlayer = getFakePlayer();
		IBlockState blockState = world.getBlockState(pos);
		ItemStack toolStack = getStackInSlot(0);
		if(toolStack.getItem() instanceof ItemPickaxe){
			if(toolStack.hasCapability(CapabilityEnergy.ENERGY, null)){
				Random rand = new Random();
				if (rand.nextInt(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, toolStack) + 1) == 0) {
					toolStack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(1000, false);
				}
			} else {
				toolStack.damageItem(2, fakePlayer);
			}
		}
		float hardness = world.getBlockState(pos).getBlockHardness(world, pos);
		harvestBlock(world, fakePlayer, pos, blockState, world.getTileEntity(pos), getStackInSlot(0), blockState.getBlock());
		world.setBlockToAir(pos);
		world.removeTileEntity(pos);
		return Math.min(80, (int) (hardness * 8));
	}

	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack, Block block) {
		if (block.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
			List<ItemStack> items = new ArrayList<ItemStack>();
			//TODO re-enable when the AT is in
//			ItemStack itemstack = block.getSilkTouchDrop(state);
//			if (!itemstack.isEmpty()) {
//				items.add(itemstack);
//			}
//			ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
//			for (ItemStack item : items) {
//				addItem(item);
//			}
		} else {
			int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
			NonNullList<ItemStack> items = NonNullList.create();
			block.getDrops(items, world, pos, state, fortune);
			for(ItemStack is : items){
				addItem(is); //TODO hold the machine and dont break any more blocks if the inv is full
			}
		}
	}

	private void addItem(ItemStack sourceStack){
		InventoryHelper.insertItemIntoInventory(this, sourceStack);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(index == 0){
			return stack.getItem() instanceof ItemPickaxe;
		}
		return super.isItemValidForSlot(index, stack);
	}

	private FakePlayer getFakePlayer() {
		Validate.isTrue(world instanceof WorldServer); //Fake players do not work on clients
		return FakePlayerFactory.getMinecraft((WorldServer) world);
	}

	@Override
	public boolean isWorking() {
		return !getTargetBlocks(world, getPos().offset(getFacing())).isEmpty();
	}

	@Override
	public boolean canBeUpgraded() {
		return false;
	}
}
