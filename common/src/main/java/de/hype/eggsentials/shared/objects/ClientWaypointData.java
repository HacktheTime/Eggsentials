package de.hype.eggsentials.shared.objects;


import de.hype.eggsentials.environment.packetconfig.EnvironmentPacketConfig;

import java.awt.*;
import java.util.List;

public class ClientWaypointData extends WaypointData {
    public static int counter = 0;
    protected int waypointId = counter++;

    public ClientWaypointData(Position pos, String jsonTextToRender, int renderDistance, boolean visible, boolean deleteOnServerSwap, RenderInformation render, Color color, boolean doTracer) {
        super(pos, jsonTextToRender, renderDistance, visible, deleteOnServerSwap, render, color, doTracer);
    }

    public ClientWaypointData(Position pos, String jsonTextToRender, int renderDistance, boolean visible, boolean deleteOnServerSwap, RenderInformation render) {
        super(pos, jsonTextToRender, renderDistance, visible, deleteOnServerSwap, render, EnvironmentPacketConfig.getDefaultWaypointColor(), EnvironmentPacketConfig.getWaypointDefaultWithTracer());
    }

    public ClientWaypointData(Position pos, String jsonTextToRender, int renderDistance, boolean visible, boolean deleteOnServerSwap, List<RenderInformation> render, Color color, boolean doTracer) {
        super(pos, jsonTextToRender, renderDistance, visible, deleteOnServerSwap, render, color, doTracer);
    }

    public ClientWaypointData(Position pos, String jsonTextToRender, int renderDistance, boolean visible, boolean deleteOnServerSwap, List<RenderInformation> render) {
        super(pos, jsonTextToRender, renderDistance, visible, deleteOnServerSwap, render, EnvironmentPacketConfig.getDefaultWaypointColor(), EnvironmentPacketConfig.getWaypointDefaultWithTracer());
    }


    public int getWaypointId() {
        return waypointId;
    }


}
