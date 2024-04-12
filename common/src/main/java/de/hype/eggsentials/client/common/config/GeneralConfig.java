package de.hype.eggsentials.client.common.config;

import com.google.gson.JsonObject;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;

import java.util.HashSet;
import java.util.Set;


public class GeneralConfig extends BBsentialsConfig {

    public transient int apiVersion = 1;
    public String[] bbsentialsRoles = {""};
    public boolean useNumCodes = false;
    public JsonObject recentBingoData = null;
    public Set<String> profileIds = new HashSet<>();
    public Boolean showNewEggFindigs = true;

    public GeneralConfig() {
        super(1);
        doInit();
    }

    public boolean hasBBRoles(String roleName) {
        if (roleName == null) return true;
        if (roleName.isEmpty()) return true;
        for (String role : bbsentialsRoles) {
            if (role.equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }

    public String getMCUUID() {
        return EnvironmentCore.utils.getMCUUID().replace("-", "");
    }

    public String getUsername() {
        return EnvironmentCore.utils.getUsername();
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setDefault() {

    }

    public void onInit() {
    }
}
