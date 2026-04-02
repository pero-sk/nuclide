package com.penguin.nuclide.tests;

import com.penguin.nuclide.atomic.Atom;
import com.penguin.nuclide.atomic.Bond;
import com.penguin.nuclide.atomic.BondType;
import com.penguin.nuclide.atomic.Molecule;
import com.penguin.nuclide.nowns.validation.NownsValidator;
import com.penguin.nuclide.nowns.validation.ValidationResult;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NownsValidatorTest {

    @Test
    void validatesSingleAtom() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("O", 0, -1, false));

        ValidationResult result = NownsValidator.validate(molecule);

        assertTrue(result.isValid());
        assertEquals(0, result.errors().size());
    }

    @Test
    void validatesDisconnectedIons() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("Na", 1, -1, false));
        molecule.atoms().add(new Atom("Cl", -1, -1, false));

        ValidationResult result = NownsValidator.validate(molecule);

        assertTrue(result.isValid());
    }

    @Test
    void validatesSimpleBondedMolecule() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.atoms().add(new Atom("O", 0, -1, false));
        molecule.bonds().add(new Bond(0, 1, BondType.DOUBLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertTrue(result.isValid());
    }

    @Test
    void rejectsNullMolecule() {
        ValidationResult result = NownsValidator.validate(null);

        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("null_molecule", result.errors().get(0).code());
    }

    @Test
    void rejectsEmptyMolecule() {
        Molecule molecule = new Molecule();

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertEquals("empty_molecule", result.errors().get(0).code());
    }

    @Test
    void rejectsBlankElement() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("", 0, -1, false));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("blank_element")));
    }

    @Test
    void rejectsInvalidIsotopeZero() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("U", 0, 0, false));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("invalid_isotope")));
    }

    @Test
    void rejectsInvalidNegativeIsotopeBelowMinusOne() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("U", 0, -2, false));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("invalid_isotope")));
    }

    @Test
    void rejectsBondIndexOutOfRange_a() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.bonds().add(new Bond(1, 0, BondType.SINGLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("bond_index_out_of_range")));
    }

    @Test
    void rejectsBondIndexOutOfRange_b() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.bonds().add(new Bond(0, 2, BondType.SINGLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("bond_index_out_of_range")));
    }

    @Test
    void rejectsSelfBond() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.bonds().add(new Bond(0, 0, BondType.SINGLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("self_bond")));
    }

    @Test
    void rejectsDuplicateBond() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.atoms().add(new Atom("O", 0, -1, false));
        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));
        molecule.bonds().add(new Bond(1, 0, BondType.DOUBLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("duplicate_bond")));
    }

    @Test
    void rejectsHydrogenWithTooManyBonds() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("H", 0, -1, false));
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.atoms().add(new Atom("O", 0, -1, false));

        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));
        molecule.bonds().add(new Bond(0, 2, BondType.SINGLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("bond_order_exceeds_limit")));
    }

    @Test
    void rejectsHalogenWithTooManyBonds() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("Cl", 0, -1, false));
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.atoms().add(new Atom("O", 0, -1, false));

        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));
        molecule.bonds().add(new Bond(0, 2, BondType.SINGLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("bond_order_exceeds_limit")));
    }

    @Test
    void rejectsNobleGasWithBond() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("He", 0, -1, false));
        molecule.atoms().add(new Atom("H", 0, -1, false));
        molecule.bonds().add(new Bond(0, 1, BondType.SINGLE));

        ValidationResult result = NownsValidator.validate(molecule);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("bond_order_exceeds_limit")));
    }

    @Test
    void validateOrThrowDoesNotThrowForValidMolecule() {
        Molecule molecule = new Molecule();
        molecule.atoms().add(new Atom("C", 0, -1, false));
        molecule.atoms().add(new Atom("O", 0, -1, false));
        molecule.bonds().add(new Bond(0, 1, BondType.DOUBLE));

        assertDoesNotThrow(() -> NownsValidator.validateOrThrow(molecule));
    }

    @Test
    void validateOrThrowThrowsForInvalidMolecule() {
        Molecule molecule = new Molecule();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> NownsValidator.validateOrThrow(molecule)
        );

        assertTrue(ex.getMessage().contains("NOWNS validation failed"));
        assertTrue(ex.getMessage().contains("empty_molecule"));
    }

    @Test
    void formatErrorsIncludesCodesAndMessages() {
        ValidationResult result = new ValidationResult();
        result.addError("example_code", "Example message");

        String formatted = result.formatErrors();

        assertTrue(formatted.contains("example_code"));
        assertTrue(formatted.contains("Example message"));
    }
}