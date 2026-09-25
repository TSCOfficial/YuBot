package ch.frily.yubot.feature;

import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import ch.frily.yubot.interaction.contextmenu.ContextMenuRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import ch.frily.yubot.interaction.select.SelectRegistry;
import ch.frily.yubot.scheduler.SchedulerRegistry;
import lombok.Getter;

import java.util.List;

/**
 * Registers the features and connects them to the bot
 */
public class FeatureRegistry {

    @Getter
    private List<IFeature> features;

    private static FeatureRegistry instance;
    public static FeatureRegistry getInstance() {
        if (instance == null) {
            instance = new FeatureRegistry();
        }
        return instance;
    }

    /**
     * Register all features and register their content to their appropriate registry
     */
    public void register() {
        this.features = List.of();


        this.features.forEach(feature -> {
            SlashCommandRegistry.getInstance().registerCommands(feature.getSlashCommands());
            SlashCommandRegistry.getInstance().registerGroups(feature.getSlashCommandGroups());
            ButtonRegistry.getInstance().register(feature.getButtons());
            ModalRegistry.getInstance().register(feature.getModals());
            SelectRegistry.getInstance().register(feature.getSelects());
            SchedulerRegistry.getInstance().register(feature.getSchedulers());
            ContextMenuRegistry.getInstance().register(feature.getContextMenus());
        });
    }
}
