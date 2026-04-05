package com.penguin.nuclide.misc;

import net.minecraft.util.StringIdentifiable;

public enum TankHalfX implements StringIdentifiable {
    WEST("west"),
    EAST("east");

    private final String name;

    TankHalfX(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
