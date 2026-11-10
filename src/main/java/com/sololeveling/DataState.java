package com.sololeveling;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataState extends PersistentState {
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public static final PersistentState.Type<DataState> TYPE =
            new PersistentState.Type<>(DataState::new, DataState::fromNbt, null);

    public static DataState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "sololeveling");
    }

    public static PlayerData of(PlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return new PlayerData();
        DataState st = get(server);
        st.markDirty();
        return st.players.computeIfAbsent(p.getUuid(), k -> new PlayerData());
    }

    public static DataState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        DataState st = new DataState();
        for (String key : nbt.getKeys()) {
            try {
                st.players.put(UUID.fromString(key), PlayerData.fromArray(nbt.getIntArray(key)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return st;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        players.forEach((uuid, d) -> nbt.putIntArray(uuid.toString(), d.toArray()));
        return nbt;
    }
}
