package ch.frily.yubot.listeners;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class InteractionListener extends ListenerAdapter {

    private static InteractionListener instance;

    public static InteractionListener getInstance() {
        if (instance == null) {
            instance = new InteractionListener();
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
            ExceptionHandler.handle(exception, event);
        }
    }

    /**
     * Execute the autocompletion for a slash command
     * @param event When an autocompletion is triggered by discord
     */
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        SlashCommandRegistry.getInstance().dispatchAutocompleteEvent(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event){
        try {
            ButtonRegistry.getInstance().dispatchButtonInteraction(event);
        }catch (Exception exception) {
            log.error("Error while dispatching button interaction: {}", exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        try {
            ModalRegistry.getInstance().dispatchModalInteraction(event);
        } catch (Exception exception) {
            log.debug("Error while dispatching modal interaction: {}", exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }


}
