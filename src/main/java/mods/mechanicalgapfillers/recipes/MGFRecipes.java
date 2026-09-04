package mods.mechanicalgapfillers.recipes;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MGFRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MechanicalGapFillers.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MechanicalGapFillers.MODID);

    public static final Supplier<RecipeType<FluidiserRecipe>> FLUIDISER_TYPE =
            RECIPE_TYPES.register(MechanicalGapFillers.FluidiserName,
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MechanicalGapFillers.MODID, "fluidiser")));

    public static final Supplier<RecipeSerializer<FluidiserRecipe>> FLUIDISER_SERIALIZER =
            RECIPE_SERIALIZERS.register(MechanicalGapFillers.FluidiserName, FluidiserRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
