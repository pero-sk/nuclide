package com.penguin.nuclide.reaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.penguin.nuclide.data.JsonHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ReactionDataLoader implements SimpleSynchronousResourceReloadListener {

    public static final String MOD_ID = "nuclide";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final ReactionRegistry REACTIONS = new ReactionRegistry();

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new ReactionDataLoader());
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(MOD_ID, "reaction_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        REACTIONS.clear();

        Map<Identifier, Resource> resources = manager.findResources(
                "reactions",
                id -> id.getNamespace().equals(MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                ReactionDefinition reaction = parseReactionDefinition(root);
                ReactionValidator.validateOrThrow(reaction);
                REACTIONS.register(reaction);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load reaction JSON: " + resourceId, e);
            }
        }

        LOGGER.info("[Nuclide] Loaded {} reaction definitions", REACTIONS.size());
    }

    private ReactionDefinition parseReactionDefinition(JsonObject root) {
        String id = JsonHelper.requireString(root, "id");
        String name = JsonHelper.optionalString(root, "name", id);

        JsonArray inputsArray = root.getAsJsonArray("inputs");
        JsonArray outputsArray = root.getAsJsonArray("outputs");

        if (inputsArray == null) {
            throw new IllegalStateException("Reaction '" + id + "' is missing required array field: inputs");
        }

        if (outputsArray == null) {
            throw new IllegalStateException("Reaction '" + id + "' is missing required array field: outputs");
        }

        List<ReactionParticipant> inputs = parseParticipants(inputsArray);
        List<ReactionParticipant> outputs = parseParticipants(outputsArray);

        ReactionConditions conditions = parseConditions(root.getAsJsonObject("conditions"));
        int durationTicks = root.has("duration") && !root.get("duration").isJsonNull()
                ? (int) root.get("duration").getAsDouble()
                : 0;

        return new ReactionDefinition(id, name, inputs, outputs, conditions, durationTicks);
    }

    private List<ReactionParticipant> parseParticipants(JsonArray array) {
        List<ReactionParticipant> participants = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            String species = obj.has("species") && !obj.get("species").isJsonNull()
                    ? obj.get("species").getAsString()
                    : null;
            String tag = obj.has("tag") && !obj.get("tag").isJsonNull()
                    ? obj.get("tag").getAsString()
                    : null;
            int count = (int) JsonHelper.requireDouble(obj, "count");

            if (species != null && tag != null) {
                throw new IllegalStateException("Reaction participant cannot define both 'species' and 'tag'");
            }

            if (species == null && tag == null) {
                throw new IllegalStateException("Reaction participant must define either 'species' or 'tag'");
            }

            participants.add(species != null
                    ? ReactionParticipant.species(species, count)
                    : ReactionParticipant.tag(tag, count));
        }

        return participants;
    }

    private ReactionConditions parseConditions(JsonObject obj) {
        if (obj == null) {
            return new ReactionConditions(null, null, false, null, null, null);
        }

        Double minTemperature = obj.has("min_temperature") && !obj.get("min_temperature").isJsonNull()
                ? obj.get("min_temperature").getAsDouble()
                : null;

        Double maxTemperature = obj.has("max_temperature") && !obj.get("max_temperature").isJsonNull()
                ? obj.get("max_temperature").getAsDouble()
                : null;

        boolean requiresSpark = obj.has("requires_spark") && !obj.get("requires_spark").isJsonNull()
                && obj.get("requires_spark").getAsBoolean();

        String catalyst = obj.has("catalyst") && !obj.get("catalyst").isJsonNull()
                ? obj.get("catalyst").getAsString()
                : null;

        Double minPressure = obj.has("min_pressure") && !obj.get("min_pressure").isJsonNull()
                ? obj.get("min_pressure").getAsDouble()
                : null;

        Double maxPressure = obj.has("max_pressure") && !obj.get("max_pressure").isJsonNull()
                ? obj.get("max_pressure").getAsDouble()
                : null;

        return new ReactionConditions(minTemperature, maxTemperature, requiresSpark, catalyst, minPressure, maxPressure);
    }
}