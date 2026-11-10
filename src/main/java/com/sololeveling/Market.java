package com.sololeveling;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Tienda del Sistema: compra con oro, vende piedras. */
public final class Market {

    public static void give(ServerPlayerEntity p, String id, int n) {
        Item it = ModItems.ALL.get(id);
        if (it != null) p.getInventory().offerOrDrop(new ItemStack(it, n));
    }

    public static void buy(ServerPlayerEntity p, int i) {
        PlayerData d = DataState.of(p);
        if (d.level < S.SHOP_LEVEL[i]) {
            Fx.bar(p, Text.literal("Requiere nivel " + S.SHOP_LEVEL[i]).formatted(Formatting.RED));
            return;
        }
        if (d.gold < S.SHOP_PRICE[i]) {
            Fx.bar(p, Text.literal("Oro insuficiente").formatted(Formatting.RED));
            Fx.sound(p, SoundEvents.ENTITY_VILLAGER_NO, 0.5f, 1.2f);
            return;
        }
        d.gold -= S.SHOP_PRICE[i];
        give(p, S.SHOP_ID[i], 1);
        Fx.sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 0.8f);
        Fx.bar(p, Text.literal("Comprado: " + S.SHOP_NAME[i]).formatted(Formatting.GREEN));
    }

    public static void sellAll(ServerPlayerEntity p) {
        PlayerInventory inv = p.getInventory();
        int gold = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack st = inv.getStack(i);
            if (st.isOf(ModItems.MANA_STONE)) gold += st.getCount() * 20;
            else if (st.isOf(ModItems.ESSENCE_STONE)) gold += st.getCount() * 250;
            else continue;
            inv.setStack(i, ItemStack.EMPTY);
        }
        if (gold == 0) {
            Fx.bar(p, Text.literal("No tienes piedras que vender.").formatted(Formatting.GRAY));
            return;
        }
        DataState.of(p).gold += gold;
        Fx.sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.2f);
        Fx.bar(p, Text.literal("Vendido por " + gold + " de oro").formatted(Formatting.GOLD));
    }

    private Market() {}
}
