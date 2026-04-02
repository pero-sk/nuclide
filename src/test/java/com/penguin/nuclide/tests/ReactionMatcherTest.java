package com.penguin.nuclide.tests;

import com.penguin.nuclide.reaction.ReactionConditions;
import com.penguin.nuclide.reaction.ReactionContext;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionMatcher;
import com.penguin.nuclide.reaction.ReactionParticipant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReactionMatcherTest {

    private static ReactionDefinition unconditionalReaction() {
        return new ReactionDefinition(
                "nuclide:water_formation",
                "Water Formation",
                List.of(
                        ReactionParticipant.species("molecules:hydrogen_gas", 2),
                        ReactionParticipant.species("molecules:oxygen_gas", 1)
                ),
                List.of(
                        ReactionParticipant.species("molecules:water", 2)
                ),
                new ReactionConditions(null, null, false, null, null, null),
                40
        );
    }

    private static ReactionDefinition heatedSparkReaction() {
        return new ReactionDefinition(
                "nuclide:ignition_test",
                "Ignition Test",
                List.of(
                        ReactionParticipant.species("molecules:hydrogen_gas", 2),
                        ReactionParticipant.species("molecules:oxygen_gas", 1)
                ),
                List.of(
                        ReactionParticipant.species("molecules:water", 2)
                ),
                new ReactionConditions(300.0, null, true, null, null, null),
                40
        );
    }

    private static ReactionDefinition catalystReaction() {
        return new ReactionDefinition(
                "nuclide:catalyst_test",
                "Catalyst Test",
                List.of(
                        ReactionParticipant.species("atoms:sodium", 1),
                        ReactionParticipant.species("atoms:chlorine", 1)
                ),
                List.of(
                        ReactionParticipant.species("molecules:sodium_chloride", 1)
                ),
                new ReactionConditions(null, null, false, "atoms:platinum", null, null),
                20
        );
    }

    @Test
    void matchesUnconditionalReaction() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        assertTrue(ReactionMatcher.matches(unconditionalReaction(), available));
    }

    @Test
    void failsWhenSpeciesMissing() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2
        );

        assertFalse(ReactionMatcher.matches(unconditionalReaction(), available));
    }

    @Test
    void failsWhenSpeciesInsufficient() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 1,
                "molecules:oxygen_gas", 1
        );

        assertFalse(ReactionMatcher.matches(unconditionalReaction(), available));
    }

    @Test
    void failsWhenBelowMinTemperature() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        ReactionContext context = new ReactionContext(100.0, true, 1.0);

        assertFalse(ReactionMatcher.matches(heatedSparkReaction(), available, context));
    }

    @Test
    void failsWhenSparkIsRequiredButMissing() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        ReactionContext context = new ReactionContext(500.0, false, 1.0);

        assertFalse(ReactionMatcher.matches(heatedSparkReaction(), available, context));
    }

    @Test
    void matchesWhenTemperatureAndSparkRequirementsAreMet() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        ReactionContext context = new ReactionContext(500.0, true, 1.0);

        assertTrue(ReactionMatcher.matches(heatedSparkReaction(), available, context));
    }

    @Test
    void failsWhenCatalystIsMissing() {
        Map<String, Integer> available = Map.of(
                "atoms:sodium", 1,
                "atoms:chlorine", 1
        );

        assertFalse(ReactionMatcher.matches(catalystReaction(), available, ReactionContext.DEFAULT));
    }

    @Test
    void matchesWhenCatalystIsPresent() {
        Map<String, Integer> available = Map.of(
                "atoms:sodium", 1,
                "atoms:chlorine", 1,
                "atoms:platinum", 1
        );

        assertTrue(ReactionMatcher.matches(catalystReaction(), available, ReactionContext.DEFAULT));
    }
}