package ch.frily.yubot.service.game.event;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.game.WordChainGameService;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WordInputEvent implements IEvent<MessageReceivedEvent> {

    private static WordInputEvent instance;
    public static WordInputEvent getInstance() {
        if (instance == null) {
            instance = new WordInputEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;

        if (castEvent.getChannel().getId().equals(EnvKey.CHANNEL_KETTENBRIEF.getValue())) {
            WordChainGameService.handleWord(castEvent);
        }
    }
}
