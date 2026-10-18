package ch.frily.yubot.service.activemod.event;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.activemod.Closure;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;

public class AutoActivityProveEvent implements IEvent<MessageReceivedEvent> {
    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;
        if (Util.isActiveMod(castEvent.getMember())) {
            Closure.handleModActivity(castEvent.getMember());
        }
    }
}
