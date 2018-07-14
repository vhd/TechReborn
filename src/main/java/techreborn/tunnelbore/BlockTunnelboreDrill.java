package techreborn.tunnelbore;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import prospector.shootingstar.ShootingStar;
import prospector.shootingstar.model.ModelCompound;
import reborncore.api.tile.IMachineGuiHandler;
import reborncore.common.blocks.BlockMachineBase;
import techreborn.client.EGui;
import techreborn.lib.ModInfo;
import techreborn.utils.TechRebornCreativeTab;

public class BlockTunnelboreDrill  extends BlockMachineBase {

	public BlockTunnelboreDrill() {
		super();
		setCreativeTab(TechRebornCreativeTab.instance);
		ShootingStar.registerModel(new ModelCompound(ModInfo.MOD_ID, this, "tunnelbore"));
	}

	@Override
	public TileEntity createNewTileEntity(final World world, final int meta) {
		return new TileTunnelboreDrill();
	}

	@Override
	public IMachineGuiHandler getGui() {
		return EGui.TUNNEL_BORE_DRILL;
	}
}
