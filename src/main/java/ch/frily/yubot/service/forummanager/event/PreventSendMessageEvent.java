package ch.frily.yubot.service.forummanager.event;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.event.IEvent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Prevent that message are sent in the create-interest post
 * <p>
 *     this can not be done by locking the post, because a locked post disables button-interactions automatically
 * </p>
 */
@Slf4j
public class PreventSendMessageEvent implements IEvent<MessageReceivedEvent> {
    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;
        if (castEvent.isFromThread()) {
            IThreadContainerUnion threadContainer = castEvent.getGuildChannel().asThreadChannel().getParentChannel();
            if (threadContainer.getId().equals(EnvKey.CHANNEL_SPECIALINTERESTS.getValue()) && castEvent.getGuildChannel().asThreadChannel().isPinned()) {
                castEvent.getMessage().delete().queue();
            }
        }
    }
}
