package com.penguin.nuclide;

import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.model.TankModelLoading;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public final class NuclideClient implements ClientModInitializer {
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

    }
}