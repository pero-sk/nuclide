package com.penguin.nuclide.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.penguin.nuclide.data.JsonHelper;
import com.penguin.nuclide.data.NuclideDataLoader;
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

public final class SpeciesTagDataLoader implements SimpleSynchronousResourceReloadListener {

    public static final String MOD_ID = "nuclide";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final SpeciesTagRegistry TAGS = new SpeciesTagRegistry();

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new SpeciesTagDataLoader());
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(MOD_ID, "species_tag_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        TAGS.clear();

        Map<Identifier, Resource> resources = manager.findResources(
                "tags/species",
                id -> id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                loadTagContribution(root, resourceId);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load species tag JSON: " + resourceId, e);
            }
        }

        LOGGER.info("[Nuclide] Loaded {} species tags", TAGS.size());
    }

    private void loadTagContribution(JsonObject root, Identifier resourceId) {
        String tagId = JsonHelper.requireString(root, "id");
        JsonArray valuesArray = root.getAsJsonArray("values");

        if (valuesArray == null) {
            throw new IllegalStateException("Tag '" + tagId + "' is missing required array field: values");
        }

        SpeciesTagDefinition tag = TAGS.getOrCreate(tagId);

        for (int i = 0; i < valuesArray.size(); i++) {
            JsonElement element = valuesArray.get(i);
            if (!element.isJsonPrimitive()) {
                throw new IllegalStateException(
                        "Tag '" + tagId + "' has non-string value at index " + i + " in " + resourceId
                );
            }

            String speciesId = element.getAsString();

            if (!NuclideDataLoader.SPECIES.containsId(speciesId)) {
                throw new IllegalStateException(
                        "Tag '" + tagId + "' references unknown species '" + speciesId + "' in " + resourceId
                );
            }

            tag.add(speciesId);
        }
    }
}