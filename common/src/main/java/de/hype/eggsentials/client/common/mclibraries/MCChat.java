package de.hype.eggsentials.client.common.mclibraries;

import de.hype.eggsentials.client.common.chat.Message;

public interface MCChat {
    void init();
    void sendChatMessage(String message);
    void sendClientSideMessage(Message message);

    default void sendClientSideMessage(Message message, boolean actionbar) {
            sendClientSideMessage(message);
    }
    void showActionBar(Message message);
}
