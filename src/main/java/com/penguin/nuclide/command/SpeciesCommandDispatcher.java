package com.penguin.nuclide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.data.NuclideDataLoader;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SpeciesCommandDispatcher {

    private SpeciesCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("species")
                        .then(argument("id", IdentifierArgumentType.identifier())
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");

                                    SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(id.toString());

                                    if (species != null) {
                                        context.getSource().sendFeedback(
                                                () -> Text.of(
                                                        "Species found: " +
                                                        species.id() +
                                                        " | name=" + species.name() +
                                                        " | raw=" + species.rawNowns() +
                                                        " | normalized=" + species.normalizedNowns() +
                                                        " | kind=" + species.kind()
                                                ),
                                                false
                                        );
                                        return 1;
                                    } else {
                                        context.getSource().sendError(Text.of("Species not found: " + id));
                                        return 0;
                                    }
                                }))
        );
    }
}