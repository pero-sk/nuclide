package com.penguin.nuclide.reaction;

public record ReactionContext(
        double temperature,
        boolean hasSpark
) {
    public static final ReactionContext DEFAULT = new ReactionContext(20.0, false);
}