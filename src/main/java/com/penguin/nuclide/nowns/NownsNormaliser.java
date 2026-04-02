package com.penguin.nuclide.nowns;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.penguin.nuclide.atomic.Atom;
import com.penguin.nuclide.atomic.Bond;
import com.penguin.nuclide.atomic.Molecule;

public final class NownsNormaliser {

    private NownsNormaliser() {}

    public static String normalise(String input) {
        return normalise(NownsParser.parse(input));
    }

    public static String normalise(ParsedMolecule parsed) {
        Molecule molecule = parsed.molecule();
        List<ComponentData> components = new ArrayList<>();

        for (List<Integer> component : findConnectedComponents(molecule)) {
            List<Integer> signature = buildAtomicNumberSignature(molecule, component);
            String rendered = renderComponentV1(molecule, component);

            components.add(new ComponentData(component, signature, rendered));
        }

        components.sort(COMPONENT_COMPARATOR);

        List<String> rendered = new ArrayList<>();
        for (ComponentData component : components) {
            rendered.add(component.rendered());
        }

        return parsed.namespace() + ":" + String.join(".", rendered);
    }

    private static final Comparator<ComponentData> COMPONENT_COMPARATOR = (a, b) -> {
        int sigCompare = compareSignatureDescending(a.signature(), b.signature());
        if (sigCompare != 0) return sigCompare;

        int sizeCompare = Integer.compare(b.componentAtomIndices().size(), a.componentAtomIndices().size());
        if (sizeCompare != 0) return sizeCompare;

        return a.rendered().compareTo(b.rendered());
    };

    /**
     * Descending lexicographic comparison:
     * [92, 8] comes before [20]
     * [8, 6] comes before [7, 6]
     * [6, 6] comes before [6]
     */
    private static int compareSignatureDescending(List<Integer> a, List<Integer> b) {
        int min = Math.min(a.size(), b.size());

        for (int i = 0; i < min; i++) {
            int cmp = Integer.compare(b.get(i), a.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }

        return Integer.compare(b.size(), a.size());
    }

    private static List<List<Integer>> findConnectedComponents(Molecule molecule) {
        int atomCount = molecule.atoms().size();

        List<List<Integer>> adjacency = new ArrayList<>();
        for (int i = 0; i < atomCount; i++) {
            adjacency.add(new ArrayList<>());
        }

        for (Bond bond : molecule.bonds()) {
            adjacency.get(bond.a()).add(bond.b());
            adjacency.get(bond.b()).add(bond.a());
        }

        boolean[] visited = new boolean[atomCount];
        List<List<Integer>> components = new ArrayList<>();

        for (int start = 0; start < atomCount; start++) {
            if (visited[start]) continue;

            List<Integer> component = new ArrayList<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited[start] = true;

            while (!stack.isEmpty()) {
                int current = stack.pop();
                component.add(current);

                for (int next : adjacency.get(current)) {
                    if (!visited[next]) {
                        visited[next] = true;
                        stack.push(next);
                    }
                }
            }

            Collections.sort(component);
            components.add(component);
        }

        return components;
    }

    private static List<Integer> buildAtomicNumberSignature(Molecule molecule, List<Integer> component) {
        List<Integer> signature = new ArrayList<>();

        for (int atomIndex : component) {
            Atom atom = molecule.atoms().get(atomIndex);
            signature.add(atomicNumber(atom.element()));
        }

        signature.sort(Comparator.reverseOrder());
        return signature;
    }

    /**
     * v1 renderer:
     * - single-atom components render directly
     * - connected components are rendered by deterministic DFS from the lowest atom index
     * - branch ordering is by lowest child atom index
     *
     * This is stable, but NOT full canonical graph rendering.
     */
    private static String renderComponentV1(Molecule molecule, List<Integer> component) {
        if (component.isEmpty()) {
            throw new IllegalStateException("Component cannot be empty");
        }

        if (component.size() == 1) {
            return formatAtom(molecule.atoms().get(component.get(0)));
        }

        Set<Integer> allowed = new HashSet<>(component);
        int root = component.get(0);

        StringBuilder sb = new StringBuilder();
        Set<Integer> visited = new HashSet<>();

        renderDepthFirst(molecule, root, -1, allowed, visited, sb);

        if (visited.size() != component.size()) {
            // Rings and revisits are not fully re-rendered canonically in v1.
            // We fail loudly rather than emit misleading nonsense.
            throw new UnsupportedOperationException(
                    "NOWNS normalisation v1 does not yet support canonical rendering of all cyclic connected components"
            );
        }

        return sb.toString();
    }

    private static void renderDepthFirst(
            Molecule molecule,
            int current,
            int parent,
            Set<Integer> allowed,
            Set<Integer> visited,
            StringBuilder sb
    ) {
        visited.add(current);
        sb.append(formatAtom(molecule.atoms().get(current)));

        List<NeighborEdge> children = new ArrayList<>();
        for (Bond bond : molecule.bonds()) {
            int next = -1;

            if (bond.a() == current && allowed.contains(bond.b())) {
                next = bond.b();
            } else if (bond.b() == current && allowed.contains(bond.a())) {
                next = bond.a();
            }

            if (next == -1 || next == parent || visited.contains(next)) {
                continue;
            }

            children.add(new NeighborEdge(next, bond));
        }

        children.sort(Comparator.comparingInt(NeighborEdge::atomIndex));

        boolean first = true;
        for (NeighborEdge child : children) {
            if (first) {
                sb.append(formatBond(child.bond()));
                renderDepthFirst(molecule, child.atomIndex(), current, allowed, visited, sb);
                first = false;
            } else {
                sb.append('(');
                sb.append(formatBond(child.bond()));
                renderDepthFirst(molecule, child.atomIndex(), current, allowed, visited, sb);
                sb.append(')');
            }
        }
    }

    private static String formatBond(Bond bond) {
        return switch (bond.type()) {
            case SINGLE -> "";
            case DOUBLE -> "=";
            case TRIPLE -> "#";
            case AROMATIC -> "~";
        };
    }

    private static String formatAtom(Atom atom) {
        boolean singleLetter = atom.element().length() == 1;
        boolean neutral = atom.charge() == 0;
        boolean noIsotope = atom.isotope() == -1;

        if (singleLetter && neutral && noIsotope) {
            if (atom.aromatic()) {
                return atom.element().toLowerCase(Locale.ROOT);
            }
            return atom.element();
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');

        if (atom.aromatic() && atom.element().length() == 1) {
            sb.append(atom.element().toLowerCase(Locale.ROOT));
        } else {
            sb.append(atom.element());
        }

        if (atom.isotope() != -1) {
            sb.append(':').append(atom.isotope());
        }

        if (atom.charge() != 0) {
            sb.append('^');
            if (atom.charge() > 0) {
                sb.append('+').append(atom.charge());
            } else {
                sb.append('-').append(Math.abs(atom.charge()));
            }
        }

        sb.append(']');
        return sb.toString();
    }

    private static int atomicNumber(String element) {
        Integer value = ATOMIC_NUMBERS.get(element);
        if (value == null) {
            throw new IllegalArgumentException("Unknown element for normalization: " + element);
        }
        return value;
    }

    private static final Map<String, Integer> ATOMIC_NUMBERS = createAtomicNumbers();

    private static Map<String, Integer> createAtomicNumbers() {
        Map<String, Integer> map = new HashMap<>();

        // Full table is better long-term. This is the full set for safety.
        put(map, 1, "H");
        put(map, 2, "He");
        put(map, 3, "Li");
        put(map, 4, "Be");
        put(map, 5, "B");
        put(map, 6, "C");
        put(map, 7, "N");
        put(map, 8, "O");
        put(map, 9, "F");
        put(map, 10, "Ne");
        put(map, 11, "Na");
        put(map, 12, "Mg");
        put(map, 13, "Al");
        put(map, 14, "Si");
        put(map, 15, "P");
        put(map, 16, "S");
        put(map, 17, "Cl");
        put(map, 18, "Ar");
        put(map, 19, "K");
        put(map, 20, "Ca");
        put(map, 21, "Sc");
        put(map, 22, "Ti");
        put(map, 23, "V");
        put(map, 24, "Cr");
        put(map, 25, "Mn");
        put(map, 26, "Fe");
        put(map, 27, "Co");
        put(map, 28, "Ni");
        put(map, 29, "Cu");
        put(map, 30, "Zn");
        put(map, 31, "Ga");
        put(map, 32, "Ge");
        put(map, 33, "As");
        put(map, 34, "Se");
        put(map, 35, "Br");
        put(map, 36, "Kr");
        put(map, 37, "Rb");
        put(map, 38, "Sr");
        put(map, 39, "Y");
        put(map, 40, "Zr");
        put(map, 41, "Nb");
        put(map, 42, "Mo");
        put(map, 43, "Tc");
        put(map, 44, "Ru");
        put(map, 45, "Rh");
        put(map, 46, "Pd");
        put(map, 47, "Ag");
        put(map, 48, "Cd");
        put(map, 49, "In");
        put(map, 50, "Sn");
        put(map, 51, "Sb");
        put(map, 52, "Te");
        put(map, 53, "I");
        put(map, 54, "Xe");
        put(map, 55, "Cs");
        put(map, 56, "Ba");
        put(map, 57, "La");
        put(map, 58, "Ce");
        put(map, 59, "Pr");
        put(map, 60, "Nd");
        put(map, 61, "Pm");
        put(map, 62, "Sm");
        put(map, 63, "Eu");
        put(map, 64, "Gd");
        put(map, 65, "Tb");
        put(map, 66, "Dy");
        put(map, 67, "Ho");
        put(map, 68, "Er");
        put(map, 69, "Tm");
        put(map, 70, "Yb");
        put(map, 71, "Lu");
        put(map, 72, "Hf");
        put(map, 73, "Ta");
        put(map, 74, "W");
        put(map, 75, "Re");
        put(map, 76, "Os");
        put(map, 77, "Ir");
        put(map, 78, "Pt");
        put(map, 79, "Au");
        put(map, 80, "Hg");
        put(map, 81, "Tl");
        put(map, 82, "Pb");
        put(map, 83, "Bi");
        put(map, 84, "Po");
        put(map, 85, "At");
        put(map, 86, "Rn");
        put(map, 87, "Fr");
        put(map, 88, "Ra");
        put(map, 89, "Ac");
        put(map, 90, "Th");
        put(map, 91, "Pa");
        put(map, 92, "U");
        put(map, 93, "Np");
        put(map, 94, "Pu");
        put(map, 95, "Am");
        put(map, 96, "Cm");
        put(map, 97, "Bk");
        put(map, 98, "Cf");
        put(map, 99, "Es");
        put(map, 100, "Fm");
        put(map, 101, "Md");
        put(map, 102, "No");
        put(map, 103, "Lr");
        put(map, 104, "Rf");
        put(map, 105, "Db");
        put(map, 106, "Sg");
        put(map, 107, "Bh");
        put(map, 108, "Hs");
        put(map, 109, "Mt");
        put(map, 110, "Ds");
        put(map, 111, "Rg");
        put(map, 112, "Cn");
        put(map, 113, "Nh");
        put(map, 114, "Fl");
        put(map, 115, "Mc");
        put(map, 116, "Lv");
        put(map, 117, "Ts");
        put(map, 118, "Og");

        return Collections.unmodifiableMap(map);
    }

    private static void put(Map<String, Integer> map, int atomicNumber, String symbol) {
        map.put(symbol, atomicNumber);
    }

    private record NeighborEdge(int atomIndex, Bond bond) {}
    private record ComponentData(List<Integer> componentAtomIndices, List<Integer> signature, String rendered) {}
}