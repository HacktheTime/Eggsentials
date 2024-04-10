package de.hype.eggsentials.shared.packets.network;


import de.hype.eggsentials.environment.packetconfig.AbstractPacket;

/**
 * Used when a Message is broadcast. Requires Mod Privilege.
 */
public class BroadcastMessagePacket extends AbstractPacket {

    public final String message;
    public final String username;
    public final String prefix;

    public BroadcastMessagePacket(String prefix, String username, String message) {
        super(1, 1); //Min and Max supported Version
        this.message = message;
        this.username = username;
        this.prefix = prefix;
    }
}
