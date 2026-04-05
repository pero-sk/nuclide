package com.penguin.nuclide.gas;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Identifier;

public final class GasRegistry {
    private final Map<Identifier, GasType> registry = new HashMap<>();

    public void register(GasType gasType) {
        if (gasType == null) {
            throw new IllegalArgumentException("GasType cannot be null");
        }
        if (registry.containsKey(gasType.getId())) {
            throw new IllegalArgumentException(
                    "GasType with id " + gasType.getId() + " is already registered"
            );
        }

        registry.put(gasType.getId(), gasType);
    }

    public GasType get(Identifier id) {
        return registry.get(id);
    }


    public boolean contains(Identifier id) {
        return registry.containsKey(id);
    }
}