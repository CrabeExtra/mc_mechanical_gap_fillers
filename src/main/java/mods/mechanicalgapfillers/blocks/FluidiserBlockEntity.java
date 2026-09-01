
package mods.mechanicalgapfillers.blocks;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FluidiserBlockEntity extends BlockEntity implements MenuProvider {

    public final EnergyStorage energyStorage = new EnergyStorage(50000, 1000, 1000);

    public final ItemStackHandler inventory = new ItemStackHandler(5) {

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> {
                    ItemStack stackInModifierSlot = this.getStackInSlot(3);
                    if(stackInModifierSlot == Items.WATER_BUCKET || )
                        yield false;
                } // input - user can try.
                case 1 -> false; // outputs
                case 2 -> false;
                case 3 -> stack.is(Items.REDSTONE) || stack.is(Items.REDSTONE_BLOCK); // energy input
                default -> 0; // Not a valid fuel source
            };

        }
    };


    public FluidiserBlockEntity(BlockPos pos, BlockState state) {
        super(MechanicalGapFillers.FLUIDISER_BLOCK_ENTITY.get(), pos, state);
    }

    private int consumeRedstone(ItemStack redstone) {

        int energyToAdd = switch (redstone.getItem()) {
            case Item item when redstone.is(Items.REDSTONE) -> 4000;       // 4,000 FE (10,000 J)
            case Item item when redstone.is(Items.REDSTONE_BLOCK) -> 14400; // 36,000 FE (90,000 J)
            default -> 0; // Not a valid fuel source
        };

        if(energyToAdd == 0) return 0;

        redstone.shrink(1);

        return energyToAdd;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidiserBlockEntity blockEntity) {
        ItemStack inputStack = blockEntity.inventory.getStackInSlot(0);

        // consumes redstone dust for energy.
        blockEntity.energyStorage.receiveEnergy(blockEntity.consumeRedstone(inputStack), false);

        if (blockEntity.energyStorage.getEnergyStored() >= 100) {
            // TODO: energy usage
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Fluidiser"); // The title displayed at the top of the GUI
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FluidiserMenu(containerId, playerInventory, this);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        public int getSlotLimit(int slot) {
            return 64; // Limits all item slot sizes to a full stack
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1 || slot == 2) return false; // Prevents putting items straight into the outputs
            return true;
        }
    };

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.inventory.serializeNBT(registries));
        tag.putInt("Energy", this.energyStorage.getEnergyStored());
    }

    // 5. Load machine inventory and power data when the chunk loads
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        this.energyStorage.receiveEnergy(tag.getInt("Energy"), false);
    }

}