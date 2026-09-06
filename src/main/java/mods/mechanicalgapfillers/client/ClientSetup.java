package mods.mechanicalgapfillers.client;

import mods.mechanicalgapfillers.fluids.MGFFluids;
import mods.mechanicalgapfillers.items.MGFItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

public class ClientSetup {

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/water_still");
            private static final ResourceLocation FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                // Light cyan tint (ARGB format)
                return 0xFF64DCFF;
            }
        }, MGFFluids.SOUL_WATER_TYPE.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            // Tint index 1 represents the fluid contained in the neoforge:item/bucket model
            if (tintIndex == 1) {
                return IClientFluidTypeExtensions.of(MGFFluids.SOUL_WATER_SOURCE.get())
                        .getTintColor();
            }
            return 0xFFFFFFFF; // Layer 0 (the metal bucket frame) remains un-tinted
        }, MGFItems.SOUL_WATER_BUCKET.get());
    }
}
