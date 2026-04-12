package com.penguin.nuclide.reaction;

import java.util.List;
import java.util.Objects;

public final class ReactionDefinition {
    private final String id;
    private final String name;
    private final List<ReactionParticipant> inputs;
    private final List<ReactionParticipant> outputs;
    private final ReactionConditions conditions;
    private final int durationTicks;

    public ReactionDefinition(
            String id,
            String name,
            List<ReactionParticipant> inputs,
            List<ReactionParticipant> outputs,
            ReactionConditions conditions,
            int durationTicks
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.conditions = Objects.requireNonNull(conditions);
        this.durationTicks = durationTicks;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<ReactionParticipant> inputs() {
        return inputs;
    }

    public List<ReactionParticipant> outputs() {
        return outputs;
    }

    public ReactionConditions conditions() {
        return conditions;
    }

    public int durationTicks() {
        return durationTicks;
    }

    @Override
    public String toString() {
        return "ReactionDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", conditions=" + conditions +
                ", durationTicks=" + durationTicks +
                '}';
    }
}