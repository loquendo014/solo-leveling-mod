package com.sololeveling;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Commands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess access,
                                CommandManager.RegistrationEnvironment env) {
        dispatcher.register(CommandManager.literal("sistema")
                .executes(ctx -> {
                    Leveling.printStatus(ctx.getSource().getPlayerOrThrow());
                    return 1;
                })
                .then(CommandManager.literal("admin")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("xp")
                                .then(CommandManager.argument("cantidad", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                            Leveling.addXp(p, IntegerArgumentType.getInteger(ctx, "cantidad"));
                                            Leveling.sync(p);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("puntos")
                                .then(CommandManager.argument("cantidad", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                            DataState.of(p).points += IntegerArgumentType.getInteger(ctx, "cantidad");
                                            Leveling.sync(p);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("puerta")
                                .executes(ctx -> {
                                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                    if (!Gates.trySpawn(ctx.getSource().getServer(), p)) {
                                        ctx.getSource().sendError(Text.literal("Solo puede aparecer una Puerta en el Overworld."));
                                    }
                                    return 1;
                                }))
                        .then(CommandManager.literal("puertas")
                                .then(CommandManager.literal("activar").executes(ctx -> {
                                    Gates.enabled = true;
                                    ctx.getSource().sendFeedback(() -> Text.literal("Puertas activadas.").formatted(Formatting.GREEN), false);
                                    return 1;
                                }))
                                .then(CommandManager.literal("desactivar").executes(ctx -> {
                                    Gates.enabled = false;
                                    Gates.close(ctx.getSource().getServer());
                                    ctx.getSource().sendFeedback(() -> Text.literal("Puertas desactivadas.").formatted(Formatting.RED), false);
                                    return 1;
                                }))
                                .then(CommandManager.literal("intervalo")
                                        .then(CommandManager.argument("minutos", IntegerArgumentType.integer(1, 240))
                                                .executes(ctx -> {
                                                    Gates.intervalSeconds = IntegerArgumentType.getInteger(ctx, "minutos") * 60;
                                                    ctx.getSource().sendFeedback(() -> Text.literal("Intervalo de Puertas actualizado.").formatted(Formatting.GREEN), false);
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("reiniciar")
                                .executes(ctx -> {
                                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                    PlayerData d = DataState.of(p);
                                    PlayerData fresh = new PlayerData();
                                    d.level = 1; d.xp = 0; d.points = 0;
                                    d.str = d.vit = d.agi = d.per = d.intel = 10;
                                    d.mana = fresh.mana;
                                    Leveling.applyStats(p);
                                    Leveling.sync(p);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Progreso reiniciado.").formatted(Formatting.YELLOW), false);
                                    return 1;
                                }))));
    }

    private Commands() {}
}
