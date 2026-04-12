package com.penguin.nuclide.data;

import java.util.Objects;

public final class StabilityDefinition {
    public static final StabilityDefinition STABLE =
            new StabilityDefinition(StabilityType.STABLE, null, false, false);

    private final StabilityType type;
    private final Integer lifetimeTicks;
    private final boolean decaysInContainer;
    private final boolean decaysInWorld;

    public StabilityDefinition(
            StabilityType type,
            Integer lifetimeTicks,
            boolean decaysInContainer,
            boolean decaysInWorld
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.lifetimeTicks = lifetimeTicks;
        this.decaysInContainer = decaysInContainer;
        this.decaysInWorld = decaysInWorld;
    }

    public StabilityType type() {
        return type;
    }

    public Integer lifetimeTicks() {
        return lifetimeTicks;
    }

    public boolean decaysInContainer() {
        return decaysInContainer;
    }

    public boolean decaysInWorld() {
        return decaysInWorld;
    }

    public boolean isStable() {
        return type == StabilityType.STABLE;
    }

    public boolean isUnstable() {
        return type == StabilityType.UNSTABLE || type == StabilityType.TRANSIENT;
    }

    @Override
    public String toString() {
        return "StabilityDefinition{" +
                "type=" + type +
                ", lifetimeTicks=" + lifetimeTicks +
                ", decaysInContainer=" + decaysInContainer +
                ", decaysInWorld=" + decaysInWorld +
                '}';
    }
}