package com.penguin.nuclide.tests;

import com.penguin.nuclide.atomic.BondType;
import com.penguin.nuclide.nowns.NownsParseException;
import com.penguin.nuclide.nowns.NownsParser;
import com.penguin.nuclide.nowns.ParsedMolecule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NownsParserTest {

    @Test
    void parsesSimpleChain() {
        ParsedMolecule parsed = NownsParser.parse("CCO");

        assertEquals("nuclide", parsed.namespace());
        assertEquals(3, parsed.molecule().atoms().size());
        assertEquals(2, parsed.molecule().bonds().size());

        assertEquals("C", parsed.molecule().atoms().get(0).element());
        assertEquals("C", parsed.molecule().atoms().get(1).element());
        assertEquals("O", parsed.molecule().atoms().get(2).element());

        assertEquals(BondType.SINGLE, parsed.molecule().bonds().get(0).type());
        assertEquals(BondType.SINGLE, parsed.molecule().bonds().get(1).type());
    }

    @Test
    void parsesExplicitNamespace() {
        ParsedMolecule parsed = NownsParser.parse("nuclide:CCO");

        assertEquals("nuclide", parsed.namespace());
        assertEquals(3, parsed.molecule().atoms().size());
        assertEquals(2, parsed.molecule().bonds().size());
    }

    @Test
    void parsesSingleAtom() {
        ParsedMolecule parsed = NownsParser.parse("O");

        assertEquals("nuclide", parsed.namespace());
        assertEquals(1, parsed.molecule().atoms().size());
        assertEquals(0, parsed.molecule().bonds().size());

        assertEquals("O", parsed.molecule().atoms().get(0).element());
        assertEquals(0, parsed.molecule().atoms().get(0).charge());
        assertEquals(-1, parsed.molecule().atoms().get(0).isotope());
        assertFalse(parsed.molecule().atoms().get(0).aromatic());
    }

    @Test
    void parsesBracketAtom() {
        ParsedMolecule parsed = NownsParser.parse("nuclide:[He]");

        assertEquals("nuclide", parsed.namespace());
        assertEquals(1, parsed.molecule().atoms().size());
        assertEquals("He", parsed.molecule().atoms().get(0).element());
        assertEquals(0, parsed.molecule().atoms().get(0).charge());
        assertEquals(-1, parsed.molecule().atoms().get(0).isotope());
    }

    @Test
    void parsesDisconnectedIonicCompound() {
        ParsedMolecule parsed = NownsParser.parse("[Na^+1].[Cl^-1]");

        assertEquals("nuclide", parsed.namespace());
        assertEquals(2, parsed.molecule().atoms().size());
        assertEquals(0, parsed.molecule().bonds().size());

        assertEquals("Na", parsed.molecule().atoms().get(0).element());
        assertEquals(1, parsed.molecule().atoms().get(0).charge());

        assertEquals("Cl", parsed.molecule().atoms().get(1).element());
        assertEquals(-1, parsed.molecule().atoms().get(1).charge());
    }

    @Test
    void parsesIsotopeTaggedAtom() {
        ParsedMolecule parsed = NownsParser.parse("[U:238]");

        assertEquals("nuclide", parsed.namespace());
        assertEquals(1, parsed.molecule().atoms().size());
        assertEquals(0, parsed.molecule().bonds().size());

        assertEquals("U", parsed.molecule().atoms().get(0).element());
        assertEquals(238, parsed.molecule().atoms().get(0).isotope());
        assertEquals(0, parsed.molecule().atoms().get(0).charge());
    }

    @Test
    void parsesRing() {
        ParsedMolecule parsed = NownsParser.parse("C1CCCCC1");

        assertEquals(6, parsed.molecule().atoms().size());
        assertEquals(6, parsed.molecule().bonds().size());

        for (int i = 0; i < 6; i++) {
            assertEquals("C", parsed.molecule().atoms().get(i).element());
            assertEquals(BondType.SINGLE, parsed.molecule().bonds().get(i).type());
        }
    }

    @Test
    void parsesAromaticAtoms() {
        ParsedMolecule parsed = NownsParser.parse("c1ccccc1");

        assertEquals(6, parsed.molecule().atoms().size());
        assertEquals(6, parsed.molecule().bonds().size());

        for (int i = 0; i < 6; i++) {
            assertEquals("C", parsed.molecule().atoms().get(i).element());
            assertTrue(parsed.molecule().atoms().get(i).aromatic());
        }
    }

    @Test
    void parsesBranch() {
        ParsedMolecule parsed = NownsParser.parse("CC(O)C");

        assertEquals(4, parsed.molecule().atoms().size());
        assertEquals(3, parsed.molecule().bonds().size());

        assertEquals("C", parsed.molecule().atoms().get(0).element());
        assertEquals("C", parsed.molecule().atoms().get(1).element());
        assertEquals("O", parsed.molecule().atoms().get(2).element());
        assertEquals("C", parsed.molecule().atoms().get(3).element());
    }

    @Test
    void parsesBracketedCentralAtom() {
        ParsedMolecule parsed = NownsParser.parse("F[U](F)(F)(F)(F)F");

        assertEquals(7, parsed.molecule().atoms().size());
        assertEquals(6, parsed.molecule().bonds().size());

        assertEquals("F", parsed.molecule().atoms().get(0).element());
        assertEquals("U", parsed.molecule().atoms().get(1).element());

        for (int i = 2; i < 7; i++) {
            assertEquals("F", parsed.molecule().atoms().get(i).element());
        }
    }

    @Test
    void parsesDoubleBond() {
        ParsedMolecule parsed = NownsParser.parse("C=C");

        assertEquals(2, parsed.molecule().atoms().size());
        assertEquals(1, parsed.molecule().bonds().size());
        assertEquals(BondType.DOUBLE, parsed.molecule().bonds().get(0).type());
    }

    @Test
    void parsesTripleBond() {
        ParsedMolecule parsed = NownsParser.parse("C#N");

        assertEquals(2, parsed.molecule().atoms().size());
        assertEquals(1, parsed.molecule().bonds().size());
        assertEquals(BondType.TRIPLE, parsed.molecule().bonds().get(0).type());
    }

    @Test
    void parsesExplicitAromaticBond() {
        ParsedMolecule parsed = NownsParser.parse("C~C");

        assertEquals(2, parsed.molecule().atoms().size());
        assertEquals(1, parsed.molecule().bonds().size());
        assertEquals(BondType.AROMATIC, parsed.molecule().bonds().get(0).type());
    }

    @Test
    void parsesMultipleDisconnectedComponents() {
        ParsedMolecule parsed = NownsParser.parse("[Ca^+2].[Cl^-1].[Cl^-1]");

        assertEquals(3, parsed.molecule().atoms().size());
        assertEquals(0, parsed.molecule().bonds().size());

        assertEquals("Ca", parsed.molecule().atoms().get(0).element());
        assertEquals(2, parsed.molecule().atoms().get(0).charge());

        assertEquals("Cl", parsed.molecule().atoms().get(1).element());
        assertEquals(-1, parsed.molecule().atoms().get(1).charge());

        assertEquals("Cl", parsed.molecule().atoms().get(2).element());
        assertEquals(-1, parsed.molecule().atoms().get(2).charge());
    }

    @Test
    void parsesCustomNamespaceWithIsotope() {
        ParsedMolecule parsed = NownsParser.parse("test:[U:238]");

        assertEquals("test", parsed.namespace());
        assertEquals(1, parsed.molecule().atoms().size());
        assertEquals("U", parsed.molecule().atoms().get(0).element());
        assertEquals(238, parsed.molecule().atoms().get(0).isotope());
    }

    @Test
    void parsesCarboxylLikeBranching() {
        ParsedMolecule parsed = NownsParser.parse("C(=O)O");

        assertEquals(3, parsed.molecule().atoms().size());
        assertEquals(2, parsed.molecule().bonds().size());

        assertEquals(BondType.DOUBLE, parsed.molecule().bonds().get(0).type());
        assertEquals(BondType.SINGLE, parsed.molecule().bonds().get(1).type());
    }

    @Test
    void rejectsChargeWithoutMagnitude() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("[Na^+]")
        );

        assertEquals("Charge magnitude must be explicit in [Na^+]", ex.getMessage());
    }

    @Test
    void rejectsMultiletterElementWithoutBrackets() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("Na")
        );

        assertEquals("Multi-letter elements must use brackets at index 0: [Na]", ex.getMessage());
    }

    @Test
    void rejectsMissingIsotopeValue() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("[U:]")
        );

        assertEquals("Missing isotope value in bracket atom: [U:]", ex.getMessage());
    }

    @Test
    void rejectsEmptyBracketAtom() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("[]")
        );

        assertEquals("Bracket atom cannot be empty", ex.getMessage());
    }

    @Test
    void rejectsBranchBeforeAnyAtom() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("(")
        );

        assertEquals("Branch start '(' cannot appear before any atom", ex.getMessage());
    }

    @Test
    void rejectsUnmatchedClosingBranch() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse(")")
        );

        assertEquals("Unmatched ')'", ex.getMessage());
    }

    @Test
    void rejectsUnclosedRing() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("C1CC")
        );

        assertTrue(ex.getMessage().startsWith("Unclosed ring closure(s):"));
    }

    @Test
    void rejectsUnclosedBracketAtom() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("[Na^1")
        );

        assertEquals("Unclosed bracket atom starting at index 0", ex.getMessage());
    }

    @Test
    void rejectsMissingChargeAfterCaret() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("[Na^]")
        );

        assertEquals("Missing charge after '^' in [Na^]", ex.getMessage());
    }

    @Test
    void rejectsEmptyBody() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("nuclide:")
        );

        assertEquals("NOWNS body cannot be empty", ex.getMessage());
    }

    @Test
    void rejectsEmptyNamespace() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse(":CCO")
        );

        assertEquals("Namespace cannot be empty", ex.getMessage());
    }

    @Test
    void rejectsLeadingBondOperator() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("=CC")
        );

        assertEquals("Bond operator '=' cannot appear before an atom", ex.getMessage());
    }

    @Test
    void rejectsTrailingBondOperator() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("CC=")
        );

        assertEquals("NOWNS string cannot end with a bond operator", ex.getMessage());
    }

    @Test
    void rejectsLeadingDisconnectedSeparator() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse(".CC")
        );

        assertEquals("Disconnected separator '.' cannot appear here", ex.getMessage());
    }

    @Test
    void rejectsTrailingDisconnectedSeparator() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("CC.")
        );

        assertEquals("NOWNS string cannot end with '.'", ex.getMessage());
    }

    @Test
    void rejectsRepeatedDisconnectedSeparator() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("C..C")
        );

        assertEquals("Disconnected separator '.' cannot appear here", ex.getMessage());
    }

    @Test
    void rejectsBranchEndingAfterBondOperator() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("C(=)")
        );

        assertEquals("Branch cannot end immediately after a bond operator", ex.getMessage());
    }

    @Test
    void rejectsMultipleConsecutiveBondOperators_hashAfterEquals() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("C=#N")
        );

        assertEquals("Multiple consecutive bond operators are not allowed", ex.getMessage());
    }

    @Test
    void rejectsMultipleConsecutiveBondOperators_doubleAromatic() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("C~~C")
        );

        assertEquals("Multiple consecutive bond operators are not allowed", ex.getMessage());
    }

    @Test
    void rejectsBranchAtBeginningEvenWithBracketAtomInside() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("([O])")
        );

        assertEquals("Branch start '(' cannot appear before any atom", ex.getMessage());
    }

    @Test
    void rejectsEmptyBranch() {
        NownsParseException ex = assertThrows(
                NownsParseException.class,
                () -> NownsParser.parse("C()")
        );

        assertEquals("Empty branch is not allowed", ex.getMessage());
    }
}