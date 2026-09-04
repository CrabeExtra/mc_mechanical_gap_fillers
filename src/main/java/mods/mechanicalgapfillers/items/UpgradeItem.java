package mods.mechanicalgapfillers.items;

import mods.mechanicalgapfillers.blocks.FluidiserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class UpgradeItem extends Item {

    public final UpgradeType type;

    public enum UpgradeType {
        SPEED(MGFItems.SPEED_UPGRADE),
        DETERMINISTIC(MGFItems.DETERMINISTIC_UPGRADE);

        private final Supplier<Item> itemSupplier;

        UpgradeType(Supplier<Item> itemSupplier) {
            this.itemSupplier = itemSupplier;
        }

        public Item getItem() {
            return itemSupplier.get();
        }
    }

    public UpgradeItem(UpgradeType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (type == UpgradeType.SPEED) {
            tooltipComponents.add(Component.translatable("tooltip.mechanicalgapfillers.speed_upgrade"));
        } else if (type == UpgradeType.DETERMINISTIC) {
            tooltipComponents.add(Component.translatable("tooltip.mechanicalgapfillers.deterministic_upgrade"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Check if player is sneaking and target block is your Fluidiser
        if (player != null && player.isSecondaryUseActive()) {
            if (level.getBlockEntity(pos) instanceof FluidiserBlockEntity entity) {
                if (!level.isClientSide()) {
                    boolean success = entity.addUpgrade(this.type, player, context.getItemInHand());
                    if (success) {
                        player.displayClientMessage(Component.literal("Added " + this.type + " upgrade!"), true);
                    }
                }
                // Returning SUCCESS tells Minecraft the item interaction was handled
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return super.useOn(context);
    }
}
