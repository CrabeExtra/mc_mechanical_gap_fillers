package mods.mechanicalgapfillers.events;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

// Notice: NO dist = Dist.CLIENT here! This runs on both Server and Client.
@EventBusSubscriber(modid = MechanicalGapFillers.MODID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                MechanicalGapFillers.FLUIDISER_BLOCK_ENTITY.get(), // Ensure this matches your BE DeferredHolder
                (blockEntity, side) -> blockEntity.energyStorage
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MechanicalGapFillers.FLUIDISER_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.externalInventory
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MechanicalGapFillers.FLUIDISER_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.fluidTank
        );
    }
}