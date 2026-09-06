package mods.mechanicalgapfillers.fluids;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import mods.mechanicalgapfillers.blocks.MGFBlocks;
import mods.mechanicalgapfillers.items.MGFItems;

import java.util.function.Consumer;

public class MGFFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MechanicalGapFillers.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, MechanicalGapFillers.MODID);

    public static final DeferredHolder<FluidType, FluidType> SOUL_WATER_TYPE = FLUID_TYPES.register("soul_water",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.mechanicalgapfillers.soul_water")
                    .density(1000)
                    .viscosity(1000)
                    .temperature(350)
                    .lightLevel(15)) {
            });

    public static final DeferredHolder<Fluid, FlowingFluid> SOUL_WATER_SOURCE = FLUIDS.register("soul_water_source",
            () -> new BaseFlowingFluid.Source(makeProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> SOUL_WATER_FLOWING = FLUIDS.register("soul_water_flowing",
            () -> new BaseFlowingFluid.Flowing(makeProperties()));

    private static BaseFlowingFluid.Properties makeProperties() {
        return new BaseFlowingFluid.Properties(
                SOUL_WATER_TYPE, SOUL_WATER_SOURCE, SOUL_WATER_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .block(MGFBlocks.SOUL_WATER_BLOCK)
                .bucket(MGFItems.SOUL_WATER_BUCKET);
    }

}
