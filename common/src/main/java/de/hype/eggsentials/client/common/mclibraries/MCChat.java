package de.hype.eggsentials.client.common.mclibraries;

import de.hype.eggsentials.client.common.chat.Message;
import de.hype.eggsentials.client.common.client.BBsentials;

public interface MCChat {
    void init();
    void sendChatMessage(String message);
    void sendClientSideMessage(Message message);

    default void sendClientSideMessage(Message message, boolean actionbar) {
        if (BBsentials.funConfig.swapActionBarChat && !BBsentials.funConfig.swapOnlyNormal) {
            actionbar = !actionbar;
        }
        else {
            sendClientSideMessage(message);
        }
    }
    void showActionBar(Message message);
}
