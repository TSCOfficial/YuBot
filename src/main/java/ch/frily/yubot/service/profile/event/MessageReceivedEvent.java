package ch.frily.yubot.service.profile.event;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.profile.MessageHandling;
import net.dv8tion.jda.api.events.Event;

import java.io.IOException;
import java.sql.SQLException;

public class MessageReceivedEvent implements IEvent<net.dv8tion.jda.api.events.message.MessageReceivedEvent> {

    private static MessageReceivedEvent instance;

    public static MessageReceivedEvent getInstance() {
        if (instance == null) {
            instance = new MessageReceivedEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return net.dv8tion.jda.api.events.message.MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        net.dv8tion.jda.api.events.message.MessageReceivedEvent castEvent = (net.dv8tion.jda.api.events.message.MessageReceivedEvent) event;
        MessageHandling.handleIncomingMessage(castEvent.getMessage());
    }
}
