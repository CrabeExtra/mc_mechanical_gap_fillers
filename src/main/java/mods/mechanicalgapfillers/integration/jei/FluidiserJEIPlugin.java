package mods.mechanicalgapfillers.integration.jei;

import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mods.mechanicalgapfillers.MechanicalGapFillers;
import mods.mechanicalgapfillers.client.FluidiserScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mods.mechanicalgapfillers.blocks.MGFBlocks;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class FluidiserJEIPlugin implements IModPlugin {

    public static final RecipeType<Object> CREATE_SPLASHING =
            RecipeType.create("create", "splashing", Object.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MechanicalGapFillers.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        // Vanilla Blasting
        registration.addRecipeCatalyst(new ItemStack(MGFBlocks.FLUIDISER_BLOCK.get()), FluidiserBlastingCategory.TYPE);

        // Create Splashing (using JEI RecipeType)
        registration.addRecipeCatalyst(new ItemStack(MGFBlocks.FLUIDISER_BLOCK.get()), FluidiserSplashingCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // Register the visual template
        registration.addRecipeCategories(new FluidiserSplashingCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new FluidiserBlastingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        RecipeManager recipeManager = level.getRecipeManager();

        // Look up Create's recipe type by ResourceLocation
        var splashingTypeOpt = BuiltInRegistries.RECIPE_TYPE.getOptional(
                ResourceLocation.fromNamespaceAndPath("create", "splashing")
        );

        splashingTypeOpt.ifPresent(splashingType -> {
            // Fetch recipes and extract the inner value from RecipeHolder
            List<SplashingRecipe> splashingRecipes = recipeManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType<SplashingRecipe>) splashingType)
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            if (!splashingRecipes.isEmpty()) {
                registration.addRecipes(FluidiserSplashingCategory.TYPE, splashingRecipes);
            }
        });

        var blastingTypeOpt = BuiltInRegistries.RECIPE_TYPE.getOptional(
                ResourceLocation.fromNamespaceAndPath("minecraft", "blasting")
        );

        blastingTypeOpt.ifPresent(blastingType -> {
            // Fetch recipes and extract the inner value from RecipeHolder
            List<BlastingRecipe> blastingRecipes = recipeManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType<BlastingRecipe>) blastingType)
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            if (!blastingRecipes.isEmpty()) {
                registration.addRecipes(FluidiserBlastingCategory.TYPE, blastingRecipes);
            }
        });
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        int progX = 76;
        int progY = 40;
        int progWidth = 21;
        int progHeight = 7;
        registration.addRecipeClickArea(FluidiserScreen.class, progX, progY, progWidth, progHeight, RecipeTypes.BLASTING);
    }
}