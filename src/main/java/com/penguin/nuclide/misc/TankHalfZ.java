package com.penguin.nuclide.misc;

import net.minecraft.util.StringIdentifiable;

public enum TankHalfZ implements StringIdentifiable {
    NORTH("north"),
    SOUTH("south");

    private final String name;

    TankHalfZ(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
