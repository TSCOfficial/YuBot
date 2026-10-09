package ch.frily.yubot.service.game;

import ch.frily.yubot.listeners.IListener;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WordListener implements IListener<MessageReceivedEvent> {
    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;

        if (castEvent.getChannel().getId().equals(EnvResolver.getString(EnvKey.CHANNEL_KETTENBRIEF))) {
            WordChainGame.handleWord(castEvent);
        }
    }
}
