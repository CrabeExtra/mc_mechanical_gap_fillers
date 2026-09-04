package mods.mechanicalgapfillers.client;

import mods.mechanicalgapfillers.blocks.FluidiserBlockEntity;
import mods.mechanicalgapfillers.utility.energy.Joules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import mods.mechanicalgapfillers.blocks.FluidiserMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jline.reader.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class FluidiserScreen extends AbstractContainerScreen<FluidiserMenu> {
    // Points to a standard 256x256 GUI texture sheet (like a vanilla dispenser/chest)
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mechanicalgapfillers", "textures/gui/fluidiser/fluidiser_menu.png");
    private static final ResourceLocation EMPTY_ENERGY_BAR =
            ResourceLocation.fromNamespaceAndPath("mechanicalgapfillers", "textures/gui/fluidiser/energy_bar_0.png");
    private static final ResourceLocation EMPTY_PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath("mechanicalgapfillers", "textures/gui/fluidiser/progress.png");
    private static final WidgetSprites EJECT_BUTTON_TEXTURE = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("mechanicalgapfillers", "fluidiser/eject"),
            ResourceLocation.fromNamespaceAndPath("mechanicalgapfillers", "fluidiser/eject")
    );

    private void addScaledCheckbox(int x, int y, float scale, boolean initialState, BiConsumer<net.minecraft.client.gui.components.Checkbox, Boolean> onValueChange, Component tooltipText) {

        var originalCheckbox = net.minecraft.client.gui.components.Checkbox.builder(CommonComponents.EMPTY, this.font)
                .pos(0, 0)
                .selected(initialState)
                .onValueChange(onValueChange::accept)
                .build();

        var tooltip = Tooltip.create(tooltipText);

        int customSize = (int)(20 * scale);

        this.addRenderableWidget(new AbstractWidget(x, y, customSize, customSize, CommonComponents.EMPTY) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(this.getX(), this.getY(), 0);
                guiGraphics.pose().scale(scale, scale, 1.0f);

                int localMouseX = (int)((mouseX - this.getX()) / scale);
                int localMouseY = (int)((mouseY - this.getY()) / scale);

                originalCheckbox.active = this.active;
                originalCheckbox.visible = this.visible;
                originalCheckbox.renderWidget(guiGraphics, localMouseX, localMouseY, partialTick);
                guiGraphics.pose().popPose();

                if (this.isHoveredOrFocused()) {
                    this.setTooltip(tooltip);
                }
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                originalCheckbox.onPress();
            }

            @Override
            protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {}
        });
    }

    @Override
    protected void init() {
        super.init();

        // add button for ejecting upgrades.
        ImageButton ejectUpgradesButton = new ImageButton(
            this.leftPos + 160, this.topPos + 10,
            10, 10,
            EJECT_BUTTON_TEXTURE,
            button -> {
                PacketDistributor.sendToServer(new EjectUpgradesPayload());
            }
        );

        ejectUpgradesButton.setTooltip(Tooltip.create(Component.literal("Eject all upgrades")));

        this.addRenderableWidget(ejectUpgradesButton);

        // add checkbox for auto-ejecting fluid.
        this.addScaledCheckbox(
            this.leftPos + 160, this.topPos + 70,
            0.5f,
            this.menu.data.get(FluidiserBlockEntity.AUTO_EJECT_DATA) != 0,
            (checkbox, isChecked) -> {
                PacketDistributor.sendToServer(
                        new AutoEjectFluidPayload(this.menu.getBlockEntity().getBlockPos(), isChecked)
                );
            },
            Component.literal("Auto-eject fluid") // Hover tooltip
        );
    }

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
        // render the title label.
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        // handle the onhover label for the water tank
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        FluidiserBlockEntity fluidiserBe = this.menu.getBlockEntity();

        if(this.hoveredSlot != null) {
            // Fluid label
            if (this.hoveredSlot.index == FluidiserBlockEntity.MODIFIER_FLUID_SLOT) {
                FluidTank tank = fluidiserBe.fluidTank;

                List<Component> tooltip = new ArrayList<>();

                if (!tank.isEmpty()) {
                    // Get the proper localized name of the fluid (e.g., "Water" or "Lava")
                    Component fluidName = tank.getFluidInTank(0).getHoverName();
                    tooltip.add(fluidName);

                    // Add the millibucket capacity row text underneath
                    String amountText = tank.getFluidAmount() + "mB";
                    tooltip.add(Component.literal(amountText).withStyle(ChatFormatting.GRAY));
                } else {
                    // Label display if the machine currently holds nothing
                    tooltip.add(Component.translatable("gui.mechanicalgapfillers.fluidiser.fluidTank.empty"));
                    tooltip.add(Component.literal("0mB").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.translatable("gui.mechanicalgapfillers.fluidiser.fluidTank.info").withStyle(ChatFormatting.BLUE));
                }

                // Draw the completed hovering split string annotation right at cursor location
                // (Offset coordinates backward out of local GUI space back to global mouse space)
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX - x, mouseY - y);
            }

            // energy slot label
            if(this.hoveredSlot.index == FluidiserBlockEntity.ENERGY_SLOT && !this.hoveredSlot.hasItem()) {

                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("gui.mechanicalgapfillers.fluidiser.energySlot.info").withStyle(ChatFormatting.BLUE));
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX - x, mouseY - y);
            }
        }

        // energy bar label.
        int barX = x + 6;
        int barY = y + 16;
        int barWidth = 8;
        int barHeight = 63;

        if (mouseX >= barX && mouseX < barX + barWidth && mouseY >= barY && mouseY < barY + barHeight) {

            // create tooltip
            List<Component> tooltip = new ArrayList<>();

            // get joules amount as text.
            String amountText = Joules.feToJ(this.menu.data.get(FluidiserBlockEntity.ENERGY_DATA)) + "J";

            // add as tooltip.
            tooltip.add(Component.literal(amountText).withStyle(ChatFormatting.GREEN));

            // render.
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX - x, mouseY - y);
        }


    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if the user clicked the slot area matching x=142, y=62
        if (this.hoveredSlot != null && this.hoveredSlot.x == 142 && this.hoveredSlot.y == 62) {
            ItemStack cursorItem = this.menu.getCarried();

            if (this.menu.getBlockEntity() instanceof FluidiserBlockEntity fluidiserBe) {
                var fluidInTank = fluidiserBe.fluidTank.getFluidInTank(0).getFluid();

                // 1. Draining Water
                if (fluidInTank.equals(Fluids.WATER) && cursorItem.is(Items.BUCKET)) {
                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.BUCKET_FILL, 1.0F)
                    );
                    // 2. Draining Lava
                } else if (fluidInTank.equals(Fluids.LAVA) && cursorItem.is(Items.BUCKET)) {
                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.BUCKET_FILL_LAVA, 1.0F)
                    );
                    // 3. Filling Tank
                } else if (fluidiserBe.fluidTank.isEmpty()) {
                    if (cursorItem.is(Items.WATER_BUCKET)) {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.BUCKET_EMPTY, 1.0F)
                        );
                    } else if (cursorItem.is(Items.LAVA_BUCKET)) {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.BUCKET_EMPTY_LAVA, 1.0F)
                        );
                    }
                }
            }
        }

        // Always delegate to super so Minecraft forwards the slot click event to FluidiserMenu!
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // render generic menu background.
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // render progress bar.

        int progX = x + 76;
        int progY = y + 40;
        int progWidth = 21;
        int progHeight = 7;

        int progress = this.menu.data.get(FluidiserBlockEntity.PROGRESS_DATA);

        int barFill = (int) Math.floor(19.0 * (double) progress / 100.0);

        int fillColour = 0xFF595959;

        // render the bar itself.
        guiGraphics.blit(EMPTY_PROGRESS_BAR, progX, progY, 0, 0, progWidth, progHeight, progWidth, progHeight);

        // render the energy %.
        guiGraphics.fill(
            progX + 1,
                progY + 1,
            Math.min(progX + barFill + 1, progX + progWidth - 1),
                progY + progHeight - 1,
            fillColour
        );

        // render energy bar.

        int currentEnergy = this.menu.data.get(FluidiserBlockEntity.ENERGY_DATA);
        int maxEnergy = FluidiserBlockEntity.MAX_ENERGY_STORAGE;

        int barX = x + 6;
        int barY = y + 16;
        int barWidth = 8;
        int barHeight = 63;

        int fillHeight = maxEnergy > 0 ? (int) ((long) currentEnergy * barHeight / maxEnergy) : 0;

        int energyColor = 0xFF00AA00; // pretty solid green 'energy'.

        // render the bar itself.
        guiGraphics.blit(EMPTY_ENERGY_BAR, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // render the energy %.
        guiGraphics.fill(
        barX + 1,                                                // Left inset edge of your inner square slot area
            Math.max(barY + barHeight - fillHeight - 1, barY + 1),  // Dynamically moves the top bounds downward as energy drops
            barX + barWidth - 1,                                    // Right inset edge
            barY + barHeight - 1,                                   // Bottom bounds locked to the bottom of the container
            energyColor
        );

        // water tank
        if (this.menu.getBlockEntity() instanceof FluidiserBlockEntity fluidiserBe) {
            FluidTank tank = fluidiserBe.fluidTank;

            if (!tank.isEmpty()) {
                Fluid fluid = tank.getFluidInTank(0).getFluid();
                FluidType fluidType = fluid.getFluidType();

                IClientFluidTypeExtensions extensions =
                        IClientFluidTypeExtensions.of(fluidType);

                ResourceLocation stillTexture = extensions.getStillTexture();

                if (stillTexture != null) {
                    TextureAtlasSprite fluidSprite =
                            this.minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)
                                    .apply(stillTexture);

                    int tintColor = extensions.getTintColor(tank.getFluidInTank(0));
                    float a = ((tintColor >> 24) & 0xFF) / 255.0f;
                    float r = ((tintColor >> 16) & 0xFF) / 255.0f;
                    float g = ((tintColor >> 8) & 0xFF) / 255.0f;
                    float b = (tintColor & 0xFF) / 255.0f;

                    // Set color, paint the 16x16 block, then reset color channels
                    guiGraphics.setColor(r, g, b, a);
                    guiGraphics.blit(x + 142, y + 62, 0, 16, 16, fluidSprite);
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
    }


}
