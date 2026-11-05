package ch.frily.yubot.event.listener;

import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

public class MessageDeleteListener extends ListenerAdapter {

    private static MessageDeleteListener instance;

    public static MessageDeleteListener getInstance(){
        if (instance == null) {
            instance = new MessageDeleteListener();
        }
        return instance;
    }

    @Override
    public void onMessageDelete(net.dv8tion.jda.api.events.message.@NonNull MessageDeleteEvent event) {
        try {
            EventRegistry.getInstance().dispatchEvent(event);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
