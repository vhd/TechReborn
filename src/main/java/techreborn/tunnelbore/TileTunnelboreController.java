package techreborn.tunnelbore;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.Validate;
import reborncore.common.network.VanillaPacketDispatcher;
import techreborn.init.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileTunnelboreController extends TileEntity implements ITickable {

	public BoreState currentState = BoreState.IDLE;

	public EnumFacing boreDirection = EnumFacing.NORTH;

	public MovingStructure structure;

	int cooldown = 0;

	@Override
	public void update() {
		if (currentState == BoreState.PREPARING_TO_MOVE && !world.isRemote) {
			structure = new MovingStructure(this);
			if (structure.prepareToMove()) {
				updateControllerState(BoreState.MOVING);
			} else {
				structure = null;
				updateControllerState(BoreState.COOL_DOWN);
				cooldown = 40;
			}
		} else if (currentState == BoreState.MOVING) {

			structure.translation.move(0.05);

			if (structure.translation.getDisplacement() > 1 && !world.isRemote) {
				updateControllerState(BoreState.COOL_DOWN);

				//Remove controller block before replacing the blocks
				world.setBlockToAir(getPos());
				structure.finishMoving();
				structure = null;
				cooldown = 15;
				replaceAndCopyController();
			}
		}

		if (currentState == BoreState.IDLE  && world.getTotalWorldTime() % 10 == 0){
			if(world.isBlockIndirectlyGettingPowered(getPos()) > 1){
				boreDirection = EnumFacing.SOUTH;
				startMoving();
			}

		}

		if(currentState == BoreState.COOL_DOWN  && !world.isRemote){
			cooldown--;
			if(cooldown <= 0){
				updateControllerState(BoreState.IDLE);
			}
		}
	}

	public void updateControllerState(BoreState boreState){
		if(getWorld().isRemote){
			return;
		}
		this.currentState = boreState;
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	public void startMoving() {
		if(currentState == BoreState.PREPARING_TO_MOVE || currentState == BoreState.MOVING){
			return;
		}
		updateControllerState(BoreState.PREPARING_TO_MOVE);
	}

	private void replaceAndCopyController() {
		BlockPos targetPos = getPos().offset(boreDirection);
		world.setBlockState(targetPos, ModBlocks.TUNNEL_BORE_CONTROLLER.getDefaultState());
		TileTunnelboreController targetBore = (TileTunnelboreController) world.getTileEntity(targetPos);
		targetBore.readFromNBT(writeToNBT(new NBTTagCompound()));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if(structure != null){
			compound.setTag("structure", structure.serializeNBT());
		}
		compound.setInteger("cooldown", cooldown);
		compound.setInteger("boreDirection", boreDirection.ordinal());
		compound.setInteger("currentState", currentState.ordinal());
		System.out.println(compound);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if(compound.hasKey("structure")){
			structure = new MovingStructure(this, compound.getCompoundTag("structure"));
		}
		cooldown = compound.getInteger("cooldown");
		boreDirection = EnumFacing.VALUES[compound.getInteger("boreDirection")];
		currentState = BoreState.values()[compound.getInteger("currentState")];
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = super.writeToNBT(new NBTTagCompound());
		writeToNBT(compound);
		return compound;
	}

	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readFromNBT(pkt.getNbtCompound());
	}

	private FakePlayer getFakePlayer() {
		Validate.isTrue(world instanceof WorldServer); //Fake players do not work on clients
		return FakePlayerFactory.getMinecraft((WorldServer) world);
	}

	public TileTunnelboreController getTunnelBore() {
		return this;
	}

	@Nonnull
	public Translation getTranslation() {
		return structure.translation;
	}

	public boolean isMoving() {
		return structure != null && structure.translation != null;
	}

	public static enum BoreState {
		IDLE,
		MOVING,
		PREPARING_TO_MOVE,
		COOL_DOWN
	}
}
