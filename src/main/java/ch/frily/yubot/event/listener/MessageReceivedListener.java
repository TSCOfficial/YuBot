package ch.frily.yubot.event.listener;

import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class MessageReceivedListener extends ListenerAdapter {

    private static MessageReceivedListener instance;

    public static MessageReceivedListener getInstance(){
        if (instance == null) {
            instance = new MessageReceivedListener();
        }
        return instance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (!event.isFromGuild()) return;
            if (event.getAuthor().isBot()) return;

            EventRegistry.getInstance().dispatchEvent(event);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
