package ch.frily.yubot.service.forummanager.event;

import ch.frily.yubot.event.IEvent;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Prevent that message are sent in the create-interest post
 * <p>
 *     this can not be done by locking the post, because a locked post blocks button-interactions aswell
 * </p>
 */
public class PreventSendMessageEvent implements IEvent<MessageReceivedEvent> {
    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;

        if (castEvent.getGuildChannel().getId().equals("1574771101035139113")) {
            ((MessageReceivedEvent) event).getMessage().delete().queue();
        }
    }
}
