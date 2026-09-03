package mods.mechanicalgapfillers.sounds;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FluidiserSounds {
    // NeoForge sound register
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MechanicalGapFillers.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RUNNING_SOUND = SOUND_EVENTS.register(
            "fluidiser",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MechanicalGapFillers.MODID, "fluidiser"))
    );
}
