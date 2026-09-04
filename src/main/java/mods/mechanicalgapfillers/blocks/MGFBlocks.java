package mods.mechanicalgapfillers.blocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static mods.mechanicalgapfillers.MechanicalGapFillers.FluidiserName;
import static mods.mechanicalgapfillers.MechanicalGapFillers.MODID;

public class MGFBlocks {

    // block registry, blocks placed in space.
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // block entity registry, info for a block that requires additional properties such as the ability to hold storage.
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    // menu registry, for interaction menus custom. I could add a new class for menu
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, "mechanicalgapfillers");

    // Creates a new Block with the id "mechanicalgapfillers:example_block", combining the namespace and path
    public static final DeferredBlock<FluidiserBlock> FLUIDISER_BLOCK =
            BLOCKS.register(FluidiserName, () -> new FluidiserBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(5.0f, 6.0f)
                    )
            );
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
}
