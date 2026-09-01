package mods.mechanicalgapfillers.blocks;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FluidiserMenu extends AbstractContainerMenu {

    private final BlockEntity blockEntity;

    // Client-side constructor initialization
    public FluidiserMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv, playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Server-side constructor initialization
    public FluidiserMenu(int containerId, Inventory playerInv, BlockEntity blockEntity) {
        super(MechanicalGapFillers.FLUIDISER_MENU.get(), containerId);

        this.blockEntity = blockEntity;


        // input
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 0, 43, 35));

        // outputs
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 1, 106, 17));
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 2, 106, 53));

        // energy
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 3,16, 62));

        // modifier TODO: remove this and add a mouseclick handler (or another method) to allow water to be added to the fluidiser's 4th slot.
        //this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 4, 142, 62));

        // Add Player Inventory Slots (Bottom of the GUI grid)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Add Player Hotbar Slots
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Required method handling shift-clicking rules for safety
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStoredEnergy() {
        return this.blockEntity instanceof FluidiserBlockEntity fluidiserBe ? fluidiserBe.energyStorage.getEnergyStored() : 0;
    }
}