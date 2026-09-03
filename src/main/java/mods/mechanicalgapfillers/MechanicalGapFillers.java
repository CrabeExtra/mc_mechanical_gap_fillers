package mods.mechanicalgapfillers;

import mods.mechanicalgapfillers.blocks.FluidiserBlock;
import mods.mechanicalgapfillers.blocks.FluidiserBlockEntity;
import mods.mechanicalgapfillers.blocks.FluidiserMenu;
import mods.mechanicalgapfillers.sounds.FluidiserSounds;
import mods.mechanicalgapfillers.client.AutoEjectFluidPayload;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MechanicalGapFillers.MODID)
public class MechanicalGapFillers {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mechanicalgapfillers";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // menu registry, for interaction menus custom.
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, "mechanicalgapfillers");

    // block registry, blocks placed in space.
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // item registry, items you see in inventory (placed blocks need corresponding items)
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // block entity registry, info for a block that requires additional properties such as the ability to hold storage.
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "mechanicalgapfillers" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);



    // TODO: if I expand this mod much more I will want to think of a method to streamline adding new block types and item types without
    //          Cluttering up this class.

    // fluidiser block declaration.
    public static final String FluidiserName = "fluidiser";
    // Creates a new Block with the id "mechanicalgapfillers:example_block", combining the namespace and path
    public static final DeferredBlock<FluidiserBlock> FLUIDISER_BLOCK =
            BLOCKS.register(FluidiserName, () -> new FluidiserBlock(
                    BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0f, 6.0f)
                )
            );
    // Creates a new BlockItem with the id "mechanicalgapfillers:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> FLUIDISER_ITEM = ITEMS.registerSimpleBlockItem(FluidiserName, FLUIDISER_BLOCK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidiserBlockEntity>> FLUIDISER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register(
                    FluidiserName,
                    () -> BlockEntityType.Builder.of(
                            FluidiserBlockEntity::new,
                            FLUIDISER_BLOCK.get())
                    .build(null)
            );
    public static final DeferredHolder<MenuType<?>, MenuType<FluidiserMenu>> FLUIDISER_MENU =
            MENUS.register("fluidiser_menu", () -> IMenuTypeExtension.create(FluidiserMenu::new));

    // Creates a new food item with the id "mechanicalgapfillers:example_id", nutrition 1 and saturation 2
    //    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
    //            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "mechanicalgapfillers:main_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MGF_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mechanicalgapfillers")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> FLUIDISER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FLUIDISER_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MechanicalGapFillers(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // register menus to event bus.
        MENUS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register " for the block entities!! Why isn't this in the block entities section of the docs!!!
        BLOCK_ENTITIES.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // TODO: make this generic if I make more machines with unique sounds.
        FluidiserSounds.SOUND_EVENTS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MechanicalGapFillers) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
//
//        // Register the item to a creative tab
//        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(this::registerPayloads);

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        // I deleted the boilerplate.
    }

    // COMMENTED THIS OUT, it's not a building type block.
    // Add the example block item to the building blocks tab
    //    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    //        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
    //            event.accept(FLUIDISER_ITEM);
    //        }
    //    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");

        registrar.playToServer(
                AutoEjectFluidPayload.TYPE,
                AutoEjectFluidPayload.STREAM_CODEC,
                (payload, context) -> {
                    // Execute safely on the main server thread
                    context.enqueueWork(() -> {
                        if (context.player().level().getBlockEntity(payload.pos()) instanceof FluidiserBlockEntity be) {
                            be.autoEjectFluid = payload.enabled();
                            be.setChangedAndUpdate(); // Sync changes to all watching clients
                        }
                    });
                }
        );
    }
}
