package de.hype.eggsentials.client.common.communication;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import de.hype.eggsentials.client.common.objects.InterceptPacketInfo;
import de.hype.eggsentials.environment.packetconfig.AbstractPacket;
import de.hype.eggsentials.environment.packetconfig.PacketManager;
import de.hype.eggsentials.environment.packetconfig.PacketUtils;
import de.hype.eggsentials.shared.constants.AuthenticationConstants;
import de.hype.eggsentials.shared.constants.InternalReasonConstants;
import de.hype.eggsentials.shared.objects.PunishmentData;
import de.hype.eggsentials.shared.packets.function.PlaySoundPacket;
import de.hype.eggsentials.shared.packets.network.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class BBsentialConnection {
    public Thread messageReceiverThread;
    public Thread messageSenderThread;
    public List<InterceptPacketInfo> packetIntercepts = new ArrayList();
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private LinkedBlockingQueue<String> messageQueue;
    private PacketManager packetManager;

    public BBsentialConnection() {
        packetManager = new PacketManager(this);
    }

    public void connect(String serverIP, int serverPort) {
        // Enable SSL handshake debugging
        System.setProperty("javax.net.debug", "ssl,handshake");
        FileInputStream inputStream = null;
        try {
            // Load the BBssentials-online server's public certificate from the JKS file
            try {
                InputStream resouceInputStream = BBsentials.class.getResourceAsStream("/assets/public_bbsentials_cert.crt");

                // Check if the resource exists
                if (resouceInputStream == null) {
                    System.out.println("The resource '/assets/public_bbsentials_cert.crt' was not found.");
                    return;
                }

                File tempFile = File.createTempFile("public_bbsentials_cert", ".crt");
                tempFile.deleteOnExit();

                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = resouceInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                resouceInputStream.close();

                // Now you have the .crt file as a FileInputStream in the tempFile
                inputStream = new FileInputStream(tempFile);

                // Use the fileInputStream as needed

            } catch (IOException e) {
                e.printStackTrace();
            }
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate serverCertificate = (X509Certificate) certFactory.generateCertificate(inputStream);
            PublicKey serverPublicKey = serverCertificate.getPublicKey();

            // Create a TrustManager that trusts only the server's public key
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null; // We don't need to check the client's certificates
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    // Do nothing, client certificate validation not required
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    // Check if the server certificate matches the expected one
                    if (certs.length == 1) {
                        PublicKey publicKey = certs[0].getPublicKey();
                        if (!publicKey.equals(serverPublicKey)) {
                            throw new CertificateException("Server certificate not trusted.");
                        }
                    }
                    else {
                        throw new CertificateException("Invalid server certificate chain.");
                    }
                }
            }};

            // Create an SSL context with the custom TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            // Create an SSL socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            socket = sslSocketFactory.createSocket(serverIP, serverPort);
            socket.setKeepAlive(true); // Enable Keep-Alive
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            messageQueue = new LinkedBlockingQueue<>();

            // Start message receiver thread
            messageReceiverThread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        String message = reader.readLine();
                        if (message != null) {
                            onMessageReceived(message);
                        }
                        else {
                            Chat.sendPrivateMessageToSelfError("BB: It seemed like you disconnected.");
                            try {
                                Thread.sleep(10000);
                            } catch (Exception e) {

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();
            messageReceiverThread.setName("bb receiver thread");
            // Start message sender thread
            messageSenderThread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        String message = messageQueue.take();
                        if (BBsentials.developerConfig.isDetailedDevModeEnabled())
                            Chat.sendPrivateMessageToSelfDebug("BBs: " + message);
                        writer.println(message);
                    }
                } catch (InterruptedException | NullPointerException ignored) {
                }
            });
            messageSenderThread.start();
            messageSenderThread.setName("bb sender thread");

        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public void onMessageReceived(String message) {
        if (!PacketUtils.handleIfPacket(this, message)) {
            if (message.startsWith("H-")) {
            }
            else {
                Chat.sendPrivateMessageToSelfSuccess("BB: " + message);
            }
        }
    }

    public <T extends AbstractPacket> void dummy(T o) {
        //this does absolutely nothing. dummy for packet in packt manager
    }

    public <E extends AbstractPacket> void sendPacket(E packet) {
        String packetName = packet.getClass().getSimpleName();
        String rawjson = PacketUtils.parsePacketToJson(packet);
        if (isConnected() && writer != null) {
            if (BBsentials.developerConfig.isDetailedDevModeEnabled() && !((packet.getClass().equals(RequestConnectPacket.class) && !BBsentials.bbServerConfig.useMojangAuth) && BBsentials.developerConfig.devSecurity)) {
                Chat.sendPrivateMessageToSelfDebug("BBDev-sP: " + packetName + ": " + rawjson);
            }
            writer.println(packetName + "." + rawjson);
        }
        else {
            Chat.sendPrivateMessageToSelfError("BB: Couldn't send a " + packetName + "! did you get disconnected?");
        }
    }

    public void onWelcomePacket(WelcomeClientPacket packet) {
        if (packet.success) {
            BBsentials.generalConfig.bbsentialsRoles = packet.roles;
            Chat.sendPrivateMessageToSelfSuccess("Login Success");
            if (!packet.motd.isEmpty()) {
                Chat.sendPrivateMessageToSelfImportantInfo(packet.motd);
            }
        }
        else {
            Chat.sendPrivateMessageToSelfError("Login Failed");
        }
    }

    public void onDisconnectPacket(DisconnectPacket packet) {
        if (EnvironmentCore.utils.isInGame()) {
            Chat.sendPrivateMessageToSelfError(packet.displayMessage);
            try {
                BBsentials.connection.close();
            } catch (Exception ignored) {
            }
            for (int i = 0; i < packet.waitBeforeReconnect.length; i++) {
                int finalI = i;
                BBsentials.executionService.schedule(() -> {
                    if (finalI == 0) {
                        BBsentials.connectToBBserver();
                    }
                    else {
                        BBsentials.conditionalReconnectToBBserver();
                    }
                }, (long) (packet.waitBeforeReconnect[i] + (Math.random() * packet.randomExtraDelay)), TimeUnit.SECONDS);
            }
        }
        else {
            if (packet.internalReason.equals(InternalReasonConstants.NOT_REGISTERED))
                EnvironmentCore.utils.showErrorScreen("Could not connect to the network. Reason: \n" + packet.displayMessage);
            else EnvironmentCore.utils.showErrorScreen(packet.displayMessage);
        }
    }

    public void onInternalCommandPacket(InternalCommandPacket packet) {
        if (packet.command.equals(InternalCommandPacket.REQUEST_POT_DURATION)) {
            sendPacket(new InternalCommandPacket(InternalCommandPacket.SET_POT_DURATION, new String[]{String.valueOf(EnvironmentCore.utils.getPotTime())}));
        }
        else if (packet.command.equals(InternalCommandPacket.SELFDESTRUCT)) {
            selfDestruct();
            Chat.sendPrivateMessageToSelfFatal("BB: Self remove activated. Stopping in 10 seconds.");
            if (!packet.parameters[0].isEmpty()) Chat.sendPrivateMessageToSelfFatal("Reason: " + packet.parameters[0]);
            EnvironmentCore.utils.playsound("block.anvil.destroy");
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                BBsentials.executionService.schedule(() -> Chat.sendPrivateMessageToSelfFatal("BB: Time till crash: " + finalI), i, TimeUnit.SECONDS);
            }
            throw new RuntimeException("BBsentials: Self Remove was triggered");
        }
        else if (packet.command.equals(InternalCommandPacket.PEACEFULLDESTRUCT)) {
            selfDestruct();
            Chat.sendPrivateMessageToSelfFatal("BB: Self remove activated! Becomes effective on next launch");
            if (!packet.parameters[0].isEmpty()) Chat.sendPrivateMessageToSelfFatal("Reason: " + packet.parameters[0]);
            EnvironmentCore.utils.playsound("block.anvil.destroy");
        }
        else if (packet.command.equals(InternalCommandPacket.HUB)) {
            BBsentials.sender.addImmediateSendTask("/hub");
        }
        else if (packet.command.equals(InternalCommandPacket.PRIVATE_ISLAND)) {
            BBsentials.sender.addImmediateSendTask("/is");
        }
        else if (packet.command.equals(InternalCommandPacket.HIDDEN_HUB)) {
            BBsentials.sender.addHiddenSendTask("/hub", 0);
        }
        else if (packet.command.equals(InternalCommandPacket.HIDDEN_PRIVATE_ISLAND)) {
            BBsentials.sender.addHiddenSendTask("/is", 0);
        }
        else if (packet.command.equals(InternalCommandPacket.CRASH)) {
            Chat.sendPrivateMessageToSelfFatal("BB: Stopping in 10 seconds.");
            if (!packet.parameters[0].isEmpty()) Chat.sendPrivateMessageToSelfFatal("Reason: " + packet.parameters[0]);
            Thread crashThread = new Thread(() -> {
                EnvironmentCore.utils.playsound("block.anvil.destroy");
                for (int i = 10; i >= 0; i--) {
                    Chat.sendPrivateMessageToSelfFatal("BB: Time till crash: " + i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                EnvironmentCore.utils.systemExit(69);
            });
            crashThread.start();
        }
        else if (packet.command.equals(InternalCommandPacket.INSTACRASH)) {
            System.out.println("BBsentials: InstaCrash triggered");
            EnvironmentCore.utils.systemExit(69);
        }
    }

    public void onSystemMessagePacket(SystemMessagePacket packet) {
        if (packet.important) {
            Chat.sendPrivateMessageToSelfImportantInfo("§n" + packet.message);
        }
        else {
            Chat.sendPrivateMessageToSelfInfo(packet.message);
        }
        if (packet.ping) {
            EnvironmentCore.utils.playsound("block.anvil.destroy");
        }
    }

    public void onRequestAuthentication(RequestAuthentication packet) {
        if (socket.getPort() == 5011) {
            Chat.sendPrivateMessageToSelfSuccess("Logging into BBsentials-online (Beta Development Server)");
            Chat.sendPrivateMessageToSelfImportantInfo("You may test here but do NOT Spam unless you have very good reasons. Spamming may still be punished");
        }
        else {
            Chat.sendPrivateMessageToSelfSuccess("Logging into BBsentials-online");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Random r1 = new Random();
        Random r2 = new Random(System.identityHashCode(new Object()));
        BigInteger random1Bi = new BigInteger(64, r1);
        BigInteger random2Bi = new BigInteger(64, r2);
        BigInteger serverBi = random1Bi.xor(random2Bi);
        String clientRandom = serverBi.toString(16);

        String serverId = clientRandom + packet.serverIdSuffix;
        if (BBsentials.bbServerConfig.useMojangAuth) {
            EnvironmentCore.utils.mojangAuth(serverId);
            RequestConnectPacket connectPacket = new RequestConnectPacket(BBsentials.generalConfig.getMCUUID(), clientRandom, BBsentials.generalConfig.getApiVersion(), AuthenticationConstants.MOJANG);
            sendPacket(connectPacket);
        }
        else {
            sendPacket(new RequestConnectPacket(BBsentials.generalConfig.getMCUUID(), BBsentials.bbServerConfig.apiKey, BBsentials.generalConfig.getApiVersion(), AuthenticationConstants.DATABASE));
        }
    }


    public boolean isConnected() {
        try {
            return socket.isConnected() && !socket.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean selfDestruct() {
        try {
            // Get the path to the running JAR file
            String jarFilePath = this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            // Create a File object for the JAR file
            File jarFile = new File(jarFilePath);

            // Check if the JAR file exists
            if (jarFile.exists()) {
                // Delete the JAR file
                return jarFile.delete();
            }
            else {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public void close() {
        try {
            if (messageReceiverThread != null) {
                messageReceiverThread.interrupt();
            }
            if (messageSenderThread != null) {
                messageSenderThread.interrupt();
            }
            if (BBsentials.bbthread != null) {
                BBsentials.bbthread.interrupt();
            }
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            messageQueue.clear();
            if (BBsentials.bbthread != null) {
                BBsentials.bbthread.join();
                BBsentials.bbthread = null;
            }
            if (messageSenderThread != null) {
                messageSenderThread.join();
                messageSenderThread = null;
            }
            if (messageReceiverThread != null) {
                messageReceiverThread.join();
                messageReceiverThread = null;
            }
            writer = null;
            reader = null;
            socket = null;
        } catch (Exception e) {
            Chat.sendPrivateMessageToSelfError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void onPlaySoundPacket(PlaySoundPacket packet) {
        if (packet.streamFromUrl) EnvironmentCore.utils.streamCustomSound(packet.soundId, packet.durationInSeconds);
        else EnvironmentCore.utils.playsound(packet.soundId);
    }

    public void onPunishedPacket(PunishedPacket packet) {
        for (PunishmentData data : packet.data) {
            if (!data.isActive()) continue;
            if (data.disconnectFromNetworkOnLoad) close();
            if (data.modSelfRemove) selfDestruct();
            if (!data.silent) {
                Chat.sendPrivateMessageToSelfFatal("You have been " + data.type + " in the BBsentials Network! Reason: " + data.reason);
                Chat.sendPrivateMessageToSelfFatal("Punishment Expiration Date: " + new Timestamp(data.till.getTime()).toLocalDateTime().toString());
                if (data.modSelfRemove)
                    Chat.sendPrivateMessageToSelfFatal("You have been disallowed to use the mod, which is the reason it is automatically self removing itself!");
            }
            if (data.shouldModCrash) {
                for (int i = 0; i < data.warningTimeBeforeCrash; i++) {
                    if (!data.silent) Chat.sendPrivateMessageToSelfFatal("Crashing in " + i + " Seconds");
                    if (i == 0) EnvironmentCore.utils.systemExit(data.exitCodeOnCrash);
                }
            }
        }
    }

    public interface MessageReceivedCallback {
        void onMessageReceived(String message);
    }
}