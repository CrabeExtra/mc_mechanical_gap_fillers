package mods.mechanicalgapfillers.utility.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public class MGFEnergyStorage extends EnergyStorage {

    public MGFEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract, 0);
    }

    public void setEnergyStored(int amount) {
        this.energy = Math.clamp(amount, 0, this.capacity);
    }
}
