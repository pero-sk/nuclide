package com.penguin.nuclide.nbt;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.species.SpeciesContainer;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class NuclideDataComponents {
    public static final ComponentType<SpeciesContainer> SPECIES_CONTAINER =
            Registry.register(
                    Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(Nuclide.MOD_ID, "species_container"),
                    ComponentType.<SpeciesContainer>builder()
                            .codec(SpeciesContainer.CODEC)
                            .build()
            );

    private NuclideDataComponents() {}

    public static void init() {}
}