package de.hype.eggsentials.client.common.client;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.chat.Sender;
import de.hype.eggsentials.client.common.client.commands.Commands;
import de.hype.eggsentials.client.common.client.objects.ServerSwitchTask;
import de.hype.eggsentials.client.common.communication.BBsentialConnection;
import de.hype.eggsentials.client.common.config.BBServerConfig;
import de.hype.eggsentials.client.common.config.DeveloperConfig;
import de.hype.eggsentials.client.common.config.GeneralConfig;
import de.hype.eggsentials.client.common.mclibraries.CustomItemTexture;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.objects.EggType;
import de.hype.eggsentials.shared.objects.Position;
import de.hype.eggsentials.shared.packets.network.EggFoundPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Eggsentials {
    public static final Sender sender = new Sender();
    public static BBsentialConnection connection;
    public static Commands coms;
    public static ScheduledExecutorService executionService = Executors.newScheduledThreadPool(1000);
    public static Map<Integer, ServerSwitchTask> onServerJoin = new HashMap<>();
    public static Map<Integer, ServerSwitchTask> onServerLeave = new HashMap<>();

    public static Map<Integer, CustomItemTexture> customItemTextures = new HashMap<>();
    public static Thread bbthread;
    public static Chat chat = new Chat();
    public static Thread debugThread;
    //General Config needs to be first config!
    public static GeneralConfig generalConfig = new GeneralConfig();
    //All Other Configs
    public static DeveloperConfig developerConfig = new DeveloperConfig();
    public static BBServerConfig bbServerConfig = new BBServerConfig();
    public static Map<Islands, Map<EggType, EggWaypoint>> islandEggMap = new HashMap<>();
    public static String overwriteActionBar;
    private static boolean initialised = false;
    private static volatile ScheduledFuture<?> futureServerJoin;
    private static volatile boolean futureServerJoinRunning;

    public static void connectToBBserver() {
        executionService.execute(() -> {
            if (connection != null) {
                connection.close();
            }
            bbthread = new Thread(() -> {
                connection = new BBsentialConnection();
                coms = new Commands();
//                connection.connect("localhost", 5020);
                connection.connect(bbServerConfig.bbServerURL, 5020);

            });
            bbthread.start();
        });
    }

    /**
     * Checks if still connected to the Server.
     *
     * @return true if it connected; false if old connection is kept.
     */
    public static boolean conditionalReconnectToBBserver() {
        if (!connection.isConnected()) {
            Chat.sendPrivateMessageToSelfInfo("Reconnecting");
            return true;
        }
        return false;
    }

    /**
     * Runs the mod initializer on the client environment.
     */

    public synchronized static void onServerJoin() {
        onServerLeave();
        if (futureServerJoin != null) {
            futureServerJoin.cancel(false);
            if (futureServerJoinRunning)
                Chat.sendPrivateMessageToSelfError("Eggsentials: You switched Lobbies so quickly that some things may weren't completed in time. Do not report this as bug!");
            else
                System.out.println("Eggsentials-Debug Output: Swapped Lobbies really quickly. Lobby Join events tasks were not executed.");
        }
        futureServerJoin = executionService.schedule(() -> {
            futureServerJoinRunning = true;
            for (ServerSwitchTask task : onServerJoin.values()) {
                if (!task.permanent) {
                    onServerJoin.remove(task.getId());
                }
                try {
                    executionService.execute(task::run);
                } catch (Exception e) {
                    System.out.println("Error in a task.");
                    e.printStackTrace();
                }
            }
            futureServerJoin = null;
            futureServerJoinRunning = false;
        }, 5, TimeUnit.SECONDS);
    }

    public static void onServerLeave() {
        for (ServerSwitchTask task : onServerLeave.values()) {
            if (!task.permanent) {
                onServerLeave.remove(task.getId()).run();
            }
            try {
                executionService.execute(task::run);
            } catch (Exception e) {
                System.out.println("Error in a task.");
                e.printStackTrace();
            }
        }
    }

    public static void init() {
        Eggsentials.connectToBBserver();
    }

    public static void addEggToIsland(Islands currentIsland, EggType type, Position pos) {
        if (type != null) {
            Eggsentials.islandEggMap.putIfAbsent(currentIsland, new HashMap<>());
            Map<EggType, EggWaypoint> islandMaped = Eggsentials.islandEggMap.get(currentIsland);
            EggWaypoint posKnown = islandMaped.get(type);
            EggWaypoint newPoint = new EggWaypoint(type, currentIsland, pos, true);
            if (posKnown == null || !posKnown.equals(newPoint)) {
                if (developerConfig.devMode)
                    Chat.sendPrivateMessageToSelfSuccess("unknown location found → sending packet");
                Eggsentials.connection.sendPacket(new EggFoundPacket(Eggsentials.generalConfig.getUsername(), currentIsland, pos, type));
                newPoint.setFound(true);
                newPoint.removeFromPool();
                islandMaped.put(type, newPoint);
            }
            else {
                if (developerConfig.devMode) Chat.sendPrivateMessageToSelfSuccess("location already known");
                posKnown.setFound(true);
                posKnown.removeFromPool();
                newPoint.setFound(true);
                newPoint.removeFromPool();
            }
        }
    }
}