package mods.mechanicalgapfillers.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class FluidiserMenu extends AbstractContainerMenu {

    private final FluidiserBlockEntity blockEntity;
    public final ContainerData data;

    public FluidiserMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        // Pass a SimpleContainerData(DATA_COUNT) instead of reading from client BlockEntity!
        this(
                containerId,
                playerInv,
                (FluidiserBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(FluidiserBlockEntity.DATA_COUNT)
        );
    }

    public FluidiserMenu(int containerId, Inventory playerInv, FluidiserBlockEntity entity, ContainerData data) {
        super(MGFBlocks.FLUIDISER_MENU.get(), containerId);

        this.blockEntity = entity instanceof FluidiserBlockEntity ? (FluidiserBlockEntity) entity : null;
        this.data = this.blockEntity != null ? this.blockEntity.data : new SimpleContainerData(FluidiserBlockEntity.DATA_COUNT);

        // add
        this.addDataSlots(this.data);

        // input
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 0, 52, 35) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                blockEntity.progress = 0;
            }
        });

        // outputs
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 1, 106, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 2, 106, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // energy
        this.addSlot(new SlotItemHandler(((FluidiserBlockEntity) blockEntity).inventory, 3,16, 62));

        // modifier
        this.addSlot(new Slot(new SimpleContainer(1), 0, 142, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

        });

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

    public FluidiserBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private void  giveItemToPlayer (Player player, ItemStack stackToGive) {
        if (player.level().isClientSide()) {
            return;
        }

        boolean added = player.getInventory().add(stackToGive);

        if (!added || !stackToGive.isEmpty()) {
            player.drop(stackToGive, false);
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Check if the player clicked our custom virtual slot ID and is holding a water bucket
        if (slotId >= 0 && slotId < this.slots.size() && this.getSlot(slotId).x == 142 && this.getSlot(slotId).y == 62) {
            ItemStack cursorItem = this.getCarried();

            if (this.blockEntity instanceof FluidiserBlockEntity fluidiserBe) {

                // if water in the tank, replace empty bucket in hand with water bucket.
                if(fluidiserBe.fluidTank.getFluidInTank(0).is(Fluids.WATER)) {
                    if(!cursorItem.is(Items.BUCKET)) return;

                    fluidiserBe.fluidTank.drain(
                        new FluidStack(Fluids.WATER, 1000),
                        IFluidHandler.FluidAction.EXECUTE
                    );

                    Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.BUCKET_FILL, 1.0F) // Pitch: 1.0F
                    );

                    fluidiserBe.setChangedAndUpdate();

                    if(cursorItem.getCount() > 1) {
                        giveItemToPlayer(player, new ItemStack(Items.BUCKET, cursorItem.getCount() - 1));
                    }

                    this.setCarried(new ItemStack(Items.WATER_BUCKET));

                    return;
                    // if lava in the tank, replace empty bucket in hand with lava bucket.
                } else if(fluidiserBe.fluidTank.getFluidInTank(0).is(Fluids.LAVA)) {
                    if(!cursorItem.is(Items.BUCKET)) return;

                    fluidiserBe.fluidTank.drain(
                        new FluidStack(Fluids.LAVA, 1000),
                        IFluidHandler.FluidAction.EXECUTE
                    );

                    Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.BUCKET_FILL_LAVA, 1.0F) // Pitch: 1.0F
                    );

                    fluidiserBe.setChangedAndUpdate();

                    if(cursorItem.getCount() > 1) {
                        giveItemToPlayer(player, new ItemStack(Items.BUCKET, cursorItem.getCount() - 1));
                    }

                    this.setCarried(new ItemStack(Items.LAVA_BUCKET));
                    return;
                    // if tank empty, add bucket fluid to tank if lava or water.
                } else if(fluidiserBe.fluidTank.isEmpty()) {
                    Fluid fluidInBucket =
                            cursorItem.is(Items.WATER_BUCKET)  ? Fluids.WATER
                            : cursorItem.is(Items.LAVA_BUCKET) ? Fluids.LAVA
                            : Fluids.EMPTY;

                    if(fluidInBucket.equals(Fluids.EMPTY)) return;

                    // Fill the tank with exactly 1000mB (1 Bucket)
                    fluidiserBe.fluidTank.fill(
                            new FluidStack(fluidInBucket, 1000),
                            IFluidHandler.FluidAction.EXECUTE
                    );

                    if(fluidInBucket.equals(Fluids.WATER)) {
                        Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.BUCKET_EMPTY, 1.0F) // Pitch: 1.0F
                        );
                    } else if(fluidInBucket.equals(Fluids.LAVA)) { // is currently always true but I might add soul lava later for haunting effect >:)
                        Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.BUCKET_EMPTY_LAVA, 1.0F) // Pitch: 1.0F
                        );
                    }

                    fluidiserBe.setChangedAndUpdate();

                    // Instantly transform the fluid bucket into an empty bucket
                    this.setCarried(new ItemStack(Items.BUCKET));
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Total number of machine container slots
            final int MACHINE_SLOTS = 4;

            final int INPUT_SLOT = FluidiserBlockEntity.INPUT_SLOT;
            final int ENERGY_SLOT = FluidiserBlockEntity.ENERGY_SLOT;

            if (index < MACHINE_SLOTS) {
                if (!this.moveItemStackTo(itemstack1, MACHINE_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }

            else {
                if (itemstack1.is(Items.REDSTONE) || itemstack1.is(Items.REDSTONE_BLOCK)) {
                    if (!this.moveItemStackTo(itemstack1, ENERGY_SLOT, ENERGY_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }

                else {
                    if (!this.moveItemStackTo(itemstack1, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

}