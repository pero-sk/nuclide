package com.penguin.nuclide.nowns;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.penguin.nuclide.atomic.Atom;
import com.penguin.nuclide.atomic.Bond;
import com.penguin.nuclide.atomic.BondType;
import com.penguin.nuclide.atomic.Molecule;

record BranchFrame(int parentAtomIndex, int atomCountAtOpen) {}

public final class NownsParser {

    private NownsParser() {}

    public static ParsedMolecule parse(String input) {
        if (input == null || input.isBlank()) {
            throw new NownsParseException("NOWNS string cannot be null or blank");
        }

        NamespaceAndBody split = splitNamespace(input);
        String namespace = split.namespace();
        String body = split.body();

        Molecule molecule = parseBody(body);

        return new ParsedMolecule(namespace, molecule);
    }

    private static NamespaceAndBody splitNamespace(String input) {
        boolean inBracket = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '[') {
                inBracket = true;
            } else if (c == ']') {
                inBracket = false;
            } else if (c == ':' && !inBracket) {
                String namespace = input.substring(0, i);
                String body = input.substring(i + 1);

                if (namespace.isEmpty()) {
                    throw new NownsParseException("Namespace cannot be empty");
                }
                if (body.isEmpty()) {
                    throw new NownsParseException("NOWNS body cannot be empty");
                }

                return new NamespaceAndBody(namespace, body);
            }
        }

        return new NamespaceAndBody("nuclide", input);
    }

    private static Molecule parseBody(String body) {
        Molecule molecule = new Molecule();

        Deque<BranchFrame> branchStack = new ArrayDeque<>();
        Map<Integer, RingAnchor> ringClosures = new HashMap<>();

        int currentAtomIndex = -1;
        BondType pendingBond = BondType.SINGLE;
        boolean lastWasDot = false;

        int i = 0;
        while (i < body.length()) {
            char c = body.charAt(i);

            switch (c) {
                case '(' -> {
                    requireCurrentAtom(currentAtomIndex, "Branch start '(' cannot appear before any atom");
                    if (pendingBond != BondType.SINGLE) {
                        throw new NownsParseException("Branch cannot begin immediately after a bond operator");
                    }
                    branchStack.push(new BranchFrame(currentAtomIndex, molecule.atoms().size()));
                    lastWasDot = false;
                    i++;
                }

                case ')' -> {
                    if (branchStack.isEmpty()) {
                        throw new NownsParseException("Unmatched ')'");
                    }
                    if (pendingBond != BondType.SINGLE) {
                        throw new NownsParseException("Branch cannot end immediately after a bond operator");
                    }

                    BranchFrame frame = branchStack.pop();
                    if (molecule.atoms().size() == frame.atomCountAtOpen()) {
                        throw new NownsParseException("Empty branch is not allowed");
                    }

                    currentAtomIndex = frame.parentAtomIndex();
                    lastWasDot = false;
                    i++;
                }

                case '=' -> {
                    if (currentAtomIndex == -1) {
                        throw new NownsParseException("Bond operator '=' cannot appear before an atom");
                    }
                    if (pendingBond != BondType.SINGLE) {
                        throw new NownsParseException("Multiple consecutive bond operators are not allowed");
                    }
                    pendingBond = BondType.DOUBLE;
                    lastWasDot = false;
                    i++;
                }

                case '#' -> {
                    if (currentAtomIndex == -1) {
                        throw new NownsParseException("Bond operator '#' cannot appear before an atom");
                    }
                    if (pendingBond != BondType.SINGLE) {
                        throw new NownsParseException("Multiple consecutive bond operators are not allowed");
                    }
                    pendingBond = BondType.TRIPLE;
                    lastWasDot = false;
                    i++;
                }

                case '~' -> {
                    if (currentAtomIndex == -1) {
                        throw new NownsParseException("Bond operator '~' cannot appear before an atom");
                    }
                    if (pendingBond != BondType.SINGLE) {
                        throw new NownsParseException("Multiple consecutive bond operators are not allowed");
                    }
                    pendingBond = BondType.AROMATIC;
                    lastWasDot = false;
                    i++;
                }

                case '.' -> {
                    if (currentAtomIndex == -1) {
                        throw new NownsParseException("Disconnected separator '.' cannot appear here");
                    }
                    if (pendingBond != BondType.SINGLE) {
                        throw new NownsParseException("Disconnected separator '.' cannot follow a bond operator");
                    }

                    currentAtomIndex = -1;
                    lastWasDot = true;
                    i++;
                }

                default -> {
                    if (Character.isDigit(c)) {
                        requireCurrentAtom(currentAtomIndex, "Ring closure digit cannot appear before any atom");

                        int ringNumber = c - '0';

                        if (!ringClosures.containsKey(ringNumber)) {
                            ringClosures.put(ringNumber, new RingAnchor(currentAtomIndex, pendingBond));
                        } else {
                            RingAnchor anchor = ringClosures.remove(ringNumber);
                            BondType ringBond = resolveRingBond(anchor.bondType(), pendingBond);
                            molecule.bonds().add(new Bond(anchor.atomIndex(), currentAtomIndex, ringBond));
                        }

                        pendingBond = BondType.SINGLE;
                        lastWasDot = false;
                        i++;
                    } else {
                        ParseAtomResult atomResult = parseAtomAt(body, i);
                        molecule.atoms().add(atomResult.atom());
                        int newAtomIndex = molecule.atoms().size() - 1;

                        if (currentAtomIndex != -1) {
                            molecule.bonds().add(new Bond(currentAtomIndex, newAtomIndex, pendingBond));
                        }

                        currentAtomIndex = newAtomIndex;
                        pendingBond = BondType.SINGLE;
                        lastWasDot = false;
                        i = atomResult.nextIndex();
                    }
                }
            }
        }

        if (!branchStack.isEmpty()) {
            throw new NownsParseException("Unclosed branch: missing ')'");
        }

        if (!ringClosures.isEmpty()) {
            throw new NownsParseException("Unclosed ring closure(s): " + ringClosures.keySet());
        }

        if (pendingBond != BondType.SINGLE) {
            throw new NownsParseException("NOWNS string cannot end with a bond operator");
        }

        if (lastWasDot) {
            throw new NownsParseException("NOWNS string cannot end with '.'");
        }

        return molecule;
    }

    private static void requireCurrentAtom(int currentAtomIndex, String message) {
        if (currentAtomIndex == -1) {
            throw new NownsParseException(message);
        }
    }

    private static BondType resolveRingBond(BondType first, BondType second) {
        if (first != BondType.SINGLE && second != BondType.SINGLE && first != second) {
            throw new NownsParseException("Conflicting ring bond types: " + first + " vs " + second);
        }

        if (second != BondType.SINGLE) {
            return second;
        }
        return first;
    }

    private static ParseAtomResult parseAtomAt(String s, int start) {
        char c = s.charAt(start);

        if (c == '[') {
            int end = s.indexOf(']', start);
            if (end == -1) {
                throw new NownsParseException("Unclosed bracket atom starting at index " + start);
            }

            String token = s.substring(start + 1, end);
            Atom atom = parseBracketAtom(token);

            return new ParseAtomResult(atom, end + 1);
        }

        if (isUppercaseElementStart(c)) {
            if (start + 1 < s.length() && Character.isLowerCase(s.charAt(start + 1))) {
                throw new NownsParseException(
                        "Multi-letter elements must use brackets at index " + start +
                        ": [" + c + s.charAt(start + 1) + "]"
                );
            }

            Atom atom = new Atom(String.valueOf(c), 0, -1, false);
            return new ParseAtomResult(atom, start + 1);
        }

        if (isAromaticLowercaseElement(c)) {
            String element = String.valueOf(Character.toUpperCase(c));
            Atom atom = new Atom(element, 0, -1, true);
            return new ParseAtomResult(atom, start + 1);
        }

        throw new NownsParseException("Unexpected character '" + c + "' at index " + start);
    }

    private static Atom parseBracketAtom(String token) {
        if (token.isBlank()) {
            throw new NownsParseException("Bracket atom cannot be empty");
        }

        int caretIndex = token.indexOf('^');
        String mainPart;
        String chargePart = null;

        if (caretIndex >= 0) {
            mainPart = token.substring(0, caretIndex);
            chargePart = token.substring(caretIndex + 1);

            if (token.indexOf('^', caretIndex + 1) >= 0) {
                throw new NownsParseException("Bracket atom contains multiple '^': [" + token + "]");
            }
        } else {
            mainPart = token;
        }

        String element;
        int isotope = -1;

        int isotopeColon = mainPart.indexOf(':');
        if (isotopeColon >= 0) {
            element = mainPart.substring(0, isotopeColon);
            String isotopeText = mainPart.substring(isotopeColon + 1);

            if (mainPart.indexOf(':', isotopeColon + 1) >= 0) {
                throw new NownsParseException("Bracket atom contains multiple ':' isotope separators: [" + token + "]");
            }
            if (isotopeText.isEmpty()) {
                throw new NownsParseException("Missing isotope value in bracket atom: [" + token + "]");
            }

            try {
                isotope = Integer.parseInt(isotopeText);
            } catch (NumberFormatException e) {
                throw new NownsParseException("Invalid isotope value '" + isotopeText + "' in [" + token + "]");
            }

            if (isotope <= 0) {
                throw new NownsParseException("Isotope must be positive in [" + token + "]");
            }
        } else {
            element = mainPart;
        }

        if (!isValidBracketElement(element)) {
            throw new NownsParseException("Invalid element symbol '" + element + "' in [" + token + "]");
        }

        int charge = 0;
        if (chargePart != null) {
            charge = parseCharge(chargePart, token);
        }

        boolean aromatic = element.length() == 1 && Character.isLowerCase(element.charAt(0));
        String normalizedElement =
                aromatic ? String.valueOf(Character.toUpperCase(element.charAt(0))) : normalizeElement(element);

        return new Atom(normalizedElement, charge, isotope, aromatic);
    }

    private static int parseCharge(String chargePart, String fullToken) {
        if (chargePart.isEmpty()) {
            throw new NownsParseException("Missing charge after '^' in [" + fullToken + "]");
        }

        char sign = chargePart.charAt(0);
        if (sign != '+' && sign != '-') {
            throw new NownsParseException("Charge must start with '+' or '-' in [" + fullToken + "]");
        }

        String magnitudeText = chargePart.substring(1);
        if (magnitudeText.isEmpty()) {
            throw new NownsParseException("Charge magnitude must be explicit in [" + fullToken + "]");
        }

        int magnitude;
        try {
            magnitude = Integer.parseInt(magnitudeText);
        } catch (NumberFormatException e) {
            throw new NownsParseException("Invalid charge magnitude '" + magnitudeText + "' in [" + fullToken + "]");
        }

        if (magnitude <= 0) {
            throw new NownsParseException("Charge magnitude must be positive in [" + fullToken + "]");
        }

        return sign == '+' ? magnitude : -magnitude;
    }

    private static boolean isUppercaseElementStart(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isAromaticLowercaseElement(char c) {
        return c == 'b' || c == 'c' || c == 'n' || c == 'o' || c == 'p' || c == 's';
    }

    private static boolean isValidBracketElement(String element) {
        if (element == null || element.isEmpty()) {
            return false;
        }

        if (element.length() == 1) {
            char c = element.charAt(0);
            return Character.isUpperCase(c) || isAromaticLowercaseElement(c);
        }

        if (element.length() == 2) {
            return Character.isUpperCase(element.charAt(0)) && Character.isLowerCase(element.charAt(1));
        }

        return false;
    }

    private static String normalizeElement(String element) {
        if (element.length() == 1) {
            return element;
        }
        return Character.toUpperCase(element.charAt(0)) + element.substring(1).toLowerCase(Locale.ROOT);
    }

    private record NamespaceAndBody(String namespace, String body) {}
    private record ParseAtomResult(Atom atom, int nextIndex) {}
    private record RingAnchor(int atomIndex, BondType bondType) {}
}