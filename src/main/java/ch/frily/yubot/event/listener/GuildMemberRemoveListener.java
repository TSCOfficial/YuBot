package ch.frily.yubot.event.listener;

import ch.frily.yubot.database.repository.TicketRepository;
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
            if (event.getMember() != null) {
                TicketRepository.getTicketsByUser(event.getMember().getUser()).forEach(ticket -> {
                    try {
                        ticket.getChannel().sendMessage("Der Benutzer hat den Server verlassen. Das Ticket wird geschlossen.").queue();
                        ticket.close(null, true);
                    } catch (Exception exception) {
                        ExceptionHandler.handle(exception);
                    }
                });
            }

        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
