package com.penguin.nuclide.tests;

import com.penguin.nuclide.reaction.ReactionConditions;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionExecutor;
import com.penguin.nuclide.reaction.ReactionParticipant;
import com.penguin.nuclide.reaction.ReactionResolver;
import com.penguin.nuclide.reaction.ResolvedReactionParticipant;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TagResolutionTest {

    @BeforeEach
    void setUpTags() {
        SpeciesTagDataLoader.TAGS.clear();
        SpeciesTagDataLoader.TAGS.getOrCreate("nuclide:carbon_any").add("atoms:carbon");
        SpeciesTagDataLoader.TAGS.getOrCreate("nuclide:carbon_any").add("atoms:carbon_14");
    }

    private static ReactionDefinition taggedReaction() {
        return new ReactionDefinition(
                "nuclide:generic_carbon_test",
                "Generic Carbon Test",
                List.of(
                        ReactionParticipant.tag("nuclide:carbon_any", 1)
                ),
                List.of(
                        ReactionParticipant.species("atoms:oxygen", 1)
                ),
                new ReactionConditions(null, null, false, null, null, null),
                20
        );
    }

    @Test
    void resolvesToFirstMatchingSpeciesInTagOrder() {
        List<ResolvedReactionParticipant> resolved = ReactionResolver.resolveInputs(
                taggedReaction(),
                Map.of(
                        "atoms:carbon", 1,
                        "atoms:carbon_14", 1
                )
        );

        assertEquals(1, resolved.size());
        assertEquals("atoms:carbon", resolved.get(0).resolvedSpeciesId());
    }

    @Test
    void resolvesToLaterSpeciesWhenEarlierOneIsUnavailable() {
        List<ResolvedReactionParticipant> resolved = ReactionResolver.resolveInputs(
                taggedReaction(),
                Map.of("atoms:carbon_14", 1)
        );

        assertEquals(1, resolved.size());
        assertEquals("atoms:carbon_14", resolved.get(0).resolvedSpeciesId());
    }

    @Test
    void executorConsumesResolvedTagSpecies() {
        Map<String, Integer> result = ReactionExecutor.execute(
                taggedReaction(),
                Map.of(
                        "atoms:carbon", 1,
                        "atoms:carbon_14", 1
                )
        );

        assertFalse(result.containsKey("atoms:carbon"));
        assertEquals(1, result.get("atoms:carbon_14"));
        assertEquals(1, result.get("atoms:oxygen"));
    }

    @Test
    void executorConsumesLaterResolvedSpeciesWhenNeeded() {
        Map<String, Integer> result = ReactionExecutor.execute(
                taggedReaction(),
                Map.of("atoms:carbon_14", 1)
        );

        assertFalse(result.containsKey("atoms:carbon_14"));
        assertEquals(1, result.get("atoms:oxygen"));
    }

    @Test
    void resolverFailsWhenNoSpeciesInTagSatisfiesInput() {
        assertThrows(IllegalArgumentException.class, () ->
                ReactionResolver.resolveInputs(
                        taggedReaction(),
                        Map.of("atoms:sodium", 1)
                )
        );
    }
}