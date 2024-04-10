package de.hype.eggsentials.environment.addonpacketconfig;

public class AbstractAddonPacket {
    public final int apiVersionMin;
    public final int apiVersionMax;

    protected AbstractAddonPacket(int apiVersionMin, int apiVersionMax) {
        this.apiVersionMax = apiVersionMax;
        this.apiVersionMin = apiVersionMin;

    }
}