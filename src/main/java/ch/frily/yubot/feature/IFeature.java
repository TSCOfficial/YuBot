package ch.frily.yubot.feature;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.embed.IEmbed;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.contextmenu.IContextMenu;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.scheduler.IScheduler;

import java.util.List;

/**
 * Basic feature class
 * <p>
 *     This class is used to define a new feature and contains all connected elements scuh as slashcommands, modals, schedulers, ...
 * </p>
 */
public interface IFeature {

    IFeature getInstance();

    String getName();

    default String getDescription() {
        return null;
    }

    default List<ISlashCommand> getSlashCommands() {
        return List.of();
    };

    default List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of();
    };

    default List<IButton> getButtons() {
        return List.of();
    };

    default List<Modal> getModals() {
        return List.of();
    };

    default List<ISelect> getSelects() {
        return List.of();
    };

    default List<IScheduler> getSchedulers() {
        return List.of();
    };

    default List<Container> getContainers() {
        return List.of();
    };

    default List<IContextMenu> getContextMenus() {
        return List.of();
    };
}
