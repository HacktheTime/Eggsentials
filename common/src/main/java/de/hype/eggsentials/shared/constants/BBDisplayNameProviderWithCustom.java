package de.hype.eggsentials.shared.constants;

public interface BBDisplayNameProviderWithCustom<T extends Enum<T>> extends BBDisplayNameProvider {
    T setDisplayName(String displayname);

}
