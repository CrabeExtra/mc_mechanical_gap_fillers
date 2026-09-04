package mods.mechanicalgapfillers.integration.jei;

import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class FluidiserSplashingCategory implements IRecipeCategory<SplashingRecipe> {

    // Define the unique RecipeType key for JEI
    public static final RecipeType<SplashingRecipe> TYPE =
            RecipeType.create(MechanicalGapFillers.MODID, "fluidiser_splashing", SplashingRecipe.class);

    private final IDrawable icon;
    private final IDrawable slotBackground;

    private static final ResourceLocation EMPTY_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(
            MechanicalGapFillers.MODID, "textures/gui/fluidiser/progress.png"
    );

    public FluidiserSplashingCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(MGFBlocks.FLUIDISER_BLOCK.get())
        );
        // JEI provides a clean 18x18 slot background out of the box
        this.slotBackground = helper.getSlotDrawable();
    }

    @Override
    public @NotNull RecipeType<SplashingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public int getWidth() { return 120; }  // Category Window Width

    @Override
    public int getHeight() { return 60; }  // Category Window Height

    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Fluidiser Washing");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(SplashingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int width = getWidth();
        int height = getHeight();

        guiGraphics.fill(0, 0, width, height, 0xFFC6C6C6);
        guiGraphics.fill(0, 0, width, 1, 0xFFFFFFFF);
        guiGraphics.fill(0, 0, 1, height, 0xFFFFFFFF);
        guiGraphics.fill(0, height - 1, width, height, 0xFF555555);
        guiGraphics.fill(width - 1, 0, width, height, 0xFF555555);

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

        // 3. Progress Bar (Exact replica of your screen logic, adjusted for JEI bounds)
        int progX = 57; // Placed neatly between inputs (ends ~x:53) and outputs (starts x:81)
        int progY = 26; // Centered vertically with 18px slots (21px + 5px offset)
        int progWidth = 21;
        int progHeight = 7;

        // Simulate 0-100% progress cycle over 2 seconds (2000 ms) in JEI
        double cycle = (System.currentTimeMillis() % 2000) / 2000.0;
        int progress = (int) (cycle * 100.0);

        int barFill = (int) Math.floor(19.0 * (double) progress / 100.0);
        int fillColour = 0xFF595959;

        // Render the empty progress bar texture
        guiGraphics.blit(EMPTY_PROGRESS_BAR, progX, progY, 0, 0, progWidth, progHeight, progWidth, progHeight);

        // Render the animated fill overlay matching your exactScreen math
        guiGraphics.fill(
                progX + 1,
                progY + 1,
                Math.min(progX + barFill + 1, progX + progWidth - 1),
                progY + progHeight - 1,
                fillColour
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SplashingRecipe recipe, IFocusGroup focuses) {
        // Water Slot (X: 17, Y: 21)
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 21)
                .setBackground(this.slotBackground, -1, -1)
                .addFluidStack(Fluids.WATER, 1000);

        // Item Input Slot (X: 37, Y: 21)
        if (!recipe.getIngredients().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 37, 21)
                    .setBackground(this.slotBackground, -1, -1)
                    .addIngredients(recipe.getIngredients().get(0));
        }

        // Output Slots (X: 81+, Y: 21)
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