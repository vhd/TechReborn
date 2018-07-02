package techreborn.tunnelbore.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import reborncore.client.multiblock.RebornFluidRenderer;
import techreborn.tunnelbore.BlockData;
import techreborn.tunnelbore.TileTunnelboreController;

public class RenderTunnelBore extends TileEntitySpecialRenderer<TileTunnelboreController> {

	RebornFluidRenderer fluidRenderer = new RebornFluidRenderer();
	private ICamera camera;

	@Override
	public void render(TileTunnelboreController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		//Renders the controller, this is an actaull block that will be in the main world
		renderBlock(Blocks.IRON_BLOCK.getDefaultState(), te.getPos(), partialTicks, te, Minecraft.getMinecraft().world);

		//Renders all of the fake blocks that are not in the actual world
		if (te.isMoving()) {
			for (BlockData blockData : te.structure.getMovingBlocks()) {
				renderBlock(blockData.getRenderState() != null ? blockData.getRenderState() : blockData.getBlockState(), blockData.getOldPos(), partialTicks, te, Minecraft.getMinecraft().world);
				if (blockData.getTileEntity(Minecraft.getMinecraft().world) != null) {
					TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(blockData.getTileEntity(Minecraft.getMinecraft().world).getClass());
					if (renderer != null) {
						Vec3d offset = te.getTranslation().getOffset();
						double dx = x - te.getPos().getX();
						double dy = y - te.getPos().getY();
						double dz = z - te.getPos().getZ();

						renderer.render(blockData.getTileEntity(Minecraft.getMinecraft().world), blockData.getOldPos().getX() + offset.x + dx, blockData.getOldPos().getY() + offset.y + dy, blockData.getOldPos().getZ() + offset.z + dz, partialTicks, destroyStage, alpha);
					}
				}
			}
		}
	}

	private void renderBlock(IBlockState state, BlockPos anchor, float partialTicks, TileTunnelboreController tileTunnelboreController, World world) {
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayerSP player = minecraft.player;
		double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		if (camera == null) {
			camera = new Frustum();
		}
		camera.setPosition(dx, dy, dz);
		BlockPos pos = anchor;
		if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(pos))) {
			return;
		}

		minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();
		ForgeHooksClient.setRenderLayer(BlockRenderLayer.CUTOUT);

		Vec3d offset = new Vec3d(0, 0, 0);
		if (tileTunnelboreController.isMoving()) {
			offset = tileTunnelboreController.getTranslation().getOffset();
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(-dx + offset.x, -dy + offset.y, -dz + offset.z);
		GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

		this.renderModel(world, pos, state);
		GlStateManager.popMatrix();
		ForgeHooksClient.setRenderLayer(originalLayer);
	}

	private void renderModel(World world, BlockPos pos, IBlockState state) {
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().blockRenderDispatcher;
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		if (state.getRenderType() == EnumBlockRenderType.LIQUID) {
			fluidRenderer.renderFluid(world, state, pos, buffer);
		} else {
			blockRendererDispatcher.renderBlock(state, pos, world, buffer);
		}
		tessellator.draw();
	}
}
