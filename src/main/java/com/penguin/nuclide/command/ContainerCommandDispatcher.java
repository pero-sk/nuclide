package com.penguin.nuclide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.data.NuclideDataLoader;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.penguin.nuclide.Nuclide;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ContainerCommandDispatcher {

    private ContainerCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("nuclide")
                .then(literal("container")
                    .then(literal("addto")
                        .then(argument("speciesId", IdentifierArgumentType.identifier())
                            .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    Identifier speciesId = IdentifierArgumentType.getIdentifier(context, "speciesId");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(speciesId.toString());

                                    SpeciesStack speciesStack = new SpeciesStack(speciesId.toString(), amount);

                                    if (species == null) {
                                        context.getSource().sendError(Text.of("Species not found: " + speciesId));
                                        return 0;
                                    }

                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    ItemStack stack = player.getMainHandStack();

                                    SpeciesContainer existing = stack.getOrDefault(
                                        NuclideDataComponents.SPECIES_CONTAINER,
                                        new SpeciesContainer()
                                    );

                                    SpeciesContainer updated = existing.copy();
                                    updated.add(speciesStack.key(), amount);

                                    stack.set(NuclideDataComponents.SPECIES_CONTAINER, updated);

                                    Nuclide.LOGGER.info(
                                        "Added to container: " + speciesId +
                                        " \n| amount=" + amount +
                                        " | container=" + updated
                                    );

                                    context.getSource().sendFeedback(
                                        () -> Text.of("Added to container: " + species.name() + " | amount=" + amount + " mmol"),
                                        false
                                    );

                                    return 1;
                                })
                            )
                        )
                    )
                    .then(literal("removefrom")
                        .then(argument("speciesId", IdentifierArgumentType.identifier())
                            .then(argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    Identifier speciesId = IdentifierArgumentType.getIdentifier(context, "speciesId");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(speciesId.toString());

                                    SpeciesStack speciesStack = new SpeciesStack(speciesId.toString(), amount);

                                    if (species == null) {
                                        context.getSource().sendError(Text.of("Species not found: " + speciesId));
                                        return 0;
                                    }

                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    ItemStack stack = player.getMainHandStack();

                                    SpeciesContainer existing = stack.getOrDefault(
                                        NuclideDataComponents.SPECIES_CONTAINER,
                                        new SpeciesContainer()
                                    );

                                    SpeciesContainer updated = existing.copy();
                                    updated.remove(speciesStack.key(), amount);

                                    stack.set(NuclideDataComponents.SPECIES_CONTAINER, updated);

                                    Nuclide.LOGGER.info(
                                        "Removed from container: " + speciesId +
                                        " \n| amount=" + amount +
                                        " \n| container=" + updated
                                    );

                                    context.getSource().sendFeedback(
                                        () -> Text.of("Removed from container: " + species.name() + " | amount=" + amount + " mmol"),
                                        false
                                    );

                                    return 1;
                                })
                            )
                        )
                    )
                        
        ));
    }
}