package com.penguin.nuclide.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.atomic.Molecule;
import com.penguin.nuclide.atomic.StateType;
import com.penguin.nuclide.nowns.NownsNormaliser;
import com.penguin.nuclide.nowns.NownsParser;
import com.penguin.nuclide.nowns.ParsedMolecule;
import com.penguin.nuclide.species.PhaseRepresentation;
import com.penguin.nuclide.species.SpeciesRepresentation;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public final class NuclideDataLoader {
    private static final Gson GSON = new Gson();

    public static final SpeciesRegistry SPECIES = new SpeciesRegistry();

    private static final String ATOMS_PATH = "atoms";
    private static final String MOLECULES_PATH = "molecules";

    private NuclideDataLoader() {}

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Nuclide.asIdentifier("species_loader");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        loadAll(manager);
                    }
                }
        );
    }

    public static void loadAll(ResourceManager manager) {
        Objects.requireNonNull(manager, "manager");

        SPECIES.clear();

        loadDirectory(manager, ATOMS_PATH, SpeciesKind.ATOM);
        loadDirectory(manager, MOLECULES_PATH, SpeciesKind.MOLECULE);

        Nuclide.LOGGER.info("Loaded {} species", SPECIES.size());
    }

    private static void loadDirectory(ResourceManager manager, String folder, SpeciesKind kind) {
        Map<Identifier, Resource> resources = manager.findResources(
                folder,
                id -> id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                SpeciesDefinition definition = parseSpecies(json, kind);
                SPECIES.register(definition);
            } catch (Exception e) {
                Nuclide.LOGGER.error("Failed to load species resource '{}': {}", resourceId, e.getMessage(), e);
            }
        }
    }

    private static SpeciesDefinition parseSpecies(JsonObject json, SpeciesKind kind) {
        String id = getRequiredString(json, "id");
        String name = getRequiredString(json, "name");
        String rawNowns = getRequiredString(json, "nowns");

        String normalizedNowns = NownsNormaliser.normalise(rawNowns);
        ParsedMolecule parsed = NownsParser.parse(normalizedNowns);
        Molecule molecule = parsed.molecule();

        StateType state = parseState(getRequiredString(json, "default_state"));
        Double meltingPoint = getOptionalDouble(json, "melting_point");
        Double boilingPoint = getOptionalDouble(json, "boiling_point");
        boolean radioactive = getRequiredBoolean(json, "radioactive");
        boolean toxic = getRequiredBoolean(json, "toxic");
        boolean flammable = getRequiredBoolean(json, "flammable");
        double molarMass = getRequiredDouble(json, "molar_mass");

        String namespace = extractNamespace(id);
        SpeciesRepresentation representation = parseRepresentation(json.getAsJsonObject("representation"));
        StabilityDefinition stability = parseStability(json.getAsJsonObject("stability"));

        return new SpeciesDefinition(
                id,
                name,
                rawNowns,
                normalizedNowns,
                namespace,
                molecule,
                parsed,
                state,
                meltingPoint,
                boilingPoint,
                radioactive,
                toxic,
                flammable,
                molarMass,
                kind,
                representation,
                stability
        );
    }

    private static StabilityDefinition parseStability(JsonObject json) {
        if (json == null) {
            return StabilityDefinition.STABLE;
        }

        StabilityType type = parseStabilityType(getOptionalString(json, "type"));
        Integer lifetimeTicks = getOptionalInt(json, "lifetime_ticks");
        boolean decaysInContainer = getOptionalBoolean(json, "decays_in_container", false);
        boolean decaysInWorld = getOptionalBoolean(json, "decays_in_world", false);

        return new StabilityDefinition(type, lifetimeTicks, decaysInContainer, decaysInWorld);
    }

    private static StabilityType parseStabilityType(String raw) {
        if (raw == null || raw.isBlank()) {
            return StabilityType.STABLE;
        }

        try {
            return StabilityType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown stability type '" + raw + "'");
        }
    }

    private static SpeciesRepresentation parseRepresentation(JsonObject json) {
        if (json == null) {
            return SpeciesRepresentation.EMPTY;
        }

        PhaseRepresentation solid = parsePhaseRepresentation(json, "solid");
        PhaseRepresentation liquid = parsePhaseRepresentation(json, "liquid");
        PhaseRepresentation gas = parsePhaseRepresentation(json, "gas");

        return new SpeciesRepresentation(solid, liquid, gas);
    }

    private static PhaseRepresentation parsePhaseRepresentation(JsonObject parent, String key) {
        if (parent == null || !parent.has(key) || parent.get(key).isJsonNull()) {
            return null;
        }

        JsonObject phaseObject = parent.getAsJsonObject(key);
        String block = getOptionalString(phaseObject, "block");

        if (block == null) {
            return null;
        }

        return new PhaseRepresentation(block);
    }

    private static StateType parseState(String raw) {
        try {
            return StateType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state '" + raw + "'");
        }
    }

    private static String extractNamespace(String id) {
        int index = id.indexOf(':');
        if (index < 0) {
            throw new IllegalArgumentException("Species id must contain namespace: " + id);
        }
        return id.substring(0, index);
    }

    private static String getRequiredString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new IllegalArgumentException("Missing required string field '" + key + "'");
        }

        String value = json.get(key).getAsString();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Field '" + key + "' cannot be blank");
        }

        return value;
    }

    private static String getOptionalString(JsonObject json, String key) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }

        String value = json.get(key).getAsString();
        return value.isBlank() ? null : value;
    }

    private static double getRequiredDouble(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new IllegalArgumentException("Missing required number field '" + key + "'");
        }

        double value = json.get(key).getAsDouble();
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Field '" + key + "' must be finite");
        }

        return value;
    }

    private static Double getOptionalDouble(JsonObject json, String key) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }

        double value = json.get(key).getAsDouble();
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Field '" + key + "' must be finite");
        }

        return value;
    }

    private static Integer getOptionalInt(JsonObject json, String key) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }

        return json.get(key).getAsInt();
    }

    private static boolean getRequiredBoolean(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new IllegalArgumentException("Missing required boolean field '" + key + "'");
        }

        return json.get(key).getAsBoolean();
    }

    private static boolean getOptionalBoolean(JsonObject json, String key, boolean fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }

        return json.get(key).getAsBoolean();
    }
}