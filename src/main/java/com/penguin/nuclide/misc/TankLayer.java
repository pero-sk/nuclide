package com.penguin.nuclide.misc;

import net.minecraft.util.StringIdentifiable;

public enum TankLayer implements StringIdentifiable {
    SINGLE("single"),
    BOTTOM("bottom"),
    MIDDLE("middle"),
    TOP("top");

    private final String name;

    TankLayer(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
