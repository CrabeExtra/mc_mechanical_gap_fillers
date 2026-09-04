package mods.mechanicalgapfillers.client;

import mods.mechanicalgapfillers.MechanicalGapFillers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AutoEjectFluidPayload(BlockPos pos, boolean enabled) implements CustomPacketPayload {
    public static final Type<AutoEjectFluidPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MechanicalGapFillers.MODID, "set_auto_eject_fluid"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AutoEjectFluidPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AutoEjectFluidPayload::pos,
            ByteBufCodecs.BOOL, AutoEjectFluidPayload::enabled,
            AutoEjectFluidPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}