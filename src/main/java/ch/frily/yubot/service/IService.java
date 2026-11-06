package ch.frily.yubot.service;

import ch.frily.yubot.database.IRepository;
import ch.frily.yubot.embed.IEmbed;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.contextmenu.IContextMenu;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.scheduler.IScheduler;
import net.dv8tion.jda.api.events.Event;

import java.util.List;

/**
 * Basic feature class
 * <p>
 *     This class is used to define a new feature and contains all connected elements such as slashcommands, modals, schedulers, ...
 * </p>
 */
public interface IService {

    /**
     * Get the name of this service
     * @return
     */
    String getName();

    /**
     * Get the description of this service
     * @return
     */
    String getDescription();

    /**
     * Get slashcommands connected to this service
     * <p>
     *     These slashcommands ony habe one layer: <code>/command</code>
     * </p>
     * @return
     */
    default List<ISlashCommand> getSlashCommands() {
        return List.of();
    }

    /**
     * Get the slashcommand-groups
     * <p>
     *     These can be <code>/group command</code> and <code>/group subgroup command</code>
     * </p>
     * @return
     */
    default List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of();
    }

    /**
     * Get any button components connected to this service
     * @return
     */
    default List<Button> getButtons() {
        return List.of();
    }

    /**
     * Get any modals connected to this service
     * @return
     */
    default List<Modal> getModals() {
        return List.of();
    }

    /**
     * Get any selectmenu connected to this service
     * <p>
     *     This contains any type of selectmenu. Whether string or entity
     * </p>
     * @return
     */
    default List<ISelect> getSelects() {
        return List.of();
    }

    /**
     * Get any schedulers connected to this service
     * @return
     */
    default List<IScheduler> getSchedulers() {
        return List.of();
    }

    /**
     * Get any contextmenu connected to this service
     * <p>
     *     This contains both types, message context & user context
     * </p>
     * @return
     */
    default List<IContextMenu> getContextMenus() {
        return List.of();
    }

    /**
     * Get any event connected to this service
     * @return
     */
    default List<IEvent<? extends Event>> getEvents() {
        return List.of();
    }

    /**
     * Get any repository connected to this service
     * @return
     */
    default List<IRepository> getRepositories() {
        return List.of();
    }
}
