package com.penguin.nuclide.inefficiency;

import java.util.List;
import java.util.Locale;

public interface InefficiencyProvider {

    record InefficiencyEntry(String name, int penaltyP) {}

    default int getBasePenaltyP() {
        return 0;
    }

    List<InefficiencyEntry> getInefficiencyEntries();

    default int getTotalPenaltyP() {
        int total = getBasePenaltyP();

        for (InefficiencyEntry entry : getInefficiencyEntries()) {
            total += entry.penaltyP();
        }

        return total;
    }

    default double getInefficiencyFraction() {
        int total = getTotalPenaltyP();
        return total / (total + 89.0);
    }

    default double getEfficiencyFraction() {
        return 1.0 - getInefficiencyFraction();
    }

    default String getInefficiencyBreakdownString() {
        List<InefficiencyEntry> entries = getInefficiencyEntries();
        int basePenalty = getBasePenaltyP();

        int totalPenalty = basePenalty;
        for (InefficiencyEntry entry : entries) {
            totalPenalty += entry.penaltyP();
        }

        double inefficiencyPercent = (totalPenalty / (totalPenalty + 89.0)) * 100.0;

        StringBuilder builder = new StringBuilder();

        if (basePenalty > 0) {
            builder.append("Base penalty: +").append(basePenalty).append("P");
        }

        for (InefficiencyEntry entry : entries) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }

            builder.append(entry.name())
                    .append(": +")
                    .append(entry.penaltyP())
                    .append("P");
        }

        if (!builder.isEmpty()) {
            builder.append("\n");
        }

        builder.append("Total penalty: +").append(totalPenalty).append("P");
        builder.append("\n");
        builder.append("Inefficiency: ")
                .append(String.format(Locale.ROOT, "%.1f%%", inefficiencyPercent));

        return builder.toString();
    }
}