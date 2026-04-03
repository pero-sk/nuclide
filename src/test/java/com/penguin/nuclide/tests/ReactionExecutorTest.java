package com.penguin.nuclide.tests;

import com.penguin.nuclide.reaction.ReactionConditions;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionExecutor;
import com.penguin.nuclide.reaction.ReactionParticipant;
import com.penguin.nuclide.species.SpeciesContainer;

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
        Map<String, Integer> initial = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );


        SpeciesContainer available = new SpeciesContainer(initial);

        SpeciesContainer result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(1, result.asMap().size());
        assertEquals(2, result.asMap().get("molecules:water"));
        assertFalse(result.asMap().containsKey("molecules:hydrogen_gas"));
        assertFalse(result.asMap().containsKey("molecules:oxygen_gas"));
    }

    @Test
    void preservesUnrelatedSpecies() {
        Map<String, Integer> init = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1,
                "atoms:sodium", 3
        );

        SpeciesContainer available = new SpeciesContainer(init);

        SpeciesContainer result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(2, result.asMap().size());
        assertEquals(2, result.asMap().get("molecules:water"));
        assertEquals(3, result.asMap().get("atoms:sodium"));
    }

    @Test
    void reducesCountsInsteadOfRemovingWhenExtrasRemain() {
        Map<String, Integer> init = Map.of(
                "molecules:hydrogen_gas", 4,
                "molecules:oxygen_gas", 2
        );

        SpeciesContainer available = new SpeciesContainer(init);

        SpeciesContainer result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(3, result.asMap().size());
        assertEquals(2, result.asMap().get("molecules:hydrogen_gas"));
        assertEquals(1, result.asMap().get("molecules:oxygen_gas"));
        assertEquals(2, result.asMap().get("molecules:water"));
    }

    @Test
    void throwsWhenReactionDoesNotMatch() {
        Map<String, Integer> init = Map.of(
                "molecules:hydrogen_gas", 1,
                "molecules:oxygen_gas", 1
        );

        SpeciesContainer available = new SpeciesContainer(init);

        assertThrows(IllegalArgumentException.class, () ->
                ReactionExecutor.execute(waterFormation(), available)
        );
    }

    @Test
    void doesNotMutateOriginalMap() {
        Map<String, Integer> init = Map.of(
                "molecules:hydrogen_gas", 2,
                "molecules:oxygen_gas", 1
        );

        SpeciesContainer available = new SpeciesContainer(init);

        SpeciesContainer result = ReactionExecutor.execute(waterFormation(), available);

        assertEquals(2, available.asMap().get("molecules:hydrogen_gas"));
        assertEquals(1, available.asMap().get("molecules:oxygen_gas"));

        assertNotSame(available, result);
    }
}