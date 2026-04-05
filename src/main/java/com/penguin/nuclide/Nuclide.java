package com.penguin.nuclide;

import com.penguin.nuclide.command.ContainerCommandDispatcher;
import com.penguin.nuclide.command.ReactionCommandDispatcher;
import com.penguin.nuclide.command.SpeciesCommandDispatcher;
import com.penguin.nuclide.command.TagCommandDispatcher;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.gas.GasRegistry;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.content.registry.ModItems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nuclide implements ModInitializer {
    public static final String MOD_ID = "nuclide";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // being realistic, 1 minecraft block = 1 cubic meter, 
    // which at that scale would be around 44,600 mmols of gas at standard temperature and pressure.
    // which would just be horrible UX for users,
    // so we'll use a 1/10th-ish scale of 4,500 mmols per block for gameplay purposes.
    public static int MMOL_PER_BLOCK = 4500;

    public static final GasRegistry GASES = new GasRegistry();

    @Override
    public void onInitialize() {
        NuclideDataComponents.init();

        NuclideDataLoader.register();

        ModBlocks.register();
        ModBlockEntities.init();
        ModItems.register();

        SpeciesTagDataLoader.register();
        ReactionDataLoader.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReactionCommandDispatcher.register(dispatcher);
            TagCommandDispatcher.register(dispatcher);
            SpeciesCommandDispatcher.register(dispatcher);
            ContainerCommandDispatcher.register(dispatcher);
        });

        LOGGER.info("Nuclide initialized");
    }

    public static Identifier asIdentifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}