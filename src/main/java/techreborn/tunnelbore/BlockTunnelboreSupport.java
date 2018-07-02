package techreborn.tunnelbore;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import prospector.shootingstar.ShootingStar;
import prospector.shootingstar.model.ModelCompound;
import techreborn.lib.ModInfo;
import techreborn.utils.TechRebornCreativeTab;

public class BlockTunnelboreSupport extends Block {

	public BlockTunnelboreSupport() {
		super(Material.IRON, MapColor.IRON);
		setUnlocalizedName("techreborn.tunnelboresupport");
		setCreativeTab(TechRebornCreativeTab.instance);
		ShootingStar.registerModel(new ModelCompound(ModInfo.MOD_ID, this, "tunnelbore"));
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
		return true;
	}

}
