package com.penguin.nuclide.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.penguin.nuclide.atomic.StateType;
import com.penguin.nuclide.nowns.NownsNormaliser;
import com.penguin.nuclide.nowns.NownsParser;
import com.penguin.nuclide.nowns.ParsedMolecule;
import com.penguin.nuclide.nowns.validation.NownsValidator;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class NuclideDataLoader implements SimpleSynchronousResourceReloadListener {

    public static final String MOD_ID = "nuclide";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final MoleculeRegistry MOLECULES = new MoleculeRegistry();

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new NuclideDataLoader());
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(MOD_ID, "molecule_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        MOLECULES.clear();

        loadFolder(manager, "molecules");

        LOGGER.info("[Nuclide] Loaded {} molecule definitions", MOLECULES.size());

        MoleculeDefinition water = MOLECULES.getById("nuclide:water");
        if (water != null) {
            LOGGER.info("[Nuclide] Water normalized NOWNS: {}", water.normalizedNowns());
        }

        MoleculeDefinition salt = MOLECULES.getByNormalizedNowns("nuclide:[Cl^-1].[Na^+1]");
        if (salt != null) {
            LOGGER.info("[Nuclide] Salt normalized NOWNS lookup worked: {}", salt.id());
        }
    }

    private void loadFolder(ResourceManager manager, String folder) {
        Map<Identifier, Resource> resources = manager.findResources(
            folder,
            id -> id.getNamespace().equals(MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);

                MoleculeDefinition definition = parseMoleculeDefinition(root, resourceId);
                MOLECULES.register(definition);

            } catch (Exception e) {
                throw new IllegalStateException("Failed to load molecule JSON: " + resourceId, e);
            }
        }
    }

    private MoleculeDefinition parseMoleculeDefinition(JsonObject root, Identifier resourceId) {
        String id = JsonHelper.requireString(root, "id");
        String name = JsonHelper.optionalString(root, "name", id);
        String rawNowns = JsonHelper.requireString(root, "nowns");
        StateType state = JsonHelper.optionalEnum(root, "state", StateType.class, StateType.SOLID);
        double meltingPoint = JsonHelper.optionalDouble(root, "melting_point", 0.0);
        double boilingPoint = JsonHelper.optionalDouble(root, "boiling_point", 0.0);
        boolean radioactive = JsonHelper.optionalBoolean(root, "radioactive", false);
        boolean toxic = JsonHelper.optionalBoolean(root, "toxic", false);
        boolean flammable = JsonHelper.optionalBoolean(root, "flammable", false);
        double molarMass = JsonHelper.requireDouble(root, "molar_mass");

        ParsedMolecule parsed = NownsParser.parse(rawNowns);
        NownsValidator.validateOrThrow(parsed.molecule());
        String normalized = NownsNormaliser.normalise(parsed);

        return new MoleculeDefinition(
                id,
                name,
                rawNowns,
                normalized,
                parsed.namespace(),
                parsed.molecule(),
                parsed,
                state,
                meltingPoint,
                boilingPoint,
                radioactive,
                toxic,
                flammable,
                molarMass
        );
    }
}