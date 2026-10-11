package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class CommandAutoCompleteInteractionListener extends ListenerAdapter {

    private static CommandAutoCompleteInteractionListener instance;

    public static CommandAutoCompleteInteractionListener getInstance() {
        if (instance == null) {
            instance = new CommandAutoCompleteInteractionListener();
        }
        return instance;
    }

    /**
     * Execute the autocompletion for a slash command
     * @param event When an autocompletion is triggered by discord
     */
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        try {
            SlashCommandRegistry.getInstance().dispatchAutocompleteEvent(event);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception, null);
        }
    }
}
