package com.penguin.nuclide.tests;

import com.penguin.nuclide.reaction.ReactionConditions;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionMatcher;
import com.penguin.nuclide.reaction.ReactionParticipant;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagMatchingTest {

    @BeforeEach
    void setUpTags() {
        SpeciesTagDataLoader.TAGS.clear();
        SpeciesTagDataLoader.TAGS.getOrCreate("nuclide:carbon_any").add("atoms:carbon");
        SpeciesTagDataLoader.TAGS.getOrCreate("nuclide:carbon_any").add("atoms:carbon_14");
    }

    @Test
    void tagInputMatchesAnySpeciesInTag() {
        ReactionDefinition reaction = new ReactionDefinition(
                "nuclide:generic_carbon_test",
                "Generic Carbon Test",
                List.of(ReactionParticipant.tag("nuclide:carbon_any", 1)),
                List.of(ReactionParticipant.species("atoms:oxygen", 1)),
                new ReactionConditions(null, null, false, null, null, null),
                20
        );

        Map<String, Integer> available = Map.of("atoms:carbon_14", 1);

        assertTrue(ReactionMatcher.matchesSpeciesOnly(reaction, available));
    }

    @Test
    void tagInputFailsWhenNoSpeciesInTagIsPresent() {
        ReactionDefinition reaction = new ReactionDefinition(
                "nuclide:generic_carbon_test",
                "Generic Carbon Test",
                List.of(ReactionParticipant.tag("nuclide:carbon_any", 1)),
                List.of(ReactionParticipant.species("atoms:oxygen", 1)),
                new ReactionConditions(null, null, false, null, null, null),
                20
        );

        Map<String, Integer> available = Map.of("atoms:sodium", 1);

        assertFalse(ReactionMatcher.matchesSpeciesOnly(reaction, available));
    }
}