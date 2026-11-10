package com.sololeveling;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class Net {

    /** Servidor -> cliente: estado del jugador. */
    public record SyncPayload(int[] data) implements CustomPayload {
        public static final CustomPayload.Id<SyncPayload> ID =
                new CustomPayload.Id<>(Identifier.of(SoloLeveling.MOD, "sync"));
        public static final PacketCodec<RegistryByteBuf, SyncPayload> CODEC = PacketCodec.of(
                (value, buf) -> buf.writeIntArray(value.data()),
                buf -> new SyncPayload(buf.readIntArray()));

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Cliente -> servidor: acción (habilidad o reparto de puntos). */
    public record ActionPayload(int action) implements CustomPayload {
        public static final CustomPayload.Id<ActionPayload> ID =
                new CustomPayload.Id<>(Identifier.of(SoloLeveling.MOD, "action"));
        public static final PacketCodec<RegistryByteBuf, ActionPayload> CODEC = PacketCodec.of(
                (value, buf) -> buf.writeVarInt(value.action()),
                buf -> new ActionPayload(buf.readVarInt()));

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ActionPayload.ID, ActionPayload.CODEC);
    }

    private Net() {}
}
