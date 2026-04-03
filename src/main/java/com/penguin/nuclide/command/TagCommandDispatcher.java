package com.penguin.nuclide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDefinition;
import com.penguin.nuclide.tag.SpeciesTagRegistry;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class TagCommandDispatcher {

    private TagCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("tag")
                        .then(literal("fromtag")
                            .then(argument("id", IdentifierArgumentType.identifier())
                                    .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");

                                            SpeciesTagDefinition tag = SpeciesTagDataLoader.TAGS.getById(id.toString());

                                            if (tag != null) {
                                                context.getSource().sendFeedback(
                                                        () -> Text.of(
                                                                "Tag found: " +
                                                                tag.id() +
                                                                " | size=" + tag.size() +
                                                                " | values=" + tag.values()
                                                        ),
                                                        false
                                                );
                                                return 1;
                                            } else {
                                                context.getSource().sendError(Text.of("Tag not found: " + id));
                                                return 0;
                                            }
                                        }
                                    )
                            )

                        )
            );
    }
}