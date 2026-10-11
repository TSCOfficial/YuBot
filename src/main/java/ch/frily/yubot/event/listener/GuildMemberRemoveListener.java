package ch.frily.yubot.event.listener;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

public class GuildMemberRemoveListener extends ListenerAdapter {

    private static GuildMemberRemoveListener instance;
    public static GuildMemberRemoveListener getInstance() {
        if (instance == null) {
            instance = new GuildMemberRemoveListener();
        }
        return instance;
    }

    @Override
    public void onGuildMemberRemove(@NonNull GuildMemberRemoveEvent event) {
        try {
            EventRegistry.getInstance().dispatchEvent(event);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
