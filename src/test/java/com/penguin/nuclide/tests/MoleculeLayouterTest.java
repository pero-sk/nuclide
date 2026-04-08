package com.penguin.nuclide.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.penguin.nuclide.atomic.Atom;
import com.penguin.nuclide.atomic.Bond;
import com.penguin.nuclide.atomic.BondType;
import com.penguin.nuclide.atomic.Molecule;
import com.penguin.nuclide.renderer.MoleculeLayout;
import com.penguin.nuclide.renderer.MoleculeLayouter;

class MoleculeLayouterTest {

    @Test
    void layoutEmptyMoleculeProducesEmptyLayout() {
        Molecule molecule = new Molecule();

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertTrue(layout.atoms().isEmpty());
        assertTrue(layout.bonds().isEmpty());
    }

    @Test
    void layoutSingleAtomProducesOnePlacedAtom() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("O", 0, 0, false));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(1, layout.atoms().size());
        assertEquals(0, layout.bonds().size());

        MoleculeLayout.LayoutAtom atom = layout.atoms().get(0);
        assertEquals(0, atom.atomIndex());
        assertEquals("O", atom.symbol());
        assertFinite(atom.x());
        assertFinite(atom.y());
    }

    @Test
    void layoutDiatomicPreservesSingleBond() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("H", 0, 0, false));
        molecule.atoms().add(new Atom("H", 0, 0, false));
        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(2, layout.atoms().size());
        assertEquals(1, layout.bonds().size());

        MoleculeLayout.LayoutBond bond = layout.bonds().get(0);
        assertEquals(0, bond.fromAtomIndex());
        assertEquals(1, bond.toAtomIndex());
        assertEquals(1, bond.order());

        assertDistinctPositions(layout);
        assertApproximatelyCentered(layout);
    }

    @Test
    void layoutWaterPreservesAtomsAndBonds() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("H", 0, 0, false)); // 0
        molecule.atoms().add(new Atom("O", 0, 0, false)); // 1
        molecule.atoms().add(new Atom("H", 0, 0, false)); // 2

        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));
        molecule.bonds().add(new Bond(1, 2, BondType.SINGLE));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(3, layout.atoms().size());
        assertEquals(2, layout.bonds().size());

        assertHasAtomSymbols(layout, "H", "O", "H");
        assertBondOrderSet(layout, Set.of(1));
        assertDistinctPositions(layout);
        assertApproximatelyCentered(layout);
    }

    @Test
    void layoutDoubleBondPreservesBondOrder() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("O", 0, 0, false));
        molecule.atoms().add(new Atom("O", 0, 0, false));
        molecule.bonds().add(new Bond(0, 1, BondType.DOUBLE));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(2, layout.atoms().size());
        assertEquals(1, layout.bonds().size());
        assertEquals(2, layout.bonds().get(0).order());
    }

    @Test
    void layoutTripleBondPreservesBondOrder() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("N", 0, 0, false));
        molecule.atoms().add(new Atom("N", 0, 0, false));
        molecule.bonds().add(new Bond(0, 1, BondType.TRIPLE));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(2, layout.atoms().size());
        assertEquals(1, layout.bonds().size());
        assertEquals(3, layout.bonds().get(0).order());
    }

    @Test
    void layoutDisconnectedComponentsPlacesAllAtoms() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("Na", 1, 0, false)); // 0
        molecule.atoms().add(new Atom("Cl", -1, 0, false)); // 1
        molecule.atoms().add(new Atom("H", 0, 0, false)); // 2
        molecule.atoms().add(new Atom("H", 0, 0, false)); // 3

        molecule.bonds().add(new Bond(2, 3, BondType.SINGLE));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(4, layout.atoms().size());
        assertEquals(1, layout.bonds().size());

        assertAllAtomIndicesPresent(layout, 0, 1, 2, 3);
        assertDistinctPositions(layout);
        assertApproximatelyCentered(layout);
    }

    @Test
    void layoutAromaticBondMapsToSingleOrderForNow() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, 0, true));
        molecule.atoms().add(new Atom("C", 0, 0, true));
        molecule.bonds().add(new Bond(0, 1, BondType.AROMATIC));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(1, layout.bonds().size());
        assertEquals(1, layout.bonds().get(0).order());
    }

    @Test
    void layoutRingPreservesAllRingBonds() {
        Molecule molecule = new Molecule();

        for (int i = 0; i < 6; i++) {
            molecule.atoms().add(new Atom("C", 0, 0, false));
        }

        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));
        molecule.bonds().add(new Bond(1, 2, BondType.SINGLE));
        molecule.bonds().add(new Bond(2, 3, BondType.SINGLE));
        molecule.bonds().add(new Bond(3, 4, BondType.SINGLE));
        molecule.bonds().add(new Bond(4, 5, BondType.SINGLE));
        molecule.bonds().add(new Bond(5, 0, BondType.SINGLE));

        MoleculeLayout layout = MoleculeLayouter.layout(molecule);

        assertEquals(6, layout.atoms().size());
        assertEquals(6, layout.bonds().size());
        assertDistinctPositions(layout);
    }

    private static void assertHasAtomSymbols(MoleculeLayout layout, String... expectedSymbols) {
        assertEquals(expectedSymbols.length, layout.atoms().size());

        var actual = layout.atoms().stream()
                .map(MoleculeLayout.LayoutAtom::symbol)
                .toList();

        for (String expected : expectedSymbols) {
            assertTrue(actual.contains(expected), "Expected symbol missing: " + expected);
        }
    }

    private static void assertBondOrderSet(MoleculeLayout layout, Set<Integer> expectedOrders) {
        Set<Integer> actualOrders = new HashSet<>();
        for (MoleculeLayout.LayoutBond bond : layout.bonds()) {
            actualOrders.add(bond.order());
        }

        assertEquals(expectedOrders, actualOrders);
    }

    private static void assertAllAtomIndicesPresent(MoleculeLayout layout, int... expectedIndices) {
        Set<Integer> actual = new HashSet<>();
        for (MoleculeLayout.LayoutAtom atom : layout.atoms()) {
            actual.add(atom.atomIndex());
        }

        for (int expected : expectedIndices) {
            assertTrue(actual.contains(expected), "Missing atom index: " + expected);
        }
    }

    private static void assertDistinctPositions(MoleculeLayout layout) {
        Set<String> seen = new HashSet<>();

        for (MoleculeLayout.LayoutAtom atom : layout.atoms()) {
            String key = Math.round(atom.x() * 1000f) + ":" + Math.round(atom.y() * 1000f);
            assertFalse(seen.contains(key), "Duplicate atom position detected for " + key);
            seen.add(key);
        }
    }

    private static void assertApproximatelyCentered(MoleculeLayout layout) {
        if (layout.atoms().isEmpty()) {
            return;
        }

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (MoleculeLayout.LayoutAtom atom : layout.atoms()) {
            minX = Math.min(minX, atom.x());
            maxX = Math.max(maxX, atom.x());
            minY = Math.min(minY, atom.y());
            maxY = Math.max(maxY, atom.y());
        }

        float centerX = (minX + maxX) / 2.0f;
        float centerY = (minY + maxY) / 2.0f;

        assertTrue(Math.abs(centerX) < 0.001f, "Layout not centered on X: " + centerX);
        assertTrue(Math.abs(centerY) < 0.001f, "Layout not centered on Y: " + centerY);
    }

    private static void assertFinite(float value) {
        assertFalse(Float.isNaN(value), "Value was NaN");
        assertFalse(Float.isInfinite(value), "Value was infinite");
    }
}