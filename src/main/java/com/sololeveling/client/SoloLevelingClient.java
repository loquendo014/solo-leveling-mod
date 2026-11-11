package com.sololeveling.client;

import com.sololeveling.Net;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SoloLevelingClient implements ClientModInitializer {

    public static KeyBinding STATUS;
    public static KeyBinding SHOP;
    public static final KeyBinding[] SKILLS = new KeyBinding[7];
    private static final int[] DEFAULT_KEYS = {GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_H, GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_R,
            GLFW.GLFW_KEY_Y, GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_N};

    @Override
    public void onInitializeClient() {
        STATUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sololeveling.status",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.sololeveling"));
        SHOP = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sololeveling.shop",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.sololeveling"));
        for (int i = 0; i < 7; i++) {
            SKILLS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sololeveling.skill" + (i + 1),
                    InputUtil.Type.KEYSYM, DEFAULT_KEYS[i], "category.sololeveling"));
        }

        ClientPlayNetworking.registerGlobalReceiver(Net.SyncPayload.ID, (payload, context) -> {
            int[] data = payload.data();
            context.client().execute(() -> ClientData.data = data);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (STATUS.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new StatusScreen());
            }
            while (SHOP.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ShopScreen());
            }
            for (int i = 0; i < 7; i++) {
                while (SKILLS[i].wasPressed()) {
                    if (ClientPlayNetworking.canSend(Net.ActionPayload.ID)) {
                        ClientPlayNetworking.send(new Net.ActionPayload(i + 1));
                    }
                }
            }
        });

        ModelDefs.registerAll();
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> Hud.render(ctx));
    }
}
