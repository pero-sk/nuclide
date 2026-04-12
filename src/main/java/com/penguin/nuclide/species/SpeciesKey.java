package com.penguin.nuclide.species;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.penguin.nuclide.atomic.StateType;

public record SpeciesKey(String speciesId, StateType currentState) {
    public static final Codec<SpeciesKey> CODEC =
            Codec.STRING.comapFlatMap(
                    SpeciesKey::parse,
                    SpeciesKey::asCodecString
            );

    private static DataResult<SpeciesKey> parse(String s) {
        int split = s.lastIndexOf('|');
        if (split < 0) {
            return DataResult.error(() -> "Invalid SpeciesKey: " + s);
        }

        String speciesId = s.substring(0, split);
        String stateName = s.substring(split + 1);

        try {
            return DataResult.success(new SpeciesKey(speciesId, StateType.valueOf(stateName)));
        } catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Invalid StateType in SpeciesKey: " + s);
        }
    }

    private String asCodecString() {
        return speciesId + "|" + currentState.name();
    }
}