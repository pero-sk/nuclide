package com.penguin.nuclide.model;

import com.penguin.nuclide.Nuclide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ModModelLoading {
    public static final Identifier TANK_SHELL_MODEL_ID = Nuclide.asIdentifier("block/species_tank_shell");

    public static final ModelIdentifier PIPE_BLOCK_TOPLEVEL_ID =
            new ModelIdentifier(Nuclide.asIdentifier("block/pipe"), "");

    public static final ModelIdentifier PIPE_ITEM_TOPLEVEL_ID =
            new ModelIdentifier(Nuclide.asIdentifier("item/pipe"), "");

    public static final Identifier PIPE_BLOCK_ID = Nuclide.asIdentifier("block/pipe");
    
    public static final Identifier PIPE_CORE_MODEL_ID =
            Nuclide.asIdentifier("block/pipe_core");

    public static final Identifier PIPE_ARM_MODEL_ID =
            Nuclide.asIdentifier("block/pipe_arm");

    private ModModelLoading() {}

    public static void register() {
        ModelLoadingPlugin.register(ctx -> {
            ctx.modifyModelAfterBake().register((model, context) -> {
                Identifier resourceId = context.resourceId();
                ModelIdentifier topLevelId = context.topLevelId();

                if (TANK_SHELL_MODEL_ID.equals(resourceId)) {
                    System.out.println("resourceId=" + resourceId + " topLevelId=" + topLevelId);
                    return new TankShellModel();
                }

                if (PIPE_BLOCK_TOPLEVEL_ID.equals(topLevelId)
                    || PIPE_ITEM_TOPLEVEL_ID.equals(topLevelId)
                    || PIPE_BLOCK_ID.equals(resourceId)) {
                        
                    System.out.println("resourceId=" + resourceId + " topLevelId=" + topLevelId);

                    return new PipeModel();
                }

                return model;
            });
        });
    }
}