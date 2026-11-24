package ch.frily.yubot.event.listener;

import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

public class ChannelDeleteListener extends ListenerAdapter {

    private static ChannelDeleteListener instance;

    public static ChannelDeleteListener getInstance() {
        if (instance == null) {
            instance = new ChannelDeleteListener();
        }
        return instance;
    }

    @Override
    public void onChannelDelete(@NonNull ChannelDeleteEvent event) {
        try {
            EventRegistry.getInstance().dispatchEvent(event);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
