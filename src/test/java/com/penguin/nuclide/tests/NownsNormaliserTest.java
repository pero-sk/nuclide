package com.penguin.nuclide.tests;

import org.junit.jupiter.api.Test;

import com.penguin.nuclide.nowns.NownsNormaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NownsNormaliserTest {

    @Test
    void addsDefaultNamespace() {
        assertEquals("nuclide:CCO", NownsNormaliser.normalise("CCO"));
    }

    @Test
    void preservesExplicitNamespace() {
        assertEquals("test:[U:238]", NownsNormaliser.normalise("test:[U:238]"));
    }

    @Test
    void sortsDisconnectedIonsByAtomicNumberSignature() {
        assertEquals(
                "nuclide:[Cl^-1].[Na^+1]",
                NownsNormaliser.normalise("[Na^+1].[Cl^-1]")
        );

        assertEquals(
                "nuclide:[Cl^-1].[Na^+1]",
                NownsNormaliser.normalise("[Cl^-1].[Na^+1]")
        );
    }

    @Test
    void sortsDisconnectedComponentsBySignatureThenSizeThenString() {
        assertEquals("nuclide:CC.C", NownsNormaliser.normalise("CC.C"));
        assertEquals("nuclide:CC.CC.C", NownsNormaliser.normalise("CC.CC.C"));
        assertEquals("nuclide:CO.CN", NownsNormaliser.normalise("CN.CO"));
    }

    @Test
    void isotopesDoNotAffectAtomicNumberSignature() {
        assertEquals(
                "nuclide:[U:235].[U:238]",
                NownsNormaliser.normalise("[U:238].[U:235]")
        );
    }

    @Test
    void keepsConnectedComponentRenderingDeterministicForSimpleConnectedGraphs() {
        assertEquals("nuclide:C=O", NownsNormaliser.normalise("C=O"));
        assertEquals("nuclide:C=O(O)", NownsNormaliser.normalise("C(=O)O"));
        assertEquals("nuclide:C=OO", NownsNormaliser.normalise("C=O(O)"));
    }
}