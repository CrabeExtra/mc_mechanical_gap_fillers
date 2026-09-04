package mods.mechanicalgapfillers.recipes;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class FluidiserRecipe implements Recipe<RecipeWrapper> {
    private final Ingredient inputItem;
    private final FluidStack inputFluid;
    private final ItemStack resultItem;
    private final int processTime;

    public FluidiserRecipe(Ingredient inputItem, FluidStack inputFluid, ItemStack resultItem, int processTime) {
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.resultItem = resultItem;
        this.processTime = processTime;
    }

    public Ingredient getInputItem() { return inputItem; }
    public FluidStack getInputFluid() { return inputFluid; }
    public int getProcessTime() { return processTime; }

    @Override
    public boolean matches(RecipeWrapper container, @NotNull Level level) {
        return this.inputItem.test(container.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeWrapper container, HolderLookup.@NotNull Provider registries) {
        return this.resultItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) { return this.resultItem; }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return MGFRecipes.FLUIDISER_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return MGFRecipes.FLUIDISER_TYPE.get(); }

    public ItemStack getResultItemDirect() {
        return this.resultItem;
    }
}
