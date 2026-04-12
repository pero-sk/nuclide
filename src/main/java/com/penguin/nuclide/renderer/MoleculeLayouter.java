package com.penguin.nuclide.renderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.penguin.nuclide.atomic.Atom;
import com.penguin.nuclide.atomic.Bond;
import com.penguin.nuclide.atomic.BondType;
import com.penguin.nuclide.atomic.Molecule;

public final class MoleculeLayouter {

    private static final float BOND_LENGTH = 24.0f;
    private static final float COMPONENT_SPACING = 48.0f;

    private MoleculeLayouter() {}

    public static MoleculeLayout layout(Molecule molecule) {
        MoleculeLayout layout = new MoleculeLayout();

        List<Atom> atoms = molecule.atoms();
        List<Bond> bonds = molecule.bonds();

        if (atoms.isEmpty()) {
            return layout;
        }

        List<List<BondConnection>> adjacency = buildAdjacency(atoms.size(), bonds);

        boolean[] placed = new boolean[atoms.size()];
        float[] xs = new float[atoms.size()];
        float[] ys = new float[atoms.size()];

        float componentOffsetX = 0.0f;

        for (int root = 0; root < atoms.size(); root++) {
            if (placed[root]) {
                continue;
            }

            List<Integer> componentAtoms = placeComponent(root, componentOffsetX, adjacency, placed, xs, ys);

            float maxX = componentOffsetX;
            for (int atomIndex : componentAtoms) {
                maxX = Math.max(maxX, xs[atomIndex]);
            }

            componentOffsetX = maxX + COMPONENT_SPACING;
        }

        for (int i = 0; i < atoms.size(); i++) {
            Atom atom = atoms.get(i);
            layout.addAtom(new MoleculeLayout.LayoutAtom(
                    i,
                    atom.element(),
                    xs[i],
                    ys[i]
            ));
        }

        for (Bond bond : bonds) {
            layout.addBond(new MoleculeLayout.LayoutBond(
                    bond.a(),
                    bond.b(),
                    bondOrderOf(bond.type())
            ));
        }

        centerLayout(layout);
        return layout;
    }

    private static List<Integer> placeComponent(
            int root,
            float startX,
            List<List<BondConnection>> adjacency,
            boolean[] placed,
            float[] xs,
            float[] ys
    ) {
        List<Integer> componentAtoms = new ArrayList<>();
        Deque<NodePlacement> queue = new ArrayDeque<>();
        queue.add(new NodePlacement(root, -1, startX, 0.0f, 0));

        while (!queue.isEmpty()) {
            NodePlacement current = queue.removeFirst();

            if (placed[current.atomIndex]) {
                continue;
            }

            placed[current.atomIndex] = true;
            xs[current.atomIndex] = current.x;
            ys[current.atomIndex] = current.y;
            componentAtoms.add(current.atomIndex);

            List<BondConnection> neighbors = adjacency.get(current.atomIndex);
            List<BondConnection> unplacedNeighbors = new ArrayList<>();

            for (BondConnection neighbor : neighbors) {
                if (!placed[neighbor.otherAtomIndex]) {
                    unplacedNeighbors.add(neighbor);
                }
            }

            float[] angles = chooseAngles(unplacedNeighbors.size());

            for (int i = 0; i < unplacedNeighbors.size(); i++) {
                BondConnection neighbor = unplacedNeighbors.get(i);
                float angle = angles[i];

                float nextX = current.x + (float) Math.cos(angle) * BOND_LENGTH;
                float nextY = current.y + (float) Math.sin(angle) * BOND_LENGTH;

                queue.addLast(new NodePlacement(
                        neighbor.otherAtomIndex,
                        current.atomIndex,
                        nextX,
                        nextY,
                        current.depth + 1
                ));
            }
        }

        return componentAtoms;
    }

    private static float[] chooseAngles(int count) {
        if (count <= 0) {
            return new float[0];
        }

        if (count == 1) {
            return new float[] { 0.0f };
        }

        if (count == 2) {
            return new float[] {
                    (float) (-Math.PI / 4.0),
                    (float) (Math.PI / 4.0)
            };
        }

        if (count == 3) {
            return new float[] {
                    (float) (-Math.PI / 3.0),
                    0.0f,
                    (float) (Math.PI / 3.0)
            };
        }

        float[] angles = new float[count];
        float spread = (float) Math.PI;
        float start = -spread / 2.0f;

        for (int i = 0; i < count; i++) {
            angles[i] = start + (spread * i / Math.max(1, count - 1));
        }

        return angles;
    }

    private static List<List<BondConnection>> buildAdjacency(int atomCount, List<Bond> bonds) {
        List<List<BondConnection>> adjacency = new ArrayList<>();

        for (int i = 0; i < atomCount; i++) {
            adjacency.add(new ArrayList<>());
        }

        for (Bond bond : bonds) {
            int a = bond.a();
            int b = bond.b();

            if (a < 0 || a >= atomCount || b < 0 || b >= atomCount) {
                continue;
            }

            int order = bondOrderOf(bond.type());

            adjacency.get(a).add(new BondConnection(b, order));
            adjacency.get(b).add(new BondConnection(a, order));
        }

        return adjacency;
    }

    private static int bondOrderOf(BondType type) {
        return switch (type) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case AROMATIC -> 1;
        };
    }

    private static void centerLayout(MoleculeLayout layout) {
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

        float offsetX = (minX + maxX) / 2.0f;
        float offsetY = (minY + maxY) / 2.0f;

        List<MoleculeLayout.LayoutAtom> centeredAtoms = new ArrayList<>();
        for (MoleculeLayout.LayoutAtom atom : layout.atoms()) {
            centeredAtoms.add(new MoleculeLayout.LayoutAtom(
                    atom.atomIndex(),
                    atom.symbol(),
                    atom.x() - offsetX,
                    atom.y() - offsetY
            ));
        }

        layout.atoms().clear();
        layout.atoms().addAll(centeredAtoms);
    }

    private record BondConnection(int otherAtomIndex, int order) {}

    private record NodePlacement(
            int atomIndex,
            int parentAtomIndex,
            float x,
            float y,
            int depth
    ) {}
}