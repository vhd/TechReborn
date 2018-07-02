package techreborn.tunnelbore;

import net.minecraft.block.state.IBlockState;
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

	@Override
	public void update() {
		if(currentState == BoreState.PREPARINGTOMOVE){
			structure = new MovingStructure(this);
			if(structure.prepareToMove()){
				currentState = BoreState.MOVING;
			}
		} else if(currentState == BoreState.MOVING){

			structure.translation.move(0.05);

			if(structure.translation.getDisplacement() > 1) {
				currentState = BoreState.IDLE;

				//Remove controller block before replacing the blocks
				world.setBlockToAir(getPos());
				structure.finishMoving();
				structure = null;
				replaceAndCopyController();
			}
		}
	}

	public void startMoving(){
		currentState = BoreState.PREPARINGTOMOVE;
	}

	private void replaceAndCopyController(){
		BlockPos targetPos = getPos().offset(boreDirection);
		world.setBlockState(targetPos, ModBlocks.TUNNEL_BORE_CONTROLLER.getDefaultState());
		TileTunnelboreController targetBore = (TileTunnelboreController) world.getTileEntity(targetPos);
		//TODO copy tile data using NBT and stuff
		targetBore.currentState = currentState;
		targetBore.boreDirection = boreDirection;

	}

	private FakePlayer getFakePlayer(){
		Validate.isTrue(world instanceof WorldServer); //Fake players do not work on clients
		return FakePlayerFactory.getMinecraft((WorldServer)world);
	}

	public TileTunnelboreController getTunnelBore(){
		return this;
	}

	@Nonnull
	public Translation getTranslation() {
		return structure.translation;
	}

	public boolean isMoving(){
		return structure != null;
	}

	public static enum BoreState {
		IDLE,
		MOVING,
		PREPARINGTOMOVE
	}
}
