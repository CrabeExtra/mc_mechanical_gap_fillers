
package mods.mechanicalgapfillers.blocks;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import mods.mechanicalgapfillers.MechanicalGapFillers;
import mods.mechanicalgapfillers.sounds.FluidiserSounds;
import mods.mechanicalgapfillers.utility.energy.MGFEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidiserBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PROGRESS_DATA = 0;
    public static final int ENERGY_DATA = 1;
    public static final int AUTO_EJECT_DATA = 2;

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT_1 = 1;
    public static final int OUTPUT_SLOT_2 = 2;
    public static final int ENERGY_SLOT = 3;
    public static final int MODIFIER_FLUID_SLOT = 4;

    public static final int DATA_COUNT = 3;

    public static final int MAX_ENERGY_STORAGE = 14400;

    public static final int OFF_STATE = 0;
    public static final int WATER_JET_STATE = 2;
    public static final int WATER_IDLE_STATE = 1;
    public static final int LAVA_JET_STATE = 4;
    public static final int LAVA_IDLE_STATE = 3;

    public final MGFEnergyStorage energyStorage = new MGFEnergyStorage(MAX_ENERGY_STORAGE, MAX_ENERGY_STORAGE, MAX_ENERGY_STORAGE);

    private boolean updateFluidMenu = false;
    public boolean autoEjectFluid = false; // the user could simply list the non-consumed fluid as an output as well as input, but people dont always think of that.
    public final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged(); // Marks chunk dirty for world saves
            updateFluidMenu = true;
        }
    };
    public double progress = 0;

    private int soundTimer = 0;
    private int soundCooldown = 0;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch(index) {
                case 0 -> (int) FluidiserBlockEntity.this.progress;
                case 1 -> {
                    int value = energyStorage.getEnergyStored();

                    yield value;
                }
                case 2 -> autoEjectFluid ? 1 : 0;
                default -> -1;
            };
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case 0:
                    FluidiserBlockEntity.this.progress = value;
                    break;
                case 1:
                    energyStorage.setEnergyStored(value);
                    break;
                case 2:
                    autoEjectFluid = value != 0;
                    break;
            };

        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> {
                    yield true;
                } // input - user can try.
                case 1 -> true; // outputs
                case 2 -> true;
                case 3 -> stack.is(Items.REDSTONE) || stack.is(Items.REDSTONE_BLOCK); // energy input
                default -> false; // Not a valid scenario.
            };

        }
    };

    // exposing the inventory to an external source under some circumstances (cant extract from input slots, insert to export slots etc)
    public final IItemHandler externalInventory = new IItemHandler() {
        @Override
        public int getSlots() { return inventory.getSlots(); }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }

        @Override
        public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == OUTPUT_SLOT_1 || slot == OUTPUT_SLOT_2) return false;
            return inventory.isItemValid(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == OUTPUT_SLOT_1 || slot == OUTPUT_SLOT_2 || slot == ENERGY_SLOT) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == INPUT_SLOT || slot == ENERGY_SLOT) return ItemStack.EMPTY;
            return inventory.extractItem(slot, amount, simulate);
        }
    };


    public FluidiserBlockEntity(BlockPos pos, BlockState state) {
        super(MechanicalGapFillers.FLUIDISER_BLOCK_ENTITY.get(), pos, state);
    }

    private boolean isSplashable(ItemStack stack, Level level) {
        if (stack.isEmpty()) return false;

        // In 1.21.1, recipes use input wrappers rather than raw Container wrappers
        SingleRecipeInput input = new SingleRecipeInput(stack);

        return level.getRecipeManager()
                .getRecipeFor(AllRecipeTypes.SPLASHING.getType(), input, level)
                .isPresent();
    }

    private boolean isBlastable(ItemStack stack, Level level) {
        if (stack.isEmpty()) return false;

        SingleRecipeInput input = new SingleRecipeInput(stack);

        return level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, input, level)
                .isPresent();
    }

    private int consumeRedstone() {
        ItemStack redstone = inventory.getStackInSlot(ENERGY_SLOT);

        int energyToAdd = switch (redstone.getItem()) {
            case Item item when redstone.is(Items.REDSTONE) -> 4000;       // 4,000 J (10,000 J)
            case Item item when redstone.is(Items.REDSTONE_BLOCK) -> 14400; // 36,000 J (14,400 J)
            default -> 0; // Not a valid fuel source
        };

        if(energyToAdd == 0) return 0;

        redstone.shrink(1);

        return energyToAdd;
    }

    private void redstoneForPowerTickRun() {
        ItemStack energyInputStack = inventory.getStackInSlot(ENERGY_SLOT);

        int feFromFullEnergyBuffer = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();

        if (energyInputStack.getCount() > 0
            && ((energyInputStack.is(Items.REDSTONE_BLOCK) && feFromFullEnergyBuffer >= 14400)
            || (energyInputStack.is(Items.REDSTONE) && feFromFullEnergyBuffer >= 4000))
        ) {
            energyStorage.receiveEnergy(consumeRedstone(), false);

            this.setChangedAndUpdate();
        }
    }

    private void handleMachineOperationRunning() {
        double secondsToProcess = fluidTank.getFluidInTank(0).is(Fluids.LAVA) ? 5 // 5 second baseline, keep in mind this needs to be balanced, otherwise iron generation would be very fast indeed.
                : fluidTank.getFluidInTank(0).is(Fluids.WATER) ? 2 // 2 second baseline, will create upgraded factory versions that are quicker.
                : 0;
        double ticksPerSecond = 20;
        double totalPowerRequiredForOneOperation = 4000;
        double maxProgress = 100;

        double totalTicksRequired = ticksPerSecond * secondsToProcess;

        int powerDrawPerTick = (int) Math.floor(totalPowerRequiredForOneOperation / totalTicksRequired);
        double incrementPerTick = maxProgress / totalTicksRequired;

        progress += incrementPerTick;
        energyStorage.extractEnergy(powerDrawPerTick, false);
    }

    private void handleMachineSoundsAndTextureChange(BlockPos pos, BlockState state) {

        soundCooldown--;

        if (isRunning() && soundCooldown <= 0) {

            if(fluidTank.getFluidInTank(0).getFluid() == Fluids.LAVA) {
                if (soundTimer % 20 == 0) {

                    level.setBlock(pos, state.setValue(FluidiserBlock.WORKING_STAGE, LAVA_JET_STATE), 3);
                    // Play running sound
                    level.playSound(
                        null,
                        pos,
                        FluidiserSounds.RUNNING_SOUND.get(),
                        SoundSource.BLOCKS,
                        0.5F,
                        0.2F
                    );

                    level.playSound(
                        null, pos,
                        SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.8F, // Volume
                        1.3F
                    );

                    level.playSound(
                        null, pos,
                        SoundEvents.LAVA_AMBIENT,
                        SoundSource.BLOCKS,
                        0.8F, // Volume
                        1.3F
                    );
                }
            } else if(fluidTank.getFluidInTank(0).getFluid() == Fluids.WATER) {
                // Loop interval based on your sound file duration (e.g. 40 ticks = 2 seconds)
                if (soundTimer % 20 == 0) {
                    level.setBlock(pos, state.setValue(FluidiserBlock.WORKING_STAGE, WATER_JET_STATE), 3);
                    // Play running sound
                    level.playSound(
                        null,
                        pos,
                        FluidiserSounds.RUNNING_SOUND.get(),
                        SoundSource.BLOCKS,
                        0.5F,
                        0.2F
                    );

                    level.playSound(
                        null, pos,
                        SoundEvents.WEATHER_RAIN,
                        SoundSource.BLOCKS,
                        0.8F, // Volume
                        1.3F
                    );
                }
            }

            soundTimer++;
            soundCooldown = 20;
        } else {
            soundTimer = 0; // Reset when inactive
        }

        if(soundCooldown <= 0) {
            level.setBlock(pos, state.setValue(FluidiserBlock.WORKING_STAGE, OFF_STATE), 3);
        } else if(soundCooldown < 10) {
            if(fluidTank.getFluidInTank(0).getFluid() == Fluids.WATER) {
                level.setBlock(pos, state.setValue(FluidiserBlock.WORKING_STAGE, WATER_IDLE_STATE), 3);
            }

            if(fluidTank.getFluidInTank(0).getFluid() == Fluids.LAVA) {
                level.setBlock(pos, state.setValue(FluidiserBlock.WORKING_STAGE, LAVA_IDLE_STATE), 3);
            }
        }
    }

    private void handleMachineOperationTickRun() {
        ItemStack inputStack = inventory.getStackInSlot(INPUT_SLOT);
        ItemStack outputOne = inventory.getStackInSlot(OUTPUT_SLOT_1);
        ItemStack outputTwo = inventory.getStackInSlot(OUTPUT_SLOT_2);
        Fluid fluidType = fluidTank.getFluid().getFluid();

        SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
        var recipeOpt = fluidType == Fluids.WATER
                ? level.getRecipeManager().getRecipeFor(AllRecipeTypes.SPLASHING.getType(), recipeInput, level)
                : level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, recipeInput, level);

        boolean hasEnoughEnergy = energyStorage.getEnergyStored() >= 100; // FE required per tick.
        boolean hasInput = inputStack.getCount() > 0;
        boolean hasFluid = fluidTank.getFluidInTank(0).getAmount() > 0;

        if(recipeOpt.isPresent() && hasInput && hasEnoughEnergy && hasFluid) {
            var recipe = recipeOpt.get().value();

            if (recipe instanceof SplashingRecipe splashingRecipe) {
                var potentialResults = splashingRecipe.getRollableResultsAsItemStacks();

                if(potentialResults.isEmpty()) return;

                if(
                    outputOne.getCount() > 0 && outputOne.getItem() != potentialResults.get(0).getItem()
                    || outputTwo.getCount() > 0 && (potentialResults.size() < 2 || outputTwo.getItem() != potentialResults.get(1).getItem())
                ) return;

                handleMachineOperationRunning();

                if(progress >= 100) {
                    List<ItemStack> outputs = splashingRecipe.rollResults(this.level.getRandom());

                    for (ItemStack o : outputs) {
                        if (o.getItem() == potentialResults.get(0).getItem()) {
                            if (!outputOne.isEmpty()) {
                                outputOne.grow(o.getCount());
                            } else {
                                ItemStack newOutputOne = new ItemStack(o.getItem(), o.getCount());
                                this.inventory.insertItem(OUTPUT_SLOT_1, newOutputOne, false);
                            }
                        } else {
                            if (!outputTwo.isEmpty()) {
                                outputTwo.grow(o.getCount());
                            } else {
                                ItemStack newOutputTwo = new ItemStack(o.getItem(), o.getCount());
                                this.inventory.insertItem(OUTPUT_SLOT_2, newOutputTwo, false);
                            }
                        }
                    }

                    inputStack.shrink(1);

                    progress = 0;
                    this.setChangedAndUpdate();
                    ejectOutput();
                }

            }

            if (recipe instanceof BlastingRecipe blastingRecipe) {
                var result = blastingRecipe.getResultItem(this.level.registryAccess());
                if(outputOne.getCount() > 0 && outputOne.getItem() != result.getItem())
                    return;

                handleMachineOperationRunning();

                if(progress >= 100) {
                    inputStack.shrink(1);
                    if(outputOne.getItem() == result.getItem()) {
                        outputOne.grow(result.getCount());
                    } else {
                        ItemStack newOutputOne = new ItemStack(result.getItem(), result.getCount());
                        this.inventory.insertItem(OUTPUT_SLOT_1, newOutputOne, false);
                    }
                    progress = 0;
                    this.setChangedAndUpdate();
                }

            }

        } else {
            if(progress != 0) {
                this.setChangedAndUpdate();
                progress = 0;
            }

        }
    }

    private void ejectSlotToTarget(int slot, IItemHandler targetHandler) {
        ItemStack outputStack = inventory.getStackInSlot(slot);
        if (outputStack.isEmpty()) return;

        // Simulate pushing into the adjacent block (Pattern Provider / Pipe)
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetHandler, outputStack, true);

        int amountToExtract = outputStack.getCount() - remainder.getCount();
        if (amountToExtract > 0) {
            // Extract the pushed amount internally from your machine
            ItemStack extracted = inventory.extractItem(slot, amountToExtract, false);
            // Actually insert into the adjacent handler
            ItemHandlerHelper.insertItemStacked(targetHandler, extracted, false);

            this.updateFluidMenu = true; // Queue tick update
        }
    }

    private void ejectFluidToTarget(IFluidHandler targetHandler) {
        FluidStack internalFluid = fluidTank.getFluid();
        if (internalFluid.isEmpty()) return;

        // Simulate draining from tank and inserting into target handler
        FluidStack drainSimulated = fluidTank.drain(internalFluid, IFluidHandler.FluidAction.SIMULATE);
        if (drainSimulated.isEmpty()) return;

        int filledAmount = targetHandler.fill(drainSimulated, IFluidHandler.FluidAction.EXECUTE);

        if (filledAmount > 0) {
            // Drain the actual inserted amount from internal tank
            fluidTank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);
            this.updateFluidMenu = true; // Queue tick update for network & sync
        }
    }

    // This will help a bunch with AE2 integrations.
    private void ejectOutput() {
        if (this.level == null || this.level.isClientSide()
                || (inventory.getStackInSlot(OUTPUT_SLOT_1).getCount() == 0 && inventory.getStackInSlot(OUTPUT_SLOT_2).getCount() == 0))
            return;

        // Check all adjacent blocks (or specifically the sides you want)
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);

            // Query if the adjacent block has an ItemHandler capability (e.g., AE2 Pattern Provider or Pipe)
            IItemHandler targetHandler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    adjacentPos,
                    direction.getOpposite()
            );

            if (targetHandler != null) {
                // Eject Output Slot 1
                ejectSlotToTarget(OUTPUT_SLOT_1, targetHandler);
                // Eject Output Slot 2
                ejectSlotToTarget(OUTPUT_SLOT_2, targetHandler);
                // Eject the fluid
                if (autoEjectFluid && !fluidTank.isEmpty()) {
                    IFluidHandler targetFluidHandler = level.getCapability(
                            Capabilities.FluidHandler.BLOCK,
                            adjacentPos,
                            direction.getOpposite()
                    );

                    if (targetFluidHandler != null) {
                        ejectFluidToTarget(targetFluidHandler);
                    }
                }
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidiserBlockEntity blockEntity) {
        // handle redstone -> power conversion logic.
        blockEntity.redstoneForPowerTickRun();
        // handle machine sounds
        blockEntity.handleMachineSoundsAndTextureChange(pos, state);

        // run the machine
        blockEntity.handleMachineOperationTickRun();

        if(blockEntity.updateFluidMenu) {
            blockEntity.setChangedAndUpdate();
            blockEntity.updateFluidMenu = false;
        }

        blockEntity.ejectOutput(); // eject output if still exists after the tick has run.

    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Fluidiser"); // The title displayed at the top of the GUI
    }

    public boolean isRunning() {
        return this.progress > 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(
                io.netty.buffer.Unpooled.buffer(),
                playerInventory.player.registryAccess()
        );

        FriendlyByteBuf.writeBlockPos(extraData, this.getBlockPos());

        return new FluidiserMenu(containerId, playerInventory, extraData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.inventory.serializeNBT(registries));
        tag.putInt("Energy", this.energyStorage.getEnergyStored());
        tag.put("FluidTank", this.fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putBoolean("AutoEjectFluid", this.autoEjectFluid);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            this.inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }

        if (tag.contains("Energy")) {
            this.energyStorage.setEnergyStored(tag.getInt("Energy"));
        }

        if (tag.contains("FluidTank")) {
            this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        }

        if (tag.contains("AutoEjectFluid")) {
            this.autoEjectFluid = tag.getBoolean("AutoEjectFluid");
        }
    }

    public void setChangedAndUpdate() {
        setChanged();
        if (this.level != null) {

            if (!this.level.isClientSide) {
                this.level.getChunkAt(this.worldPosition).setUnsaved(true); // mark chunk as modified.
            }

            // Send a block update packet from server to all nearby clients
            this.level.sendBlockUpdated(
                    this.worldPosition,
                    this.getBlockState(),
                    this.getBlockState(),
                    Block.UPDATE_ALL
            );

        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries); // Re-uses your exact NBT saving logic for network transmission
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            this.loadAdditional(pkt.getTag(), registries); // Re-uses your exact NBT loading logic on the client
        }
    }



}