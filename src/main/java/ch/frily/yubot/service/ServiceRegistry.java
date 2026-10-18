package ch.frily.yubot.service;

import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.service.absence.AbsenceService;
import ch.frily.yubot.service.activemod.ActivemodService;
import ch.frily.yubot.service.eventtool.EventReminderService;
import ch.frily.yubot.service.game.WordChainGameService;
import ch.frily.yubot.service.postoffice.PostOfficeService;
import ch.frily.yubot.service.profile.ProfileService;
import ch.frily.yubot.service.storage.SessionStorageService;
import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import ch.frily.yubot.interaction.contextmenu.ContextMenuRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import ch.frily.yubot.interaction.select.SelectRegistry;
import ch.frily.yubot.scheduler.SchedulerRegistry;
import ch.frily.yubot.util.EnvKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the features and connects them to the bot
 */
@Slf4j
public class ServiceRegistry {

    @Getter
    private List<Service> features;

    private static ServiceRegistry instance;
    public static ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    /**
     * Loads the services and registers their components
     * <p>
     *     This method registers the following components of the services:
     *     <ul>
     *         <li>Slashcommands & Slashcommand-Groups</li>
     *         <li>Context Menus, both User & Message</li>
     *         <li>Buttons</li>
     *         <li>Modals</li>
     *         <li>Select Menus of all type</li>
     *         <li>Schedules</li>
     *         <li>Events</li>
     *     </ul>
     *     Each registered component is then handled in their according register.
     * </p>
     */
    public void load() {
        this.features = List.of(
                TicketService.getInstance(),
                WordChainGameService.getInstance(),
                ActivemodService.getInstance(),
                AbsenceService.getInstance(),
                SessionStorageService.getInstance(),
                ProfileService.getInstance(),
                PostOfficeService.getInstance(),
                EventReminderService.getInstance()
        );


        ArrayList<CommandData> commands = new ArrayList<>();

        this.features.forEach(feature -> {
            log.info("Registering feature: " + feature.getClass().getSimpleName());
            List<SlashCommandData> slashcommands = SlashCommandRegistry.getInstance().registerCommands(feature.getSlashCommands());
            commands.addAll(slashcommands);
            List<SlashCommandData> slashcommandGroups = SlashCommandRegistry.getInstance().registerGroups(feature.getSlashCommandGroups());
            commands.addAll(slashcommandGroups);
            List<CommandData> ctxMenus = ContextMenuRegistry.getInstance().register(feature.getContextMenus());
            commands.addAll(ctxMenus);
            ButtonRegistry.getInstance().register(feature.getButtons());
            ModalRegistry.getInstance().register(feature.getModals());
            SelectRegistry.getInstance().register(feature.getSelects());
            SchedulerRegistry.getInstance().register(feature.getSchedulers());
            EventRegistry.getInstance().register(feature.getEvents());
        });


        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
        guild.updateCommands()
                .addCommands(commands)
                .queue();
    }
}
