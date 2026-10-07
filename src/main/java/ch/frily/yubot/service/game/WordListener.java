package ch.frily.yubot.service.game;

import ch.frily.yubot.listeners.IListener;
import ch.frily.yubot.listeners.ListenerType;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WordListener implements IListener<MessageReceivedEvent> {
    @Override
    public ListenerType getListenerType() {
        return ListenerType.MESSAGE_RECEIVED;
    }

    @Override
    public <T extends Event> void execute(T event) {
// Word-Chain game
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;
        if (castEvent.getChannel().getId().equals(EnvResolver.getString(EnvKey.CHANNEL_KETTENBRIEF))) {
            WordChainGame.handleWord(castEvent);
        }
    }
}
