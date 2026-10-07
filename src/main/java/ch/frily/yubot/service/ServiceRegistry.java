package ch.frily.yubot.service;

import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import ch.frily.yubot.interaction.contextmenu.ContextMenuRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import ch.frily.yubot.interaction.select.SelectRegistry;
import ch.frily.yubot.scheduler.SchedulerRegistry;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the features and connects them to the bot
 */
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
     * Loads the features and registers them
     * <p>
     *     This also automatically registers the Slashcommands & Context-Menus.
     * </p>
     */
    public void load() {
        this.features = List.of(
                new TicketService()
        );


        ArrayList<CommandData> commands = new ArrayList<>();

        this.features.forEach(feature -> {
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
        });


        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        guild.updateCommands()
                .addCommands(commands)
                .queue();
    }
}
