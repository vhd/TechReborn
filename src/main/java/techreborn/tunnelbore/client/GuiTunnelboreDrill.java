package techreborn.tunnelbore.client;

import net.minecraft.entity.player.EntityPlayer;
import techreborn.client.gui.GuiBase;
import techreborn.tunnelbore.TileTunnelboreDrill;

public class GuiTunnelboreDrill extends GuiBase{

	TileTunnelboreDrill tile;

	public GuiTunnelboreDrill(final EntityPlayer player, final TileTunnelboreDrill tile) {
		super(player, tile, tile.createContainer(player));
		this.tile = tile;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float f, final int mouseX, final int mouseY) {
		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		final GuiBase.Layer layer = GuiBase.Layer.BACKGROUND;
		this.drawSlot(80, 20, layer);

		for (int x = 0; x < 9; x++) {
			this.drawSlot(8 + (x * 18), 50, layer);
		}

	}

	@Override
	public boolean enableSlotConfig() {
		return false;
	}
}
