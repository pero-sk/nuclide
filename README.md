# Nuclide

This mod adds atomic chemistry and simulation to Minecraft. It mainly focuses on atoms but also implements EM waves.

# NOWNS

## Acronym

NOWNS stands for Nuclide Open Way to Notate Species

## What is it?

NOWNS is a SMILES-derived notation  with extensions for namespaces, isotope tagging, and charge representation.

NOWNS follows standard SMILES syntax and semantics unless explicitly overridden by NOWNS additions.

Also unlike standard SMILES, NOWNS does not assume implicit hydrogens; all atoms must be written explicitly.

NOWNS also takes heavy inspiration from FROWNS ([check out Destroy's FROWNS here](https://github.com/Petrolpark-Mods/Destroy/wiki/FROWNS))

## Structure

(Charge is not represented using standard SMILES syntax; check [charge](#charge))

First, it follows the main SMILES structure.

Example:
- Sodium Chloride: `[Na^+1].[Cl^-1]`
- Ethanol: `CCO`

In NOWNS everything is a molecule, even singular atoms:
- `nuclide:[He]`
- `nuclide:O`

Single-letter elements may omit brackets; multi-letter elements must use brackets.

## Rings

Ring closures use matching digits:

Cyclohexane:
C1CCCCC1

Cyclopropane:
C1CC1

Note: ring normalisation is v1 and may produce non-canonical output for complex polycyclic structures.

## Additions

### Namespace

NOWNS adds an optional namespace prefix:

- `[Na^+1].[Cl^-1]`
- `nuclide:[Na^+1].[Cl^-1]`

Unnamespaced molecules default to the `nuclide` namespace, so these are equivalent.

### Isotopes

NOWNS adds isotope tagging:

```text
[U:238]
```

### Charge

NOWNS uses a different syntax than SMILES:

```text
[Element^±charge]
```

Examples:

- [Na^+1] 
- [Cl^-1]

Rules:

- Charge must include a sign (+ or -)
- Magnitude must be explicit (+1, -1, not just + or -)

### Aromatic atoms

Aromatic atoms use lowercase single-letter symbols:

- `c` carbon (aromatic)
- `n` nitrogen (aromatic)
- `o` oxygen (aromatic)
- `s` sulfur (aromatic)
- `b` boron (aromatic)
- `p` phosphorus (aromatic)

Aromatic bonds use `~`.

Benzene: `c1ccccc1`
Pyridine: `c1ccncc1`

Multi-letter aromatic elements must use brackets: `[se]`, `[te]`

### Limitations

- Implicit hydrogens: not supported. All atoms must be explicit.
- Fractional charges: not supported. Charges must be non-zero integers.
- Multi-digit ring closures (%10, %11): not supported. Use digits 1-9 only.
- Stereochemistry (@, @@, /, \\): reserved for a future version.
- Full canonical graph normalisation for cyclic structures: v1 produces
  deterministic but not fully canonical output for rings.

# NOWNS normalisation

After a NOWNS string has been parsed, it is normalised. For datapack authors, you don't need to care about normalisation as this section is for modders.

### Scope

These rules apply only to disconnected components and do not reorder atoms within a connected component.

NOWNS normalisation v1 guarantees deterministic ordering of disconnected components (order-independent input → stable output), but does not yet provide full canonicalisation of connected molecular graphs.

### Disconnected component ordering

Disconnected components are sorted by:

- descending atomic-number signature
- Signatures are compared lexicographically (left-to-right).
- - [8, 6] > [6, 6] because 8 > 6 at index 0
- then descending component size
- then rendered string (canonical NOWNS string of the component) ascending

### Atomic-number signature

Atomic number is used for signature calculation only.

A component’s atomic-number signature is:
- the list of atomic numbers of its atoms
- sorted in descending order

Isotopes do not affect atomic-number signature (only element identity is used).

Examples:

```text
[Ca^+2]   -> [20]
[Cl^-1]   -> [17]
[U:238]O  -> [92, 8]
```

Ordering example:

```text
[U:238]O, [Ca^+2], [Cl^-1]
```

### Tie breaking

If two components have the same signature:

- larger component first
- then alphabetical string comparison for stability

### Signature tie-break examples

#### - Example 1

CC.C

Components:
```text
CC -> [6, 6]
C  -> [6]
```

Result:
```text
CC.C
```

#### - Example 2

CC.CC.C

Components:
```text
CC -> [6, 6]
CC -> [6, 6]
C  -> [6]
```

[6, 6] > [6], between the two CC components:
- same signature
- same size
- same string

Result:
```text
CC.CC.C
```

#### - Example 3

CO.OC

Components:
```text
CO -> [8, 6]
OC -> [8, 6]
```

Same signature and size -> alphabetical string comparison:
```text
"CO" < "OC"
```

Result:
```text
CO.OC
```

#### - Example 4

CN.CO

Components:
```text
CN -> [7, 6]
CO -> [8, 6]
```

Comparison:
```text
[8, 6] > [7, 6]
```

Result:
```text
CO.CN
```

# Tags

Nuclide provides species tags so reactions can refer to groups of species instead of only exact species ids.

Tags make reactions more flexible and extensible, especially for addon content.

Unlike most other Nuclide data, tag files do not need to live only under `data/nuclide/`. Nuclide collects tag definitions from:

data/\<namespace\>/tags/species/

for all available namespaces.

### How are tags identified?

A tag’s identity is defined by its `id` field inside the JSON, not by its file path.

The file path only determines that the file will be discovered as a tag definition.

Example:

data/myaddon/tags/species/carbon_any.json

could define either:

- `myaddon:carbon_any`
- or `nuclide:carbon_any`

depending on the JSON contents.

### Tag file format

A tag file contains:

- an `id` field for the tag id
- a `values` array of species ids

Example:

```json
{
  "id": "nuclide:carbon_any",
  "values": [
    "atoms:carbon",
    "atoms:carbon_14"
  ]
}
```

### Which namespace should I use?

If you want a tag that is specific to your addon, use your addon’s namespace.

Example:

- `myaddon:custom_fuel`

If you want to contribute to a shared Nuclide tag that other addons can also use, use the `nuclide:` namespace.

Example:

- `nuclide:carbon_any`

This means an addon may define tags under its own `data/<addon>/tags/species/` path while still contributing to a shared `nuclide:*` tag, as long as the JSON `id` uses the `nuclide:` namespace.

### How do I add onto a built-in tag?

Nuclide does not treat any single tag file as the one source of truth for a tag.

Instead, tag definitions are merged by tag id.

That means if your addon provides a tag file whose `id` matches a built-in Nuclide tag id, both sets of values are combined.

For example, if Nuclide defines:

```json
{
  "id": "nuclide:carbon_any",
  "values": [
    "atoms:carbon",
    "atoms:carbon_14"
  ]
}
```

and your addon defines:

```json
{
  "id": "nuclide:carbon_any",
  "values": [
    "atoms:carbon_13"
  ]
}
```

then the final `nuclide:carbon_any` tag will contain the union of all listed species.

Any reaction using that tag will accept all contributed species.

