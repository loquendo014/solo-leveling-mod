package com.sololeveling;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final Map<String, Item> ALL = new LinkedHashMap<>();
    public static Item MANA_STONE, ESSENCE_STONE, HEALTH_POTION, MANA_POTION, ELIXIR, RETURN_STONE, GATE_KEY,
            RANDOM_BOX, POWER_FRAGMENT, DEMON_DAGGER;

    private static Item reg(String id, Item item) {
        Registry.register(Registries.ITEM, Identifier.of(SoloLeveling.MOD, id), item);
        ALL.put(id, item);
        return item;
    }

    public static void init() {
        MANA_STONE = reg("mana_stone", new Tip(new Item.Settings(), "Véndela en la Tienda [U] o al Mercader."));
        ESSENCE_STONE = reg("essence_stone", new Tip(new Item.Settings().rarity(Rarity.RARE), "Núcleo de un jefe. Muy valiosa."));
        HEALTH_POTION = reg("health_potion", new Use(0, 16, "Restaura el 50% de tu vida."));
        MANA_POTION = reg("mana_potion", new Use(1, 16, "Restaura el 50% de tu maná."));
        ELIXIR = reg("elixir", new Use(2, 4, "Cura por completo, limpia efectos y da absorción."));
        RETURN_STONE = reg("return_stone", new Use(3, 8, "Te devuelve a tu punto de reaparición."));
        GATE_KEY = reg("gate_key", new Use(4, 8, "Abre una Puerta cerca de ti (Overworld)."));
        RANDOM_BOX = reg("random_box", new Use(5, 16, "¿Bendita o maldita? Ábrela."));
        POWER_FRAGMENT = reg("power_fragment", new Use(6, 64, "+1 punto de estadística."));
        reg("kasaka_fang", new Dagger(0, 3, "Veneno paralizante."));
        reg("knight_killer", new Dagger(1, 5, "Debilita al enemigo."));
        reg("baruka_dagger", new Dagger(2, 7, "Hielo: ralentiza mucho."));
        DEMON_DAGGER = reg("demon_king_dagger", new Dagger(3, 10, "Marchita y quema."));
        reg("kamish_wrath", new Dagger(4, 16, "La daga más poderosa. Marchita y te regenera."));

        Registry.register(Registries.ITEM_GROUP, Identifier.of(SoloLeveling.MOD, "main"), FabricItemGroup.builder()
                .icon(() -> new ItemStack(DEMON_DAGGER))
                .displayName(Text.literal("Solo Leveling"))
                .entries((ctx, entries) -> ALL.values().forEach(i -> entries.add(i)))
                .build());
    }

    static class Tip extends Item {
        final String tip;

        Tip(Settings s, String tip) {
            super(s);
            this.tip = tip;
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.literal(tip).formatted(Formatting.GRAY));
        }
    }

    static class Dagger extends SwordItem {
        final int kind;
        final String tip;

        Dagger(int kind, int dmg, String tip) {
            super(ToolMaterials.NETHERITE, new Item.Settings().rarity(kind >= 3 ? Rarity.EPIC : Rarity.RARE).fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, dmg, -1.6f)));
            this.kind = kind;
            this.tip = tip;
        }

        @Override
        public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            switch (kind) {
                case 0 -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                }
                case 1 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
                case 2 -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2));
                    Fx.part(target, ParticleTypes.SNOWFLAKE, 1, 10, 0.3, 0.02);
                }
                case 3 -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
                    target.setOnFireFor(4);
                }
                default -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 2));
                    attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 1));
                    Fx.part(target, ParticleTypes.SCULK_SOUL, 1, 8, 0.3, 0.02);
                }
            }
            return super.postHit(stack, target, attacker);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.literal(tip).formatted(Formatting.LIGHT_PURPLE));
        }
    }

    static class Use extends Tip {
        final int kind;

        Use(int kind, int max, String tip) {
            super(new Item.Settings().maxCount(max), tip);
            this.kind = kind;
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack);
            PlayerData d = DataState.of(p);
            boolean used = true;
            switch (kind) {
                case 0 -> {
                    p.heal(p.getMaxHealth() * 0.5f);
                    Fx.part(p, ParticleTypes.HEART, 1, 6, 0.4, 0);
                }
                case 1 -> d.mana = Math.min(d.manaMax(), d.mana + d.manaMax() / 2);
                case 2 -> {
                    p.clearStatusEffects();
                    p.setHealth(p.getMaxHealth());
                    d.mana = d.manaMax();
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 1200, 2));
                    Fx.part(p, ParticleTypes.TOTEM_OF_UNDYING, 1, 40, 0.5, 0.3);
                }
                case 3 -> {
                    ServerWorld w = p.getServer().getWorld(p.getSpawnPointDimension());
                    BlockPos pos = p.getSpawnPointPosition();
                    if (w == null || pos == null) {
                        w = p.getServer().getOverworld();
                        pos = w.getSpawnPos();
                    }
                    p.teleport(w, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, p.getYaw(), p.getPitch());
                    Fx.part(p, ParticleTypes.REVERSE_PORTAL, 1, 60, 0.5, 0.3);
                }
                case 4 -> {
                    used = Gates.trySpawn(p.getServer(), p);
                    if (!used) Fx.bar(p, Text.literal("La llave solo funciona en el Overworld.").formatted(Formatting.RED));
                }
                case 5 -> {
                    int r = p.getRandom().nextInt(100);
                    if (r < 40) {
                        int g = 50 + p.getRandom().nextInt(201);
                        d.gold += g;
                        Fx.msg(p, "Caja Aleatoria: +" + g + " de oro.");
                    } else if (r < 65) Market.give(p, "mana_stone", 2 + p.getRandom().nextInt(4));
                    else if (r < 80) Market.give(p, "health_potion", 2);
                    else if (r < 90) Market.give(p, "mana_potion", 2);
                    else if (r < 97) Market.give(p, "gate_key", 1);
                    else {
                        Market.give(p, "power_fragment", 1);
                        Fx.msg(p, "¡Caja bendita! Has obtenido un Fragmento de Poder.");
                    }
                }
                default -> {
                    d.points++;
                    Fx.msg(p, "+1 punto de estadística.");
                }
            }
            if (used) {
                if (!p.getAbilities().creativeMode) stack.decrement(1);
                p.getItemCooldownManager().set(this, 20);
                Fx.sound(p, SoundEvents.ENTITY_GENERIC_DRINK, 0.7f, 1.2f);
                Leveling.sync(p);
            }
            return TypedActionResult.success(stack);
        }
    }

    private ModItems() {}
}
