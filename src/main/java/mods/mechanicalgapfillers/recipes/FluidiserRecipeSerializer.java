package mods.mechanicalgapfillers.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidiserRecipeSerializer implements RecipeSerializer<FluidiserRecipe> {

    // 1. MapCodec for JSON (Data Packs & Recipes)
    public static final MapCodec<FluidiserRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("input_item").forGetter(FluidiserRecipe::getInputItem),
                    FluidStack.CODEC.fieldOf("input_fluid").forGetter(FluidiserRecipe::getInputFluid),
                    ItemStack.SINGLE_ITEM_CODEC.fieldOf("result").forGetter(FluidiserRecipe::getResultItemDirect),
                    Codec.INT.optionalFieldOf("processing_time", 100).forGetter(FluidiserRecipe::getProcessTime)
            ).apply(instance, FluidiserRecipe::new)
    );

    // 2. StreamCodec for Networking (Server -> Client Packet Sync)
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidiserRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, FluidiserRecipe::getInputItem,
            FluidStack.STREAM_CODEC, FluidiserRecipe::getInputFluid,
            ItemStack.STREAM_CODEC, FluidiserRecipe::getResultItemDirect,
            ByteBufCodecs.VAR_INT, FluidiserRecipe::getProcessTime,
            FluidiserRecipe::new
    );

    @Override
    public MapCodec<FluidiserRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidiserRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}