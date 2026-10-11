package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ModalInteractionListener extends ListenerAdapter {

    private static ModalInteractionListener instance;

    public static ModalInteractionListener getInstance() {
        if (instance == null) {
            instance = new ModalInteractionListener();
        }
        return instance;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        try {
            ModalRegistry.getInstance().dispatchModalInteraction(event);
        } catch (Exception exception) {
            log.error("Error while dispatching modal '{}' interaction for {}: {}", event.getModalId(), event.getUser().getEffectiveName(), exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }
}
