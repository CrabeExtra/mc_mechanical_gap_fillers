package mods.mechanicalgapfillers.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import mods.mechanicalgapfillers.blocks.FluidiserMenu;

public class FluidiserScreen extends AbstractContainerScreen<FluidiserMenu> {
    // Points to a standard 256x256 GUI texture sheet (like a vanilla dispenser/chest)
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mechanicalgapfillers", "textures/screens/fluidiser_menu.png");

    public FluidiserScreen(FluidiserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        int maxEnergy = 50000;
        int currentEnergy = this.getMenu().getStoredEnergy();

        int barHeight = maxEnergy > 0 ? (int)(50 * ((float)currentEnergy / maxEnergy)) : 0;

        int barX = x + 154;
        int barY = y + 20 + (50 - barHeight); // Fills from the bottom upward
        guiGraphics.fill(barX, barY, barX + 10, barY + barHeight, 0xFF00FF00);
    }
}
