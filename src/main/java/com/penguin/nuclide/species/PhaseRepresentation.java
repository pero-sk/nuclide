package com.penguin.nuclide.species;

public record PhaseRepresentation(String block) {
    public PhaseRepresentation {
        if (block != null && block.isBlank()) {
            throw new IllegalArgumentException("representation block cannot be blank");
        }
    }
}