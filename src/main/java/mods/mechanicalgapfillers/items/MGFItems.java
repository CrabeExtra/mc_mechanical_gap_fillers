package mods.mechanicalgapfillers.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static mods.mechanicalgapfillers.MechanicalGapFillers.FluidiserName;
import static mods.mechanicalgapfillers.MechanicalGapFillers.MODID;
import mods.mechanicalgapfillers.blocks.MGFBlocks;

public class MGFItems {

    // item registry, items you see in inventory (placed blocks need corresponding items)
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // upgrade registries
    public static final DeferredItem<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new UpgradeItem(UpgradeItem.UpgradeType.SPEED, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> DETERMINISTIC_UPGRADE = ITEMS.register("deterministic_upgrade",
            () -> new UpgradeItem(UpgradeItem.UpgradeType.DETERMINISTIC, new Item.Properties().stacksTo(16)));

    // Fluidiser item
    public static final DeferredItem<BlockItem> FLUIDISER_ITEM = ITEMS.registerSimpleBlockItem(FluidiserName, MGFBlocks.FLUIDISER_BLOCK);
}
