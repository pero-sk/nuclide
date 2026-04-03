package com.penguin.nuclide;

import com.penguin.nuclide.command.ReactionCommandDispatcher;
import com.penguin.nuclide.command.SpeciesCommandDispatcher;
import com.penguin.nuclide.command.TagCommandDispatcher;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.content.items.CanisterItem;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nuclide implements ModInitializer {
    public static final String MOD_ID = "nuclide";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CanisterItem CANISTER_ITEM =
            new CanisterItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        NuclideDataComponents.init();

        NuclideDataLoader.register();
        SpeciesTagDataLoader.register();
        ReactionDataLoader.register();

        Registry.register(Registries.ITEM, asIdentifier("canister"), CANISTER_ITEM);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReactionCommandDispatcher.register(dispatcher);
            TagCommandDispatcher.register(dispatcher);
            SpeciesCommandDispatcher.register(dispatcher);
        });

        LOGGER.info("Nuclide initialized");
    }

    public static Identifier asIdentifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}