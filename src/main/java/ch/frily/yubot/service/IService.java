package ch.frily.yubot.service;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.contextmenu.IContextMenu;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.listeners.IListener;
import ch.frily.yubot.scheduler.IScheduler;

import java.util.List;

/**
 * Basic feature class
 * <p>
 *     This class is used to define a new feature and contains all connected elements such as slashcommands, modals, schedulers, ...
 * </p>
 */
public interface IService {

    String getName();

    String getDescription();

    default List<ISlashCommand> getSlashCommands() {
        return List.of();
    }

    default List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of();
    }

    default List<Button> getButtons() {
        return List.of();
    }

    default List<Modal> getModals() {
        return List.of();
    }

    default List<ISelect> getSelects() {
        return List.of();
    }

    default List<IScheduler> getSchedulers() {
        return List.of();
    }

    default List<IContextMenu> getContextMenus() {
        return List.of();
    }

    default List<IListener> getListeners() {
        return List.of();
    }
}
