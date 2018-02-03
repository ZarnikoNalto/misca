package msifeed.mc.misca.crabs.action;

import com.google.gson.*;
import msifeed.mc.misca.crabs.rules.Effect;
import msifeed.mc.misca.crabs.rules.Modifier;
import msifeed.mc.misca.crabs.rules.Rules;

import java.lang.reflect.Type;
import java.util.List;

public class ActionJsonSerializer implements JsonSerializer<Action>, JsonDeserializer<Action> {
    @Override
    public JsonElement serialize(Action action, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject root = new JsonObject();
        root.addProperty("name", action.name);
        root.addProperty("title", action.title);
        root.addProperty("type", action.type.toString());

        root.add("modifiers", serializeToStrings(action.modifiers));
        if (!action.target_effects.isEmpty()) root.add("target_effects", serializeToStrings(action.target_effects));
        if (!action.self_effects.isEmpty()) root.add("self_effects", serializeToStrings(action.self_effects));

        final JsonArray tags = new JsonArray();
        if (action.isDefencive()) tags.add(new JsonPrimitive("defencive"));
        if (tags.size() > 0) root.add("tags", tags);

        return root;
    }

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject root = (JsonObject) json;

        final String name = root.get("name").getAsString();
        final String title = root.get("title").getAsString();
        final String typeName = root.get("type").getAsString();
        final Action.Type type = Action.Type.valueOf(typeName.toUpperCase());

        final Action action = new Action(name, title, type);

        final JsonElement dealNoDamage = root.get("defencive");
        if (dealNoDamage != null) action.defencive = dealNoDamage.getAsBoolean();

        for (JsonElement e : root.getAsJsonArray("modifiers")) {
            final Modifier m = Rules.mod(e.getAsString().toLowerCase());
            if (m == null) throw new JsonParseException("Unknown modifier: " + e.toString());
            action.modifiers.add(m);
        }

        final JsonArray targetEffects = root.getAsJsonArray("target_effects");
        if (targetEffects != null) {
            for (JsonElement e : targetEffects) {
                final Effect eff = Rules.effect(e.getAsString().toLowerCase());
                if (eff == null) throw new JsonParseException("Unknown effect: " + e.toString());
                action.target_effects.add(eff);
            }
        }

        final JsonArray selfEffects = root.getAsJsonArray("self_effects");
        if (selfEffects != null) {
            for (JsonElement e : selfEffects) {
                final Effect eff = Rules.effect(e.getAsString().toLowerCase());
                if (eff == null) throw new JsonParseException("Unknown effect: " + e.toString());
                action.self_effects.add(eff);
            }
        }

        final JsonArray tags = root.getAsJsonArray("tags");
        if (tags != null) {
            for (JsonElement e : tags) {
                if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString())
                    throw new JsonParseException("Action tag must be a string!");
                switch (e.getAsString()) {
                    case "defencive":
                        action.defencive = true;
                        break;
                }
            }
        }

        return action;
    }

    private JsonArray serializeToStrings(List objects) {
        final JsonArray array = new JsonArray();
        for (Object o : objects) array.add(new JsonPrimitive(o.toString().toLowerCase()));
        return array;
    }
}
