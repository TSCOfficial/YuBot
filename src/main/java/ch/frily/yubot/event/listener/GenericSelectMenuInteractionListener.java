package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.select.SelectRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

@Slf4j
public class GenericSelectMenuInteractionListener extends ListenerAdapter {

    private static GenericSelectMenuInteractionListener instance;

    public static GenericSelectMenuInteractionListener getInstance() {
        if (instance == null) {
            instance = new GenericSelectMenuInteractionListener();
        }
        return instance;
    }

    @Override
    public void onGenericSelectMenuInteraction(@NonNull GenericSelectMenuInteractionEvent<?, ?> event) {
        try {
            SelectRegistry.getInstance().dispatchSelectInteraction(event);
        } catch (Exception exception) {
            log.error("Error while dispatching select '{}' interaction for {}: {}", event.getInteraction().getCustomId(), event.getUser().getEffectiveName(), exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }
}
