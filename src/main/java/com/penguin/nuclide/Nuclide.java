package com.penguin.nuclide;

import com.penguin.nuclide.command.ReactionCommandDispatcher;
import com.penguin.nuclide.command.SpeciesCommandDispatcher;
import com.penguin.nuclide.command.TagCommandDispatcher;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nuclide implements ModInitializer {
    public static final String MOD_ID = "nuclide";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        NuclideDataLoader.register();
        SpeciesTagDataLoader.register();
        ReactionDataLoader.register();


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReactionCommandDispatcher.register(dispatcher);
            TagCommandDispatcher.register(dispatcher);
            SpeciesCommandDispatcher.register(dispatcher);
        });

        LOGGER.info("Nuclide initialized");
    }
}