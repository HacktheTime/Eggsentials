package de.hype.eggsentials.client.common.objects;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.client.CustomGson;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaypointRoute {
    public static final File waypointRouteDirectory = new File(EnvironmentCore.utils.getConfigPath(), "waypoints/routes/");
    public static final File exportRouteDirectory = new File(EnvironmentCore.utils.getConfigPath(), "waypoints/routes/export/");
    public int currentNode;
    public List<RouteNode> nodes = new ArrayList<>();
    public String name;

    public WaypointRoute(String name, List<RouteNode> nodes) {
        this.name = name;
        if (name.isEmpty()) {
            this.name = new Date().toString().replace(" ", "_");
        }
        this.nodes = nodes;
        this.currentNode = 0;
    }

    private WaypointRoute(File file) {
        String fileName = file.getName();
        name = fileName.substring(0, fileName.lastIndexOf("."));
        loadConfiguration(file);
    }

    /**
     * Does not actually load the route into the mod but acutally gets it as object from the file.
     * To actually load it use {@link #loadRoute(String)}
     *
     * @param file file to load from
     * @return the route
     * @throws Exception when there is a problem when loading the route
     */
    public static WaypointRoute loadFromFile(File file) {
        WaypointRoute route = new WaypointRoute(file);

        if (isColewehightsFormat(file)) {
            route.loadFromColewehightsFormat(file);
        }
        else {
            route.loadConfiguration(file);
        }
        return route;
    }

    private static boolean isColewehightsFormat(File file) {
        try (FileReader reader = new FileReader(file)) {
            Gson gson = CustomGson.create();
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);

            if (jsonArray != null && jsonArray.size() > 0) {
                JsonObject nodeObject = jsonArray.get(0).getAsJsonObject();
                return nodeObject.has("x") && nodeObject.has("y") && nodeObject.has("z")
                        && nodeObject.has("r") && nodeObject.has("g") && nodeObject.has("b")
                        && nodeObject.has("options") && nodeObject.getAsJsonObject("options").has("name");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void loadRoute(WaypointRoute route) {
        BBsentials.temporaryConfig.route = route;
    }

    public static WaypointRoute loadRoute(String nameOrPath) throws Exception {
        File file = new File(nameOrPath);
        if (!file.exists()) {
            if (!nameOrPath.endsWith(".json")) nameOrPath += ".json";
            file = new File(waypointRouteDirectory.getPath(),nameOrPath);
            if (!file.exists()) throw new Exception("Route does not exist");
            BBsentials.temporaryConfig.route = new WaypointRoute(file);
            return BBsentials.temporaryConfig.route;
        }
        throw new Exception("Route does not exist");
    }

    private void loadFromColewehightsFormat(File file) {
        Gson gson = CustomGson.create();
        try (FileReader reader = new FileReader(file)) {
            JsonArray colewehightsArray = gson.fromJson(reader, JsonArray.class);
            for (int i = 0; i < colewehightsArray.size(); i++) {
                JsonObject nodeObject = colewehightsArray.get(i).getAsJsonObject();
                int x = nodeObject.get("x").getAsInt();
                int y = nodeObject.get("y").getAsInt();
                int z = nodeObject.get("z").getAsInt();
                float r = nodeObject.get("r").getAsFloat();
                float g = nodeObject.get("g").getAsFloat();
                float b = nodeObject.get("b").getAsFloat();
                String name = nodeObject.getAsJsonObject("options").get("name").getAsString();

                RouteNode node = new RouteNode(x, y, z, r, g, b, name, true, this);
                nodes.add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadConfiguration(File file) {
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (!java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    JsonObject jsonObject = loadJsonFile(file);
                    if (jsonObject.has(fieldName)) {
                        field.set(this, CustomGson.create().fromJson(jsonObject.get(fieldName), field.getType()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private JsonObject loadJsonFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    public void save() {
        waypointRouteDirectory.mkdirs();
        JsonObject jsonObject = new JsonObject();

        for (Field field : getClass().getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    jsonObject.add(field.getName(), CustomGson.create().toJsonTree(field.get(this)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        saveToColewheightsFormat();
        String fileName = name + ".json";
        File configFile = new File(waypointRouteDirectory, fileName);
        try (FileWriter writer = new FileWriter(configFile)) {
            Gson gson = CustomGson.create();
            String jsonOutput = gson.toJson(jsonObject);
            writer.write(jsonOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public synchronized int getNextNodeId() {
//        return ++nextNodeId;
//    }

    public void saveToColewheightsFormat() {
        exportRouteDirectory.mkdirs();
        Gson gson = CustomGson.create();
        JsonArray colewheightsArray = new JsonArray();

        for (RouteNode node : nodes) {
            JsonObject nodeObject = new JsonObject();
            nodeObject.addProperty("x", node.coords.x);
            nodeObject.addProperty("y", node.coords.y);
            nodeObject.addProperty("z", node.coords.z);
            nodeObject.addProperty("r", node.color.getRed());
            nodeObject.addProperty("g", node.color.getGreen());
            nodeObject.addProperty("b", node.color.getBlue());

            JsonObject optionsObject = new JsonObject();
            optionsObject.addProperty("name", String.valueOf(node.name));
            nodeObject.add("options", optionsObject);

            colewheightsArray.add(nodeObject);
        }

        String fileName = name + "_colewheight.json";
        File colewehightsFile = new File(exportRouteDirectory + fileName);

        try (FileWriter writer = new FileWriter(colewehightsFile)) {
            writer.write(gson.toJson(colewheightsArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RouteNode getCurrentNode() {
        return nodes.get(currentNode);
    }

    public void doNextNodeCheck(double distance) {
        RouteNode current = getCurrentNode();
        int triggerRange = current.triggerNextRange;
        if (triggerRange == -1) triggerRange = RouteNode.DEFAULT_TRIGGER_NEXT_RANGE;
        if (distance < triggerRange) currentNode++;
        if (currentNode >= nodes.size()) currentNode = 0;
    }

    public void setCurentNode(int startingnodeid) {
        currentNode = startingnodeid;
        if (currentNode >= nodes.size()) currentNode = 0;
        if (currentNode < 0) currentNode = nodes.size() - 1;

    }
}
