package techreborn.tunnelbore;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.Validate;
import techreborn.init.ModBlocks;

import javax.annotation.Nonnull;

public class TileTunnelboreController extends TileEntity implements ITickable {

	public BoreState currentState = BoreState.IDLE;

	public EnumFacing boreDirection = EnumFacing.NORTH;

	public MovingStructure structure;

	int cooldown = 0;

	@Override
	public void update() {
		if (currentState == BoreState.PREPARING_TO_MOVE) {
			structure = new MovingStructure(this);
			if (structure.prepareToMove()) {
				currentState = BoreState.MOVING;
			} else {
				structure = null;
				currentState = BoreState.COOL_DOWN;
				cooldown = 40;
			}
		} else if (currentState == BoreState.MOVING) {

			structure.translation.move(0.05);

			if (structure.translation.getDisplacement() > 1) {
				currentState = BoreState.COOL_DOWN;

				//Remove controller block before replacing the blocks
				world.setBlockToAir(getPos());
				structure.finishMoving();
				structure = null;
				cooldown = 15;
				replaceAndCopyController();
			}
		}

		if (currentState == BoreState.IDLE ){
			for(EnumFacing dir : EnumFacing.VALUES){
				if(world.isSidePowered(getPos(), dir)){
					startMoving();
					break;
				}
			}
		}

		if(currentState == BoreState.COOL_DOWN){
			cooldown--;
			if(cooldown <= 0){
				currentState = BoreState.IDLE;
			}
		}
	}

	public void startMoving() {
		currentState = BoreState.PREPARING_TO_MOVE;
	}

	private void replaceAndCopyController() {
		BlockPos targetPos = getPos().offset(boreDirection);
		world.setBlockState(targetPos, ModBlocks.TUNNEL_BORE_CONTROLLER.getDefaultState());
		TileTunnelboreController targetBore = (TileTunnelboreController) world.getTileEntity(targetPos);
		//TODO copy tile data using NBT and stuff
		targetBore.currentState = currentState;
		targetBore.boreDirection = boreDirection;
		targetBore.cooldown = cooldown;

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
