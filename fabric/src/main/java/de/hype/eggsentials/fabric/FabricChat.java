package de.hype.eggsentials.fabric;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.chat.Message;
import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.mclibraries.MCChat;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FabricChat implements MCChat {
    public Chat chat = new Chat();

    public FabricChat() {
        init();
    }

    public void init() {
        // Register a callback for a custom message type
//        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
//            chat.onEvent(new Message(Text.Serialization.toJsonString(message), message.getString()));
//        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, isActionBar) -> {
            boolean block = (Eggsentials.chat.processNotThreaded(new Message(Text.Serialization.toJsonString(message), message.getString()), isActionBar) == null);
            return !block;
        });
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::prepareOnEvent);

    }

    public Text prepareOnEvent(Text text, boolean actionbar) {
        String json = Text.Serialization.toJsonString(text);
        Message message = new Message(json, text.getString(), actionbar);

        if (!actionbar) {
            message = chat.onEvent(message, actionbar);
        }
        Text toReturn = null;
        try {
            toReturn = Text.Serialization.fromJson(message.getJson());
        } catch (Exception e) {
            Chat.sendPrivateMessageToSelfError("Text: " + Text.Serialization.toJsonString(text) + " Error: " + e.getMessage());
            e.printStackTrace();
        }
        return toReturn;
    }

    public void sendClientSideMessage(Message message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.Serialization.fromJson(message.getJson()));
        }
    }

    public void showActionBar(Message message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (Eggsentials.overwriteActionBar.isEmpty())
                message = Message.of(Eggsentials.overwriteActionBar);
            client.player.sendMessage(Text.Serialization.fromJson(message.getJson()), true);
        }
    }

    public void sendChatMessage(String message) {
        MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(message);
    }
}
