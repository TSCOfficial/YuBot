package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import ch.frily.yubot.interaction.contextmenu.ContextMenuRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class GenericContextInteractionListener extends ListenerAdapter {

    private static GenericContextInteractionListener instance;

    public static GenericContextInteractionListener getInstance() {
        if (instance == null) {
            instance = new GenericContextInteractionListener();
        }
        return instance;
    }

    /**
     * Execute a context menu interaction
     * <br>
     * Both context menu types (USER & MESSAGE) are caught here
     * @param event GenericContextInteractionEvent
     */
    @Override
    public void onGenericContextInteraction(@NotNull GenericContextInteractionEvent<?> event) {
        try {
            ContextMenuRegistry.getInstance().dispatchInteractionEvent(event);
        } catch (Exception exception) {
            log.error("Error while dispatching '{}' context interaction executed by {}: {}", event.getInteraction().getFullCommandName(), event.getUser().getEffectiveName(), exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }
}
