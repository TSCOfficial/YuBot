package ch.frily.yubot.service.forummanager.event;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class InterestPostDeleteEvent implements IEvent<ChannelDeleteEvent> {
    @Override
    public Class<? extends Event> getListenerType() {
        return ChannelDeleteEvent.class;
    }

    @Override
    public void execute(Event event) throws SQLException, IOException {
        ChannelDeleteEvent castEvent = (ChannelDeleteEvent) event;
        if (castEvent.isFromType(ChannelType.GUILD_PUBLIC_THREAD)) {
            SpecialInterestRepository.deleteByPostId(castEvent.getChannel().getId());
        }
    }
}
