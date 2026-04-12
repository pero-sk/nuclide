# Molecule JSON Guide

This guide explains how to create `.json` files for molecules in Nuclide.

It is focused on:

- where molecule files go
- what fields they should contain
- how `nowns` should be written
- which namespace to use in ids and NOWNS strings

---

# File location

Molecule files go in:

src/main/resources/data/nuclide/molecules/

Example:

src/main/resources/data/nuclide/molecules/water.json

Each file should define exactly one molecule.

---

# Basic molecule JSON structure

A minimal molecule file looks like this:

{
  "id": "molecules:water",
  "name": "Water",
  "nowns": "HOH",
  "molar_mass": 18.015
}

A more complete file can include additional properties:

{
  "id": "molecules:water",
  "name": "Water",
  "nowns": "HOH",
  "molar_mass": 18.015,
  "state": "liquid",
  "melting_point": 0.0,
  "boiling_point": 100.0,
  "radioactive": false,
  "toxic": false,
  "flammable": false
}

---

# Fields

## id

Type: string  
Required: yes

This is the unique registry id for the molecule or atom.

Examples:

"id": "molecules:water"
"id": "molecules:sodium_chloride"
"id": "atoms:uranium_238"

Rules:

- must be unique
- should be lowercase
- should use underscores for multi-word names
- should use `molecules:` for molecules
- should use `atoms:` for single atoms / atomic species

Good:

- `molecules:water`
- `molecules:sodium_chloride`
- `atoms:uranium_238`

Bad:

- `Water`
- `nuclide:water`
- `minecraft:water`
- `molecules:sodium chloride`

---

## name

Type: string  
Required: yes

Human-readable name of the molecule.

Example:

"name": "Water"

This does not have to be unique.

---

## nowns

Type: string  
Required: yes

The NOWNS string describing the molecule structure.

Example:

"nowns": "HOH"

Nuclide uses explicit NOWNS, which means:

- all atoms must be written explicitly
- implicit hydrogens are not used

So water is:

HOH

not:

O

---

## molar_mass

Type: number  
Required: yes

Example:

"molar_mass": 18.015

Use standard molar mass values.

---

## state

Type: string / enum  
Required: no

Example:

"state": "liquid"

Suggested values:

- `solid`
- `liquid`
- `gas`
- `plasma`

Use lowercase in JSON.

---

## melting_point

Type: number  
Required: no

Example:

"melting_point": 0.0

Store in degrees Celsius.

---

## boiling_point

Type: number  
Required: no

Example:

"boiling_point": 100.0

Store in degrees Celsius.

---

## radioactive

Type: boolean  
Required: no

Example:

"radioactive": false

---

## toxic

Type: boolean  
Required: no

Example:

"toxic": false

---

## flammable

Type: boolean  
Required: no

Example:

"flammable": false

---

# NOWNS rules for molecule JSON

Nuclide uses NOWNS as its molecular notation system.

Important rules:

- NOWNS is explicit
- no implicit hydrogens
- multi-letter elements must use brackets
- single-letter elements may omit brackets
- charges use `^`
- isotopes use `:`
- disconnected species use `.`

Examples:

Water:
HOH

Hydrogen gas:
[H][H]

Oxygen gas:
O=O

Sodium chloride:
[Na^+1].[Cl^-1]

Uranium-238 atom:
[U:238]

Calcium chloride:
NownsNorm[Ca^+2]([Cl^-1])[Cl^-1]aliser

---

# Namespace guide

This is the namespace rule set for Nuclide molecule JSON.

## 1. `molecules:` namespace

Use `molecules:` for molecule ids.

Examples:

- `molecules:water`
- `molecules:sodium_chloride`
- `molecules:oxygen_gas`
- `molecules:uranium_hexafluoride`

This should be the default namespace for normal molecular species.

---

## 2. `atoms:` namespace

Use `atoms:` for single atoms or atomic species.

Examples:

- `atoms:uranium_238`
- `atoms:oxygen`
- `atoms:carbon_14`

Use this when the JSON entry represents a single atom rather than a larger molecule.

Example:

{
  "id": "atoms:uranium_238",
  "name": "Uranium-238",
  "nowns": "[U:238]"
}

---

## 3. `nuclide:` namespace

Do not use `nuclide:` for molecule ids in this JSON system.

Avoid:

- `nuclide:water`
- `nuclide:sodium_chloride`
- `nuclide:uranium_238`

---

## 4. `minecraft:` namespace

Do not use `minecraft:` for molecule ids.

Avoid:

- `minecraft:water`
- `minecraft:oxygen`

`minecraft:` belongs to vanilla Minecraft registry content, not Nuclide molecule definitions.

---

# Practical namespace summary

Use this rule set:

- `molecules:` -> normal molecules
- `atoms:` -> single atoms / atomic species
- do not use `nuclide:` for molecule ids
- do not use `minecraft:` for molecule ids

If unsure:

- use `molecules:` for compounds and normal molecules
- use `atoms:` for single-atom entries

---

# Examples

## Water

{
  "id": "molecules:water",
  "name": "Water",
  "nowns": "HOH",
  "molar_mass": 18.015,
  "state": "liquid",
  "melting_point": 0.0,
  "boiling_point": 100.0,
  "radioactive": false,
  "toxic": false,
  "flammable": false
}

## Sodium chloride

{
  "id": "molecules:sodium_chloride",
  "name": "Sodium Chloride",
  "nowns": "[Na^+1].[Cl^-1]",
  "molar_mass": 58.44,
  "state": "solid",
  "melting_point": 801.0,
  "boiling_point": 1465.0,
  "radioactive": false,
  "toxic": false,
  "flammable": false
}

## Uranium-238 atom

{
  "id": "atoms:uranium_238",
  "name": "Uranium-238",
  "nowns": "[U:238]",
  "molar_mass": 238.05,
  "state": "solid",
  "melting_point": 1405.0,
  "boiling_point": 4404.0,
  "radioactive": true,
  "toxic": true,
  "flammable": false
}

---

# Common mistakes

## Wrong: using implicit hydrogens

{
  "id": "molecules:water",
  "name": "Water",
  "nowns": "O"
}

This is wrong for Nuclide because NOWNS is explicit.

Use:

"nowns": "HOH"

---

## Wrong: using `nuclide:` for ids

{
  "id": "nuclide:water",
  "name": "Water",
  "nowns": "HOH"
}

This is wrong for this system.

Use:

"id": "molecules:water"

---

## Wrong: using `minecraft:` for ids

{
  "id": "minecraft:water",
  "name": "Water",
  "nowns": "HOH"
}

This is wrong.

Use:

"id": "molecules:water"

---

## Wrong: using multi-letter elements without brackets

{
  "id": "molecules:sodium_chloride",
  "name": "Sodium Chloride",
  "nowns": "Na.Cl"
}

This is wrong.

Use:

"nowns": "[Na^+1].[Cl^-1]"

---

## Wrong: omitting explicit charge when needed

{
  "id": "molecules:sodium_chloride",
  "name": "Sodium Chloride",
  "nowns": "[Na].[Cl]"
}

This describes neutral sodium and chlorine atoms, not sodium chloride as an ionic compound.

Use:

"nowns": "[Na^+1].[Cl^-1]"

---

# Recommended conventions

- use `molecules:` for built-in molecule ids
- use `atoms:` for built-in single-atom ids
- keep ids lowercase
- use underscores in ids
- keep names human-readable
- keep NOWNS explicit
- use brackets for all multi-letter elements
- do not use `nuclide:` for molecule ids
- do not use `minecraft:` for molecule ids

---

# Loader expectations

`A valid molecule JSON should be able to go through this pipeline:

JSON -> parse NOWNS -> validate -> normalise -> register

So a molecule file must have:

- a valid `id`
- a valid `name`
- a valid `nowns`
- a valid `molar_mass`

Everything else can be optional depending on your schema.

---

# Final rule

If you are making standard Nuclide molecule files:

- put them in `data/nuclide/molecules/`
- use `molecules:` for molecule ids
- use `atoms:` for atomic species ids
- write NOWNS explicitly
- never rely on implicit hydrogens
- do not use `nuclide:` for ids
- do not use `minecraft:` for ids

# Reaction JSON Guide