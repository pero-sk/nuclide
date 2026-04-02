package com.penguin.nuclide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.penguin.nuclide.data.MoleculeDefinition;
import com.penguin.nuclide.data.NuclideDataLoader;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MoleculeCommandDispatcher {

    private MoleculeCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("molecule")
                        .then(argument("id", IdentifierArgumentType.identifier())
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");

                                    MoleculeDefinition molecule = NuclideDataLoader.MOLECULES.getById(id.toString());

                                    if (molecule != null) {
                                        context.getSource().sendFeedback(
                                                () -> Text.of(
                                                        "Molecule found: " +
                                                        molecule.id() +
                                                        " | name=" + molecule.name() +
                                                        " | raw=" + molecule.rawNowns() +
                                                        " | normalized=" + molecule.normalizedNowns()
                                                ),
                                                false
                                        );
                                        return 1;
                                    } else {
                                        context.getSource().sendError(Text.of("Molecule not found: " + id));
                                        return 0;
                                    }
                                }))
        );
    }
}