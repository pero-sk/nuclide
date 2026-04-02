package com.penguin.nuclide.tests;

import com.penguin.nuclide.reaction.ReactionConditions;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionExecutor;
import com.penguin.nuclide.reaction.ReactionParticipant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReactionExecutorTest {

    private static ReactionDefinition waterFormation() {
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

    @Test
    void executesReactionAndProducesOutputs() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        Map<String, Integer> result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(1, result.size());
        assertEquals(2, result.get("molecules:water"));
        assertFalse(result.containsKey("molecules:hydrogen_gas"));
        assertFalse(result.containsKey("molecules:oxygen_gas"));
    }

    @Test
    void preservesUnrelatedSpecies() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1,
                "atoms:sodium", 3
        );

        Map<String, Integer> result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(2, result.size());
        assertEquals(2, result.get("molecules:water"));
        assertEquals(3, result.get("atoms:sodium"));
    }

    @Test
    void reducesCountsInsteadOfRemovingWhenExtrasRemain() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 4,
                "molecules:oxygen_gas", 2
        );

        Map<String, Integer> result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(3, result.size());
        assertEquals(2, result.get("molecules:hydrogen_gas"));
        assertEquals(1, result.get("molecules:oxygen_gas"));
        assertEquals(2, result.get("molecules:water"));
    }

    @Test
    void throwsWhenReactionDoesNotMatch() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 1,
                "molecules:oxygen_gas", 1
        );

        assertThrows(IllegalArgumentException.class, () ->
                ReactionExecutor.execute(waterFormation(), available)
        );
    }

    @Test
    void doesNotMutateOriginalMap() {
        Map<String, Integer> available = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        Map<String, Integer> result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(2, available.get("molecules:hydrogen_gas"));
        assertEquals(1, available.get("molecules:oxygen_gas"));

        assertNotSame(available, result);
    }
}