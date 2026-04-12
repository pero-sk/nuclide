package com.penguin.nuclide.gas;

import org.joml.Vector3f;

import net.minecraft.util.Identifier;

public final class GasType {
    public static final Vector3f COLORLESS = new Vector3f(-1f, -1f, -1f);

    private final Identifier id;
    private final String displayName;
    private final float density;
    private final float maxSafePressure;
    private final float dissipationRate;
    private final Vector3f color;
    private final float flammability;
    private final float toxicity;

    public GasType(
            Identifier id,
            float density,
            float maxSafePressure,
            float dissipationRate,
            Vector3f color,
            String displayName,
            float flammability,
            float toxicity
    ) {
        if (id == null) throw new IllegalArgumentException("GasType id cannot be null");
        if (density <= 0f) throw new IllegalArgumentException("Gas density must be > 0");
        if (maxSafePressure <= 0f) throw new IllegalArgumentException("Max safe pressure must be > 0");
        if (dissipationRate < 0f) throw new IllegalArgumentException("Dissipation rate cannot be negative");
        if (flammability < 0f) throw new IllegalArgumentException("Flammability cannot be negative");
        if (toxicity < 0f) throw new IllegalArgumentException("Toxicity cannot be negative");

        this.id = id;
        this.density = density;
        this.maxSafePressure = maxSafePressure;
        this.dissipationRate = dissipationRate;
        this.color = color != null ? new Vector3f(color) : new Vector3f(COLORLESS);
        this.displayName = displayName != null ? displayName : id.getPath();
        this.flammability = flammability;
        this.toxicity = toxicity;
    }

    public Identifier getId() { return id; }
    public String getDisplayName() { return displayName; }
    public float getDensity() { return density; }
    public float getMaxSafePressure() { return maxSafePressure; }
    public float getDissipationRate() { return dissipationRate; }
    public Vector3f getColor() { return new Vector3f(color); }
    public boolean isColorless() { return color.x == -1f && color.y == -1f && color.z == -1f; }
    public float getFlammability() { return flammability; }
    public boolean isFlammable() { return flammability > 0f; }
    public float getToxicity() { return toxicity; }
    public boolean isToxic() { return toxicity > 0f; }
}