package ch.frily.yubot.event.listener;

import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.absence.Absence;
import ch.frily.yubot.database.repository.AbsenceRepository;
import ch.frily.yubot.service.activemod.Closure;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.service.profile.MessageHandling;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageReceivedListener extends ListenerAdapter {

    private static MessageReceivedListener instance;

    public static MessageReceivedListener getInstance(){
        if (instance == null) {
            instance = new MessageReceivedListener();
        }
        return instance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (!event.isFromGuild()) return;
            if (event.getAuthor().isBot()) return;

            EventRegistry.getInstance().dispatchEvent(event);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
