package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class SlashCommandInteractionListener extends ListenerAdapter {

    private static SlashCommandInteractionListener instance;

    public static SlashCommandInteractionListener getInstance() {
        if (instance == null) {
            instance = new SlashCommandInteractionListener();
        }
        return instance;
    }

    /**
     * Gets triggered as soon as a slash command is executed
     * @param event SlashCommandInteractionEvent
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            SlashCommandRegistry.getInstance().dispatchInteractionEvent(event);
        } catch (Exception exception) {
            log.error("Error while dispatching slashcommand '{}' interaction for {}: {}", event.getInteraction().getFullCommandName(), event.getMember().getEffectiveName(), exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }
}
