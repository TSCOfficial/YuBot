package ch.frily.yubot.service.profile.event;

import ch.frily.yubot.service.profile.repository.ProfileMessageRepository;
import ch.frily.yubot.event.IEvent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.Event;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class MessageDeleteEvent implements IEvent<net.dv8tion.jda.api.events.message.MessageDeleteEvent> {

    private static MessageDeleteEvent instance;

    public static MessageDeleteEvent getInstance() {
        if (instance == null) {
            instance = new MessageDeleteEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return net.dv8tion.jda.api.events.message.MessageDeleteEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        net.dv8tion.jda.api.events.message.MessageDeleteEvent castEvent = (net.dv8tion.jda.api.events.message.MessageDeleteEvent) event;
        if (ProfileMessageRepository.exists(castEvent.getChannel().getId(), castEvent.getMessageId())) {
            log.info("Message deleted: {}", castEvent.getMessageId());
            ProfileMessageRepository.delete(castEvent.getChannel().getId(), castEvent.getMessageId());
        } else {
            log.info("Message is not webhook");
        }

    }
}
