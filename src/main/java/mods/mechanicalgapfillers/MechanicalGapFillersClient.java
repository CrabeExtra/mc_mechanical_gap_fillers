package mods.mechanicalgapfillers;

import mods.mechanicalgapfillers.blocks.MGFBlocks;
import mods.mechanicalgapfillers.client.FluidiserScreen;
import mods.mechanicalgapfillers.fluids.MGFFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MechanicalGapFillers.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MechanicalGapFillers.MODID, value = Dist.CLIENT)
public class MechanicalGapFillersClient {
    public MechanicalGapFillersClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MGFBlocks.FLUIDISER_MENU.get(), FluidiserScreen::new);
    }



}
