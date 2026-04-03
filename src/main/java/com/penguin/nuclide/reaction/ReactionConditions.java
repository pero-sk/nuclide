package com.penguin.nuclide.reaction;


public final class ReactionConditions {
    private final Double minTemperature;
    private final Double maxTemperature;
    private final boolean requiresSpark;
    private final String catalystSpeciesId;

    private final Double minPressure;
    private final Double maxPressure;

    public ReactionConditions(
            Double minTemperature,
            Double maxTemperature,
            boolean requiresSpark,
            String catalystSpeciesId,
            Double minPressure,
            Double maxPressure
    ) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.requiresSpark = requiresSpark;
        this.catalystSpeciesId = catalystSpeciesId;
        this.minPressure = minPressure;
        this.maxPressure = maxPressure;
    }

    public Double minTemperature() {
        return minTemperature;
    }

    public Double maxTemperature() {
        return maxTemperature;
    }

    public boolean requiresSpark() {
        return requiresSpark;
    }

    public String catalystSpeciesId() {
        return catalystSpeciesId;
    }

    public boolean hasTemperatureBounds() {
        return minTemperature != null || maxTemperature != null;
    }

    public boolean hasCatalyst() {
        return catalystSpeciesId != null && !catalystSpeciesId.isBlank();
    }

    public Double minPressure() {
        return minPressure;
    }

    public Double maxPressure() {
        return maxPressure;
    }

    @Override
    public String toString() {
        return "ReactionConditions{" +
                "minTemperature=" + minTemperature +
                ", maxTemperature=" + maxTemperature +
                ", requiresSpark=" + requiresSpark +
                ", catalystSpeciesId='" + catalystSpeciesId + '\'' +
                '}';
    }
}