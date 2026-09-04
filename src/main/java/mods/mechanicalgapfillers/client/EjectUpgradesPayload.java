package mods.mechanicalgapfillers.client;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import mods.mechanicalgapfillers.blocks.FluidiserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EjectUpgradesPayload() implements CustomPacketPayload {
    public static final Type<EjectUpgradesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MechanicalGapFillers.MODID, "eject_upgrades"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EjectUpgradesPayload> STREAM_CODEC =
            StreamCodec.unit(new EjectUpgradesPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EjectUpgradesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Check if the player has your machine menu open
                if (player.containerMenu instanceof FluidiserMenu menu) {
                    menu.getBlockEntity().removeUpgrades();
                }
            }
        });
    }
}

