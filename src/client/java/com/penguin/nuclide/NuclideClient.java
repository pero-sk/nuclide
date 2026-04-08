package com.penguin.nuclide;

import org.lwjgl.glfw.GLFW;

import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.model.TankModelLoading;
import com.penguin.nuclide.screen.MoleculeDebugScreen;
import com.penguin.nuclide.screen.SpeciesFilterScreen;

import com.penguin.nuclide.content.items.SpeciesFilterItem;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;

public final class NuclideClient implements ClientModInitializer {

    private static KeyBinding openMoleculeDebugScreen;

    @Override
    public void onInitializeClient() {
        TankModelLoading.register();

        BlockRenderLayerMap.INSTANCE.putBlock(
                ModBlocks.SPECIES_TANK_CASING,
                RenderLayer.getTranslucent()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                ModBlocks.SPECIES_TANK_CONTROLLER,
                RenderLayer.getTranslucent()
        );

        openMoleculeDebugScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nuclide.open_molecule_debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.nuclide.debug"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMoleculeDebugScreen.wasPressed()) {
                client.setScreen(new MoleculeDebugScreen());
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.getItem() instanceof SpeciesFilterItem) {
                openSpeciesFilterScreen(hand);
                return net.minecraft.util.TypedActionResult.success(stack, true);
            }

            return net.minecraft.util.TypedActionResult.pass(stack);
        });

    }

    public static void openSpeciesFilterScreen(Hand hand) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.setScreen(new SpeciesFilterScreen(hand));
    }
}