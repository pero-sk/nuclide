package com.penguin.nuclide.model;

import com.penguin.nuclide.Nuclide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class TankModelLoading {
    public static final Identifier MODEL_ID = Nuclide.asIdentifier("block/species_tank_shell");

    private TankModelLoading() {}

    public static void register() {
        ModelLoadingPlugin.register(ctx -> {
            ctx.modifyModelAfterBake().register((model, context) -> {
                Identifier resourceId = context.resourceId();

                if (MODEL_ID.equals(resourceId)) {
                    return new TankShellModel();
                }

                return model;
            });
        });
    }
}