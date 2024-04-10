package de.hype.eggsentials.fabric;

import de.hype.eggsentials.client.common.api.ISimpleOption;
import net.minecraft.client.MinecraftClient;

public class Options implements de.hype.eggsentials.client.common.mclibraries.Options {
    public void setFov(int value) {
        ((ISimpleOption) (Object) MinecraftClient.getInstance().options.getFov()).set(value);
    }
    public void setGamma(double value) {
        ((ISimpleOption) (Object) MinecraftClient.getInstance().options.getGamma()).set(value);
    }
}

