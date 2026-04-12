package com.penguin.nuclide.nowns.validation;

import com.penguin.nuclide.atomic.Atom;
import com.penguin.nuclide.atomic.Bond;
import com.penguin.nuclide.atomic.BondType;
import com.penguin.nuclide.atomic.Molecule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NownsValidator {

    private NownsValidator() {}

    public static ValidationResult validate(Molecule molecule) {
        ValidationResult result = new ValidationResult();

        if (molecule == null) {
            result.addError("null_molecule", "Molecule cannot be null");
            return result;
        }

        validateAtoms(molecule, result);
        validateBonds(molecule, result);
        validateBondOrderSums(molecule, result);

        return result;
    }

    public static void validateOrThrow(Molecule molecule) {
        ValidationResult result = validate(molecule);

        if (!result.isValid()) {
            throw new IllegalStateException("NOWNS validation failed:\n" + result.formatErrors());
        }
    }

    private static void validateAtoms(Molecule molecule, ValidationResult result) {
        if (molecule.atoms().isEmpty()) {
            result.addError("empty_molecule", "Molecule must contain at least one atom");
            return;
        }

        for (int i = 0; i < molecule.atoms().size(); i++) {
            Atom atom = molecule.atoms().get(i);

            if (atom.element() == null || atom.element().isBlank()) {
                result.addError("blank_element", "Atom at index " + i + " has a blank element symbol");
            }

            if (atom.isotope() == 0 || atom.isotope() < -1) {
                result.addError(
                        "invalid_isotope",
                        "Atom at index " + i + " has invalid isotope value " + atom.isotope()
                );
            }
        }
    }

    private static void validateBonds(Molecule molecule, ValidationResult result) {
        int atomCount = molecule.atoms().size();
        Set<String> seenPairs = new HashSet<>();

        for (int i = 0; i < molecule.bonds().size(); i++) {
            Bond bond = molecule.bonds().get(i);

            if (bond.a() < 0 || bond.a() >= atomCount) {
                result.addError(
                        "bond_index_out_of_range",
                        "Bond at index " + i + " has invalid atom index a=" + bond.a()
                );
            }

            if (bond.b() < 0 || bond.b() >= atomCount) {
                result.addError(
                        "bond_index_out_of_range",
                        "Bond at index " + i + " has invalid atom index b=" + bond.b()
                );
            }

            if (bond.a() == bond.b()) {
                result.addError(
                        "self_bond",
                        "Bond at index " + i + " connects atom " + bond.a() + " to itself"
                );
            }

            int low = Math.min(bond.a(), bond.b());
            int high = Math.max(bond.a(), bond.b());
            String key = low + ":" + high;

            if (!seenPairs.add(key)) {
                result.addError(
                        "duplicate_bond",
                        "Duplicate bond detected between atoms " + low + " and " + high
                );
            }

            if (bond.type() == null) {
                result.addError(
                        "null_bond_type",
                        "Bond at index " + i + " has null bond type"
                );
            }
        }
    }

    private static void validateBondOrderSums(Molecule molecule, ValidationResult result) {
        Map<Integer, Double> bondOrderSums = new HashMap<>();

        for (int i = 0; i < molecule.atoms().size(); i++) {
            bondOrderSums.put(i, 0.0);
        }

        for (Bond bond : molecule.bonds()) {
            if (bond.a() < 0 || bond.a() >= molecule.atoms().size()) {
                continue;
            }
            if (bond.b() < 0 || bond.b() >= molecule.atoms().size()) {
                continue;
            }
            if (bond.a() == bond.b()) {
                continue;
            }
            if (bond.type() == null) {
                continue;
            }

            double order = bondOrderValue(bond.type());
            bondOrderSums.put(bond.a(), bondOrderSums.get(bond.a()) + order);
            bondOrderSums.put(bond.b(), bondOrderSums.get(bond.b()) + order);
        }

        for (int i = 0; i < molecule.atoms().size(); i++) {
            Atom atom = molecule.atoms().get(i);
            double sum = bondOrderSums.get(i);

            double max = maxAllowedBondOrder(atom);
            if (sum > max) {
                result.addError(
                        "bond_order_exceeds_limit",
                        "Atom at index " + i +
                        " (" + atom.element() + ") has bond-order sum " + sum +
                        ", exceeding allowed limit " + max
                );
            }
        }
    }

    private static double bondOrderValue(BondType type) {
        if (type == null) return 0.0;

        return switch (type) {
            case SINGLE -> 1.0;
            case DOUBLE -> 2.0;
            case TRIPLE -> 3.0;
            case AROMATIC -> 1.5;
        };
    }

    /**
     * v1 heuristic limits.
     * These are not full chemistry rules, just sanity guards.
     */
    private static double maxAllowedBondOrder(Atom atom) {
        return switch (atom.element()) {
            case "H" -> 1.0;
            case "He", "Ne", "Ar", "Kr", "Xe", "Rn", "Og" -> 0.0;
            case "O" -> 2.5;
            case "N" -> 4.0;
            case "C" -> 4.0;
            case "F", "Cl", "Br", "I", "At", "Ts" -> 1.5;
            default -> 8.0;
        };
    }
}