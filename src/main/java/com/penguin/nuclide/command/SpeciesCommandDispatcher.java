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
                literal("nuclide")
                .then(literal("species")
                        .then(argument("id", IdentifierArgumentType.identifier())
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");

                                    SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(id.toString());

                                    if (species != null) {

                                        String SolidRep = species.representation().solid() != null
                                            ? species.representation().solid().block().toString().trim()
                                            : "none";
                                        
                                        String LiquidRep = species.representation().liquid() != null
                                            ? species.representation().liquid().block().toString().trim()
                                            : "none";
                                        
                                        String GasRep = species.representation().gas() != null
                                            ? species.representation().gas().block().toString().trim()
                                            : "none";

                                        context.getSource().sendFeedback(
                                                () -> Text.of(
                                                        "Species found: " +
                                                        species.id() +
                                                        " \n| name=" + species.name() +
                                                        " \n| raw=" + species.rawNowns() +
                                                        " \n| normalized=" + species.normalizedNowns() +
                                                        " \n| kind=" + species.kind() +
                                                        " \n| solid.representation=" + SolidRep +
                                                        " \n| liquid.representation=" + LiquidRep +
                                                        " \n| gas.representation=" + GasRep
                                                ),
                                                false
                                        );
                                        return 1;
                                    } else {
                                        context.getSource().sendError(Text.of("Species not found: " + id));
                                        return 0;
                                    }
                                }))
        ));
    }
}