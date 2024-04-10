package de.hype.eggsentials.shared.packets.network;

import de.hype.eggsentials.environment.packetconfig.AbstractPacket;
import de.hype.eggsentials.shared.objects.PunishmentData;

import java.util.ArrayList;
import java.util.List;

public class PunishedPacket extends AbstractPacket {
    public List<PunishmentData> data;

    public PunishedPacket(List<PunishmentData> punishments) {
        super(1, 1);
        data = new ArrayList<>(punishments);
    }
}
