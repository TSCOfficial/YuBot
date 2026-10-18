package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.contextmenu.ContextMenuRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ButtonInteractionListener extends ListenerAdapter {

    private static ButtonInteractionListener instance;

    public static ButtonInteractionListener getInstance() {
        if (instance == null) {
            instance = new ButtonInteractionListener();
        }
        return instance;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event){
        try {
            ButtonRegistry.getInstance().dispatchButtonInteraction(event);
        } catch (Exception exception) {
            log.error("Error while dispatching button '{}' interaction for {}: {}", event.getButton().getCustomId(), event.getUser().getGlobalName(), exception.getMessage());
            ExceptionHandler.handle(exception, event);
        }
    }
}
