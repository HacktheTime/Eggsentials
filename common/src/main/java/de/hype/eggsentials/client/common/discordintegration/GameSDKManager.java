package de.hype.eggsentials.client.common.discordintegration;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import de.hype.eggsentials.client.common.objects.InterceptPacketInfo;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.packets.network.RequestUserInfoPacket;
import de.jcm.discordgamesdk.*;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityJoinRequestReply;
import de.jcm.discordgamesdk.lobby.*;
import de.jcm.discordgamesdk.user.DiscordUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GameSDKManager extends DiscordEventAdapter {
    private final String mcUsername = BBsentials.generalConfig.getUsername();
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Core core;
    private Lobby currentLobby;

    private ScheduledFuture callbackRunner;
    private Map<Long, DiscordLobbyUser> members = new HashMap<>();

    public GameSDKManager() throws Exception {
        // Initialize the Core
        if (core == null) {
            initCore();
            callbackRunner = BBsentials.executionService.scheduleAtFixedRate(() -> {
                try {
                    runContinously();
                } catch (Exception ignored) {
                }
            }, 0, 14, TimeUnit.MILLISECONDS);
            connectToDiscord();
            BBsentials.executionService.scheduleAtFixedRate(this::updateActivity, 1, 20, TimeUnit.SECONDS);
        }

    }

    public static void initCore() throws IOException {
        // Find out which name Discord's library has (.dll for Windows, .so for Linux)
        File sdkDir = new File(EnvironmentCore.utils.getConfigPath() + "/discord_game_sdk");
        sdkDir.mkdirs();
        String dcNativeName = "discord_game_sdk";
        String suffix;
        String jniName = "discord_game_sdk_jni";
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        String version = "2.5.6";
        boolean needsDCNativeLibLoadFirst = false;

        if (osName.contains("windows")) {
            suffix = ".dll";
            osName = "windows";
            jniName = jniName + suffix;
            needsDCNativeLibLoadFirst = true;
        }
        else if (osName.contains("linux")) {
            suffix = ".so";
            osName = "linux";
            jniName = "lib" + jniName + suffix;
        }
        else if (osName.contains("mac os")) {
            suffix = ".dylib";
            osName = "macos";
            jniName = "lib" + jniName + suffix;
        }
        else {
            throw new RuntimeException("cannot determine OS type: " + osName);
        }

		/*
		Some systems report "amd64" (e.g. Windows and Linux), some "x86_64" (e.g. Mac OS).
		At this point we need the "x86_64" version, as this one is used in the ZIP.
		 */
        if (arch.equals("amd64"))
            arch = "x86_64";

        // Path of Discord's library inside the ZIP
        String zipPath = "lib/" + arch + "/" + dcNativeName + suffix;
        File dcNativeLibraryStoragePath = new File(sdkDir, "discord_game_sdk_" + version + suffix);
        if (!dcNativeLibraryStoragePath.exists()) {
            System.out.println("Downloading Discord SDK");
            // Open the URL as a ZipInputStream
            URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/" + version + "/discord_game_sdk.zip");
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestProperty("User-Agent", "discord-game-sdk4j (https://github.com/JnCrMx/discord-game-sdk4j)");
            ZipInputStream zin = new ZipInputStream(connection.getInputStream());

            // Search for the right file inside the ZIP
            ZipEntry entry;
            boolean found = false;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(zipPath)) {
                    // Create a new temporary directory
                    // We need to do this, because we may not change the filename on Windows
                    if (!(sdkDir.mkdir() || sdkDir.exists()))
                        throw new IOException("Cannot create temporary directory");

                    // Create a temporary file inside our directory (with a "normal" name)

                    // Copy the file in the ZIP to our temporary file
                    Files.copy(zin, dcNativeLibraryStoragePath.toPath());
                    // Return our temporary file
                    found = true;
                    break;
                }
                // next entry
                zin.closeEntry();
            }
            zin.close();
            if (!found) throw new RuntimeException("Could not find Discord Native Library!");
        }


        if (arch.equals("x86_64"))
            arch = "amd64";

        if (needsDCNativeLibLoadFirst) System.load(dcNativeLibraryStoragePath.getAbsolutePath());


        String path = "/native/" + osName + "/" + arch + "/" + jniName;
        File jniStoragePath = new File(sdkDir, jniName);
        if (!jniStoragePath.exists()) {
            InputStream in = Core.class.getResourceAsStream(path);
            if (in == null)
                throw new RuntimeException(new FileNotFoundException("cannot find Discord native library at " + path));
            try {
                Files.copy(in, jniStoragePath.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.load(jniStoragePath.getAbsolutePath());
        Core.initDiscordNative(dcNativeLibraryStoragePath.getAbsolutePath());
    }

    public void joinLobby(Long lobbyId, String secret, boolean connectToVc, boolean blocking) {
        LobbyManager mgn = getLobbyManager();
        leaveLobby(mgn, currentLobby);
        getLobbyManager().connectLobby(lobbyId, secret, (result, lobby) -> currentLobby = lobby);
        while (currentLobby == null && blocking) {
            try {
                Thread.sleep(100);
                core.runCallbacks();
            } catch (InterruptedException e) {
            }
        }
        initMembers();
    }

    public void leaveLobby(LobbyManager mgn, Lobby lobby) {
        if (currentLobby != null) {
            mgn.disconnectNetwork(lobby);
            mgn.disconnectVoice(lobby);
            mgn.disconnectLobby(lobby);
        }
        currentLobby = null;

    }

    public void stop() {
        stop.set(true);
    }

    public void runContinously() {
        try {
            core.runCallbacks();
        } catch (Exception e) {

        }
        try {
            // Sleep a bit to save CPU
            Thread.sleep(16);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateActivity() {
        // Create the Activity
        try (Activity activity = new Activity()) {
            if (BBsentials.developerConfig.devMode) {
                activity.setDetails("Programming this ↑");
                activity.setState("Developer Mode: Enabled");
                activity.assets().setLargeImage("i_am_root_backup_laugh");
                activity.assets().setLargeText("I am Root (→ Linux for I'm the Admin)");
                activity.timestamps().setStart(Instant.now());
                activity.assets().setSmallText("BBsentials. A mod by @hackthetime");
                activity.assets().setSmallImage("bingo_hub");
            }
            else {
                Islands island = EnvironmentCore.utils.getCurrentIsland();
                if (island != null) {
                    activity.setDetails("Playing Hypixel Skyblock");
                    activity.setState(EnvironmentCore.utils.getServerId() + ": " + EnvironmentCore.utils.getCurrentIsland().getDisplayName());
                    activity.assets().setSmallText("BBsentials. A mod by @hackthetime");
                    activity.assets().setSmallImage("bingo_hub");
                    activity.assets().setLargeImage("bingo_card");
                }
                else {
                    activity.setDetails("Playing Minecraft");
                    activity.setState("");
                    activity.assets().setLargeText("BBsentials. A mod by @hackthetime");
                    activity.assets().setLargeImage("bingo_hub");
                }
            }
            // Setting a start time causes an "elapsed" field to appear
            activity.timestamps().setStart(Instant.ofEpochSecond(0));

            // We are in a party with 10 out of 100 people.
            try {
                int maxPlayers = EnvironmentCore.utils.getMaximumPlayerCount();
                int currentPlayers = EnvironmentCore.utils.getPlayerCount();
                activity.party().size().setMaxSize(maxPlayers);
                activity.party().size().setCurrentSize(currentPlayers);
            } catch (Exception e) {

            }

//            activity.party().size().setMaxSize(EnvironmentCore.utils.getMaximumPlayerCount());
//            activity.party().size().setCurrentSize(EnvironmentCore.utils.getPlayerCount());

            // Make a "cool" image show up

            // Setting a join secret and a party ID causes an "Ask to Join" button to appear
//            if (currentLobby == null) blockingCreateDefaultLobby();
//            activity.party().setID(String.valueOf(currentLobby.getId()));
//            if (BBsentials.discordConfig.useRPCJoin)
//                activity.secrets().setJoinSecret(getLobbyManager().getLobbyActivitySecret(currentLobby));
//            if (BBsentials.discordConfig.useRPCSpectate) {
////                activity.secrets().setSpectateSecret(getLobbyManager().getLobbyActivitySecret(currentLobby));
//            }
            // Finally, update the currentLobby activity to our activity
            core.activityManager().updateActivity(activity);
        }
    }

    public void clearActivity() {
        core.activityManager().clearActivity();
    }

    public Core getCore() {
        return core;
    }

    @Override
    public void onActivityJoinRequest(DiscordUser user) {
        if (!BBsentials.connection.isConnected()) BBsentials.conditionalReconnectToBBserver();
        AtomicReference<String> username = new AtomicReference<>("");
        if (user.getUsername().equals("hackthetime")) username.set("Hype_the_Time");
        else if (user.getUsername().equals("ooffyy")) username.set("ooffyy");
        else if (user.getUsername().equals("mininoob46")) username.set("mininoob46");
        else {
            BBsentials.connection.sendPacket(RequestUserInfoPacket.fromDCUserID(user.getUserId(), false));
            BBsentials.connection.packetIntercepts.add(new InterceptPacketInfo<RequestUserInfoPacket>(RequestUserInfoPacket.class, true, true, false, false) {
                @Override
                public void run(RequestUserInfoPacket packet) {
                    if (packet.mcUsername == null) {
                        core.activityManager().sendRequestReply(user.getUserId(), ActivityJoinRequestReply.NO);
                        Chat.sendPrivateMessageToSelfError("BB: DC RPC: DC username: " + user.getUsername() + " requested to join but was denied cause they are not registered.");
                        return;
                    }
                    if (!packet.getActivePunishments().isEmpty()) {
                        core.activityManager().sendRequestReply(user.getUserId(), ActivityJoinRequestReply.NO);
                        Chat.sendPrivateMessageToSelfError("BB: DC RPC: DC username: " + user.getUsername() + " requested to join but was denied since their is a punishment ongoing");
                        return;
                    }
                    acceptUserJoinRequest(user, mcUsername, packet.hasRole("mod"));
                }
            });
        }
        acceptUserJoinRequest(user, username.get(), true);
        //Call when someone wants to join me
    }

    public void acceptUserJoinRequest(DiscordUser user, String mcUsername, boolean shallBypass) {
        core.activityManager().sendRequestReply(user.getUserId(), ActivityJoinRequestReply.YES);
        BBsentials.sender.addSendTask("/p " + mcUsername, 1.5);
        if (currentLobby == null) {
            LobbyManager manager = BBsentials.dcGameSDK.getCore().lobbyManager();
            LobbyTransaction txn = manager.getLobbyCreateTransaction();
            txn.setType(LobbyType.PUBLIC);
            txn.setCapacity(50);
            txn.setLocked(false);
            txn.setMetadata("hoster", mcUsername);
            manager.createLobby(manager.getLobbyCreateTransaction(), ((result, lobby) -> {
                this.currentLobby = lobby;
            }));
        }
        else {
            if (getLobbyManager().getMemberUsers(currentLobby).size() == currentLobby.getCapacity() && shallBypass) {
                LobbyTransaction trx = getLobbyManager().getLobbyCreateTransaction();
                trx.setCapacity(currentLobby.getCapacity() + 1);
                trx.setMetadata("hoster", mcUsername);
                getLobbyManager().updateLobby(currentLobby, trx);
            }
            getLobbyManager().connectVoice(currentLobby);
        }

    }

    public LobbyManager getLobbyManager() {
        return getCore().lobbyManager();
    }

    public void disconnectFromLobby(Lobby lobby) {
        try {
            getLobbyManager().disconnectLobby(lobby);
        } catch (Exception e) {

        }
        try {
            getLobbyManager().disconnectVoice(lobby);
        } catch (Exception e) {

        }
    }

    public void disconnectFromLobbyVC(Lobby lobby) {
        try {
            getLobbyManager().disconnectVoice(lobby);
        } catch (Exception e) {

        }
    }

    @Override
    public void onActivityJoin(String secret) {
        if (currentLobby != null) disconnectLobby();
        Chat.sendPrivateMessageToSelfError("BB: DISCORD RPC join Request: Working...");
        BBsentials.executionService.schedule(() -> {
            LobbyManager manager = BBsentials.dcGameSDK.getCore().lobbyManager();
            manager.connectLobbyWithActivitySecret(secret, ((result, lobby) -> {
                if (!result.equals(Result.OK)) {
                    Chat.sendPrivateMessageToSelfError("BB: DISCORD RPC join Request failed: " + result);
                    return;
                }
                manager.connectNetwork(lobby);
                Chat.sendPrivateMessageToSelfSuccess("BB: DISCORD RPC join: Success");
                manager.connectVoice(lobby, result2 -> {
                    if (result2.equals(Result.OK))
                        Chat.sendPrivateMessageToSelfSuccess("BB: DISCORD RPC Voice Connect: Success");
                    else if (result2.equals(Result.LOBBY_FULL))
                        Chat.sendPrivateMessageToSelfError("BB: DISCORD RPC Voice Connect: Lobby Full");
                    else Chat.sendPrivateMessageToSelfError("BB: DISCORD RPC Voice Connect: " + result2);
                });

                setOwnMetaData(lobby);
            }));
            initMembers();
        }, 10, TimeUnit.SECONDS);
        //Call for when I join a lobby // request too
    }

    private void setOwnMetaData(Lobby lobby) {
        LobbyMemberTransaction trans = core.lobbyManager().getMemberUpdateTransaction(lobby, core.userManager().getCurrentUser().getUserId());
        trans.setMetadata("mcusername", BBsentials.generalConfig.getUsername());
        //TODO card count, point count?
        core.lobbyManager().updateMember(lobby, core.userManager().getCurrentUser().getUserId(), trans);
    }

    @Override
    public void onActivitySpectate(String secret) {
        if (currentLobby != null) disconnectLobby();
        Chat.sendPrivateMessageToSelfError("BB: DISCORD RPC join Request: Working...");
        BBsentials.executionService.schedule(() -> {
            LobbyManager manager = BBsentials.dcGameSDK.getCore().lobbyManager();
            manager.connectLobbyWithActivitySecret(secret, ((result, lobby) -> {
                if (!result.equals(Result.OK)) {
                    Chat.sendPrivateMessageToSelfError("BB: DISCORD RPC join Request failed: " + result);
                    return;
                }
                manager.connectNetwork(lobby);
                Chat.sendPrivateMessageToSelfSuccess("BB: DISCORD RPC join: Success");
            }));
            initMembers();
        }, 10, TimeUnit.SECONDS);
    }

    public LobbySearchQuery getSearch() {
        return getLobbyManager().getSearchQuery();
    }

    public LobbyTransaction updateLobby(Lobby lobby) {
        return getLobbyManager().getLobbyUpdateTransaction(lobby);
    }

    @Override
    public void onLobbyMessage(long lobbyId, long userId, byte[] data) {
        super.onLobbyMessage(lobbyId, userId, data);
    }

    public List<DiscordUser> getLobbyMembers() {
        if (currentLobby == null) return new ArrayList<>();
        return getLobbyManager().getMemberUsers(currentLobby);
    }

    public Lobby getCurrentLobby() {
        return currentLobby;
    }

    public void createLobby(LobbyTransaction transaction) {
        disconnectLobby();
        getLobbyManager().createLobby(transaction, (result, lobby) -> currentLobby = lobby);
        initMembers();
    }

    public void updateCurrentLobby(LobbyTransaction transaction) {
        getLobbyManager().updateLobby(currentLobby, transaction);
    }

    public void blockingCreateDefaultLobby() {
        if (currentLobby != null) disconnectLobby();
        AtomicReference<Lobby> lobby = new AtomicReference<>(null);
        LobbyTransaction trn = getLobbyManager().getLobbyCreateTransaction();
        trn.setCapacity(15);
        trn.setLocked(false);
        trn.setMetadata("hoster", mcUsername);
        getLobbyManager().createLobby(trn, lobby::set);
        while (lobby.get() == null) {
            try {
                Thread.sleep(100);
                core.runCallbacks();
            } catch (InterruptedException ignored) {

            }
        }
        currentLobby = lobby.get();
        initMembers();
    }

    public void openVoiceSettings() {
        core.overlayManager().openVoiceSettings();
    }

    public void inviteToGuild(String inviteCode) {
        core.overlayManager().openGuildInvite(inviteCode);
    }

    @Override
    public void onSpeaking(long lobbyId, long userId, boolean speaking) {
        members.get(userId).setIsTalking(speaking);
    }

    private void initMembers() {
        members.clear();
        LobbyManager mgn = getLobbyManager();
        BBsentials.executionService.execute(() -> {
            for (DiscordUser lobbyMember : getLobbyMembers()) {
                try {
                    if (members.get(lobbyMember.getUserId()) == null) {
                        DiscordLobbyUser user = new DiscordLobbyUser(lobbyMember, getCurrentLobby());
                        if (user == null) throw new RuntimeException("Uhm here");
                        user.updateMetaData(mgn);
                        members.put(lobbyMember.getUserId(), user);
                    }
                    //else already inserted.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void joinVC() {
        getLobbyManager().connectVoice(currentLobby);
    }

    public void connectToDiscord() {
        // Set parameters for the Core
//            CreateParams params;
        try {
            disconnectLobby();
//                params = new CreateParams();
            CreateParams params = new CreateParams();

//            params.setClientID(698611073133051974L);
            params.setFlags(CreateParams.getDefaultFlags());
            params.setClientID(1209174746605031444L);
            params.registerEventHandler(this);

            // Create the Core
            core = new Core(params);
            updateActivity();
            initMembers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectLobby() {
        disconnectFromLobby(currentLobby);
        currentLobby = null;
        members.clear();
    }

    public void blockingJoinLobbyWithActivitySecret(String activitySecret) {
        LobbyManager mgn = getLobbyManager();
        leaveLobby(mgn, currentLobby);
        mgn.connectLobbyWithActivitySecret(activitySecret, (result, lobby) -> currentLobby = lobby);
        while (currentLobby == null) {
            try {
                Thread.sleep(100);
                core.runCallbacks();
            } catch (InterruptedException e) {
            }
        }
        initMembers();
    }

    public List<Lobby> blockingSearch(ISearchQuery query) {
        getLobbyManager().search(query.configureSearch(getLobbyManager().getSearchQuery()));
        return getLobbyManager().getLobbies();
    }

    public void deleteLobby() {
        deleteLobby(currentLobby);
        currentLobby = null;
        members.clear();
    }

    public void deleteLobby(Lobby lobby) {
        getLobbyManager().deleteLobby(lobby);
    }

    public void disconnectLobbyVC() {
        disconnectFromLobbyVC(currentLobby);
    }

    @Override
    public void onMemberConnect(long lobbyId, long userId) {
        initMembers();
    }

    public List<DiscordLobbyUser> getAdvancedLobbyMembers() {
        if (members == null || members.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(members.values());
    }

    @Override
    public void onMemberDisconnect(long lobbyId, long userId) {
        initMembers();
    }


    @Override
    public void onMemberUpdate(long lobbyId, long userId) {
        members.get(userId).updateMetaData(getLobbyManager());
    }

    @FunctionalInterface
    public interface ISearchQuery {
        LobbySearchQuery configureSearch(LobbySearchQuery query);
    }
}
