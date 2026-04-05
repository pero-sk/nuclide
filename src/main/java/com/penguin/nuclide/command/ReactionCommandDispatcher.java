package com.penguin.nuclide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.reaction.ReactionConditions;
import com.penguin.nuclide.tag.SpeciesTagDefinition;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionExecutor;
import com.penguin.nuclide.reaction.ReactionMatcher;
import com.penguin.nuclide.reaction.ReactionParticipant;
import com.penguin.nuclide.reaction.ReactionSearcher;
import com.penguin.nuclide.species.SpeciesContainer;

import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.StringJoiner;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ReactionCommandDispatcher {

    private ReactionCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("nuclide")
                .then(literal("reaction")
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
                                                                    " \n| name=" + reaction.name() +
                                                                    " \n| inputs=" + reaction.inputs().size() +
                                                                    " \n| outputs=" + reaction.outputs().size() +
                                                                    " \n| duration=" + reaction.durationTicks() +
                                                                    " \n| conditions=" + reaction.conditions()
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

                                                    SpeciesContainer inventory;
                                                    try {
                                                        inventory = parseInventory(inventoryText);
                                                    } catch (IllegalArgumentException e) {
                                                        context.getSource().sendError(Text.of(e.getMessage()));
                                                        return 0;
                                                    }

                                                    boolean matches = ReactionMatcher.matchesSpeciesOnly(reaction, inventory);

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

                                                    SpeciesContainer inventory;
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

                                                    SpeciesContainer result = ReactionExecutor.execute(reaction, inventory);

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
                        .then(literal("search")
                            .then(argument("inventory", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String inventoryText = StringArgumentType.getString(context, "inventory");

                                    SpeciesContainer inventory;
                                    try {
                                        inventory = parseInventory(inventoryText);
                                    } catch (IllegalArgumentException e) {
                                        context.getSource().sendError(Text.of(e.getMessage()));
                                        return 0;
                                    }

                                    java.util.List<ReactionDefinition> matches =
                                            ReactionSearcher.findMatches(
                                                    ReactionDataLoader.REACTIONS.values(),
                                                    inventory,
                                                    false
                                            );

                                    if (matches.isEmpty()) {
                                        context.getSource().sendError(Text.of(
                                                "No matching reactions for inventory: " + formatInventory(inventory)
                                        ));
                                        return 0;
                                    }

                                    context.getSource().sendFeedback(
                                            () -> Text.of("Matching reactions: " + matches.size()),
                                            false
                                    );

                                    for (ReactionDefinition reaction : matches) {
                                        context.getSource().sendFeedback(
                                                () -> Text.of(
                                                        "- " + reaction.id() +
                                                        " \n| name=" + reaction.name() +
                                                        " \n| inputs=" + formatParticipants(reaction.inputs()) +
                                                        " \n| outputs=" + formatParticipants(reaction.outputs()) +
                                                        " \n| conditions=" + formatConditions(reaction.conditions()) +
                                                        " \n| duration=" + reaction.durationTicks() + " ticks"
                                                ),
                                                false
                                        );
                                    }

                                    return matches.size();
                                })))
        ));
    }

    private static SpeciesContainer parseInventory(String input) {
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

        SpeciesContainer inventory = new SpeciesContainer();

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

            inventory.add(speciesId, count);
        }

        return inventory;
    }

    private static String describeMissingRequirements(ReactionDefinition reaction, SpeciesContainer availableSpecies) {
        StringJoiner joiner = new StringJoiner(", ");

        for (ReactionParticipant input : reaction.inputs()) {
            if (input.isSpecies()) {
                int available = availableSpecies.countOf(input.speciesId());
                if (available < input.count()) {
                    int missing = input.count() - available;
                    joiner.add(input.speciesId() + " need " + input.count() + ", have " + available + ", missing " + missing);
                }
            } else if (input.isTag()) {
                SpeciesTagDefinition tag = SpeciesTagDataLoader.TAGS.getById(input.tagId());
                boolean satisfied = false;

                if (tag != null) {
                    for (String speciesId : tag.values()) {
                        int available = availableSpecies.countOf(speciesId);
                        if (available >= input.count()) {
                            satisfied = true;
                            break;
                        }
                    }
                }

                if (!satisfied) {
                    joiner.add("#" + input.tagId() + " need " + input.count() + " from one matching species");
                }
            }
        }

        String result = joiner.toString();
        return result.isEmpty() ? "none" : result;
    }

    private static String formatParticipants(Iterable<ReactionParticipant> participants) {
        StringJoiner joiner = new StringJoiner(", ");

        for (ReactionParticipant participant : participants) {
            if (participant.isSpecies()) {
                SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(participant.speciesId());
                if (species != null) {
                    joiner.add(participant.count() + "x " + participant.speciesId() + " (" + species.name() + ")");
                } else {
                    joiner.add(participant.count() + "x " + participant.speciesId());
                }
            } else if (participant.isTag()) {
                joiner.add(participant.count() + "x #" + participant.tagId());
            } else {
                joiner.add(participant.count() + "x <invalid participant>");
            }
        }

        return joiner.toString();
    }

    private static String formatInventory(SpeciesContainer inventory) {
        if (inventory.asMap().isEmpty()) {
            return "(empty)";
        }

        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<String, Integer> entry : inventory.asMap().entrySet()) {
            SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(entry.getKey());
            if (species != null) {
                joiner.add(entry.getValue() + "x " + entry.getKey() + " (" + species.name() + ")");
            } else {
                joiner.add(entry.getValue() + "x " + entry.getKey());
            }
        }

        return joiner.toString();
    }

    private static String formatConditions(ReactionConditions conditions) {
        if (conditions == null) {
            return "none";
        }

        StringJoiner joiner = new StringJoiner(", ");

        if (conditions.minTemperature() != null) {
            joiner.add("min_temperature=" + conditions.minTemperature());
        }

        if (conditions.maxTemperature() != null) {
            joiner.add("max_temperature=" + conditions.maxTemperature());
        }

        if (conditions.requiresSpark()) {
            joiner.add("requires_spark=true");
        }

        if (conditions.hasCatalyst()) {
            SpeciesDefinition catalyst = NuclideDataLoader.SPECIES.getById(conditions.catalystSpeciesId());
            if (catalyst != null) {
                joiner.add("catalyst=" + conditions.catalystSpeciesId() + " (" + catalyst.name() + ")");
            } else {
                joiner.add("catalyst=" + conditions.catalystSpeciesId());
            }
        }

        String result = joiner.toString();
        return result.isEmpty() ? "none" : result;
    }
}