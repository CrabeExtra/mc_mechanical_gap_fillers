package mods.mechanicalgapfillers.integration.jei;

import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mods.mechanicalgapfillers.MechanicalGapFillers;
import mods.mechanicalgapfillers.blocks.MGFBlocks;
import mods.mechanicalgapfillers.fluids.MGFFluids;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class FluidiserHauntingCategory implements IRecipeCategory<HauntingRecipe> {
    public static final RecipeType<HauntingRecipe> TYPE =
            RecipeType.create(MechanicalGapFillers.MODID, "fluidiser_haunting", HauntingRecipe.class);

    private final IDrawable icon;
    private final IDrawable slotBackground;
    private final IDrawableAnimated progressBar;

    private static final ResourceLocation EMPTY_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(
            MechanicalGapFillers.MODID, "textures/gui/fluidiser/progress.png"
    );

    public FluidiserHauntingCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(MGFBlocks.FLUIDISER_BLOCK.get())
        );
        this.slotBackground = helper.getSlotDrawable();

        IDrawableStatic staticProgress = helper.createDrawable(EMPTY_PROGRESS_BAR, 0, 0, 21, 7);
        this.progressBar = helper.createAnimatedDrawable(staticProgress, 40, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public @NotNull RecipeType<HauntingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public int getWidth() { return 120; }

    @Override
    public int getHeight() { return 60; }

    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Fluidiser Haunting");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(HauntingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int width = getWidth();
        int height = getHeight();

        // Background box & borders
        guiGraphics.fill(0, 0, width, height, 0xFFC6C6C6);
        guiGraphics.fill(0, 0, width, 1, 0xFFFFFFFF);
        guiGraphics.fill(0, 0, 1, height, 0xFFFFFFFF);
        guiGraphics.fill(0, height - 1, width, height, 0xFF555555);
        guiGraphics.fill(width - 1, 0, width, height, 0xFF555555);

        // Heat/Level indicator gauge
        int barX = 3;
        int barY = 12;
        int barWidth = 8;
        int barHeight = 36;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        int fillHeight = (int) (barHeight * 0.75);
        guiGraphics.fill(
                barX + 1,
                barY + barHeight - fillHeight,
                barX + barWidth - 1,
                barY + barHeight - 1,
                0xFF00AA00
        );

        this.progressBar.draw(guiGraphics, 57, 26);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HauntingRecipe recipe, IFocusGroup focuses) {
        // Fluid Slot
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 21)
                .setBackground(this.slotBackground, -1, -1)
                .addFluidStack(MGFFluids.SOUL_WATER_SOURCE.get(), 1000);

        if (!recipe.getIngredients().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 37, 21)
                    .setBackground(this.slotBackground, -1, -1)
                    .addIngredients(recipe.getIngredients().get(0));
        }

        var rollableResults = recipe.getRollableResults();
        int startX = 81;
        int startY = 21;

        for (int i = 0; i < rollableResults.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, startX + (i * 18), startY)
                    .setBackground(this.slotBackground, -1, -1)
                    .addItemStack(rollableResults.get(i).getStack());
        }
    }
}
