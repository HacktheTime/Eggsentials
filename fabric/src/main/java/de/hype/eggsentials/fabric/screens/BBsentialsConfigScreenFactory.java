package de.hype.eggsentials.fabric.screens;

import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.config.ConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BBsentialsConfigScreenFactory {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("BBsentials ConfigManager"));
        builder.setSavingRunnable(ConfigManager::saveAll);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory base = builder.getOrCreateCategory(Text.of("basr"));
        base.addEntry(entryBuilder.startBooleanToggle(Text.of("Show messages for findings"), BBsentials.generalConfig.showNewEggFindigs)
                .setDefaultValue(true)
                .setTooltip(Text.of("Do you want findings to be announced in Chat?"))
                .setSaveConsumer(newValue -> BBsentials.generalConfig.showNewEggFindigs = newValue)
                    .requireRestart()
                    .build());
        return builder.build();
    }
}
