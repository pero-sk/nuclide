package com.penguin.nuclide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionExecutor;
import com.penguin.nuclide.reaction.ReactionMatcher;
import com.penguin.nuclide.reaction.ReactionParticipant;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ReactionCommandDispatcher {

    private ReactionCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("reaction")
                        .then(literal("find")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
                                            ReactionDefinition reaction = ReactionDataLoader.REACTIONS.getById(id.toString());

                                            if (reaction == null) {
                                                context.getSource().sendError(Text.of("Reaction not found: " + id));
                                                return 0;
                                            }

                                            context.getSource().sendFeedback(
                                                    () -> Text.of(
                                                            "Reaction found: " +
                                                                    reaction.id() +
                                                                    " | name=" + reaction.name() +
                                                                    " | inputs=" + reaction.inputs().size() +
                                                                    " | outputs=" + reaction.outputs().size() +
                                                                    " | duration=" + reaction.durationTicks() +
                                                                    " | conditions=" + reaction.conditions()
                                                    ),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.of("Inputs: " + formatParticipants(reaction.inputs())),
                                                    false
                                            );
                                            context.getSource().sendFeedback(
                                                    () -> Text.of("Outputs: " + formatParticipants(reaction.outputs())),
                                                    false
                                            );

                                            return 1;
                                        })))
                        .then(literal("match")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .then(argument("inventory", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
                                                    String inventoryText = StringArgumentType.getString(context, "inventory");

                                                    ReactionDefinition reaction = ReactionDataLoader.REACTIONS.getById(id.toString());
                                                    if (reaction == null) {
                                                        context.getSource().sendError(Text.of("Reaction not found: " + id));
                                                        return 0;
                                                    }

                                                    Map<String, Integer> inventory;
                                                    try {
                                                        inventory = parseInventory(inventoryText);
                                                    } catch (IllegalArgumentException e) {
                                                        context.getSource().sendError(Text.of(e.getMessage()));
                                                        return 0;
                                                    }

                                                    boolean matches = ReactionMatcher.matches(reaction, inventory);

                                                    if (matches) {
                                                        context.getSource().sendFeedback(
                                                                () -> Text.of("Reaction matches: true"),
                                                                false
                                                        );
                                                        return 1;
                                                    } else {
                                                        context.getSource().sendError(Text.of(
                                                                "Reaction matches: false | missing or insufficient: " +
                                                                        describeMissingRequirements(reaction, inventory)
                                                        ));
                                                        return 0;
                                                    }
                                                }))))
                        .then(literal("run")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .then(argument("inventory", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
                                                    String inventoryText = StringArgumentType.getString(context, "inventory");

                                                    ReactionDefinition reaction = ReactionDataLoader.REACTIONS.getById(id.toString());
                                                    if (reaction == null) {
                                                        context.getSource().sendError(Text.of("Reaction not found: " + id));
                                                        return 0;
                                                    }

                                                    Map<String, Integer> inventory;
                                                    try {
                                                        inventory = parseInventory(inventoryText);
                                                    } catch (IllegalArgumentException e) {
                                                        context.getSource().sendError(Text.of(e.getMessage()));
                                                        return 0;
                                                    }

                                                    if (!ReactionMatcher.matches(reaction, inventory)) {
                                                        context.getSource().sendError(Text.of(
                                                                "Reaction cannot be executed: missing or insufficient: " +
                                                                        describeMissingRequirements(reaction, inventory)
                                                        ));
                                                        return 0;
                                                    }

                                                    Map<String, Integer> result = ReactionExecutor.execute(reaction, inventory);

                                                    context.getSource().sendFeedback(
                                                            () -> Text.of("Reaction executed: " + reaction.id()),
                                                            false
                                                    );
                                                    context.getSource().sendFeedback(
                                                            () -> Text.of("Consumed: " + formatParticipants(reaction.inputs())),
                                                            false
                                                    );
                                                    context.getSource().sendFeedback(
                                                            () -> Text.of("Produced: " + formatParticipants(reaction.outputs())),
                                                            false
                                                    );
                                                    context.getSource().sendFeedback(
                                                            () -> Text.of("Result inventory: " + formatInventory(result)),
                                                            false
                                                    );

                                                    return 1;
                                                }))))
        );
    }

    private static Map<String, Integer> parseInventory(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Inventory cannot be empty");
        }

        String[] tokens = trimmed.split("\\s+");
        if (tokens.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Inventory must be provided as species/count pairs, for example: " +
                            "molecules:hydrogen_gas 2 molecules:oxygen_gas 1"
            );
        }

        Map<String, Integer> inventory = new LinkedHashMap<>();

        for (int i = 0; i < tokens.length; i += 2) {
            String speciesId = tokens[i];
            String countText = tokens[i + 1];

            if (NuclideDataLoader.SPECIES.getById(speciesId) == null) {
                throw new IllegalArgumentException("Unknown species id in inventory: " + speciesId);
            }

            int count;
            try {
                count = Integer.parseInt(countText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid count '" + countText + "' for species " + speciesId);
            }

            if (count <= 0) {
                throw new IllegalArgumentException("Count must be positive for species " + speciesId);
            }

            inventory.merge(speciesId, count, Integer::sum);
        }

        return inventory;
    }

    private static String describeMissingRequirements(ReactionDefinition reaction, Map<String, Integer> availableSpecies) {
        StringJoiner joiner = new StringJoiner(", ");

        for (ReactionParticipant input : reaction.inputs()) {
            int available = availableSpecies.getOrDefault(input.speciesId(), 0);
            if (available < input.count()) {
                int missing = input.count() - available;
                joiner.add(input.speciesId() + " need " + input.count() + ", have " + available + ", missing " + missing);
            }
        }

        String result = joiner.toString();
        return result.isEmpty() ? "none" : result;
    }

    private static String formatParticipants(Iterable<ReactionParticipant> participants) {
        StringJoiner joiner = new StringJoiner(", ");

        for (ReactionParticipant participant : participants) {
            SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(participant.speciesId());
            if (species != null) {
                joiner.add(participant.count() + "x " + participant.speciesId() + " (" + species.name() + ")");
            } else {
                joiner.add(participant.count() + "x " + participant.speciesId());
            }
        }

        return joiner.toString();
    }

    private static String formatInventory(Map<String, Integer> inventory) {
        if (inventory.isEmpty()) {
            return "(empty)";
        }

        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(entry.getKey());
            if (species != null) {
                joiner.add(entry.getValue() + "x " + entry.getKey() + " (" + species.name() + ")");
            } else {
                joiner.add(entry.getValue() + "x " + entry.getKey());
            }
        }

        return joiner.toString();
    }
}