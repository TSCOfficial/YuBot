package ch.frily.yubot.service.eventtool.schedule;

import ch.frily.yubot.database.repository.EventReminderRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
public class EventReminderScheduler implements IScheduler {
    @Override
    public String cronExpression() {
        return "0/15 * * * *";
    }

    @Override
    public void execute() {
        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
        List<ScheduledEvent> upcomingEvents = guild.getScheduledEvents().stream().filter(event -> {
            log.info("Processing event: {}", event.getName());
            // remove already reminded events from list by returning false
            try {
                if (event.getStatus() != ScheduledEvent.Status.SCHEDULED) {
                    EventReminderRepository.delete(event.getId());
                    return false;
                }
                LocalDateTime startTime = event.getStartTime().atZoneSameInstant(ZoneId.of("Europe/Zurich")).toLocalDateTime();
                LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                if (event.getStatus().equals(ScheduledEvent.Status.SCHEDULED) && (!startTime.isBefore(now) && !startTime.isAfter(now.plusHours(2)))) {
                    try {
                        boolean isSent = EventReminderRepository.reminderAlreadySent(event.getId());
                        log.info("reminder already sent: {}", isSent);
                        return !isSent;

                    } catch (NotFoundException e) {
                        log.info("Event reminder not found, sending reminder");
                        return true;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
            return false;
        }).toList();

        upcomingEvents.forEach(this::sendReminder);
    }

    private void sendReminder(ScheduledEvent event) {
        log.info("Sending reminder for event {}", event.getId());
        TextChannel channel = EnvKey.CHANNEL_ANKUENDIGUNGEN.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER);
        StringBuilder reminderSB = new StringBuilder();
        reminderSB.append(EnvKey.ROLE_EVENTPING.getAsRole().getAsMention()).append("\n");
        reminderSB.append(String.format("# __%s__ startet in <t:%d:R>", event.getName(), Util.toEpochSeconds(event.getStartTime().atZoneSameInstant(ZoneId.of("Europe/Zurich")).toLocalDateTime()))).append("\n");
        reminderSB.append(event.getDescription()).append("\n");
        if (event.getType() == ScheduledEvent.Type.STAGE_INSTANCE || event.getType() == ScheduledEvent.Type.VOICE) {
            reminderSB.append(String.format("-# %s | %s", Objects.requireNonNull(event.getChannel()).getJumpUrl(), event.getJumpUrl()));
        } else if (event.getType() == ScheduledEvent.Type.EXTERNAL) {
            reminderSB.append(String.format("-# %s | %s", event.getLocation(), event.getJumpUrl()));
        } else {
            reminderSB.append(String.format("-# %s", event.getJumpUrl()));
        }
        channel.sendMessage(reminderSB.toString()).queue(ThrowingConsumer.wrap(null, _ -> {
            EventReminderRepository.setStatus(event.getId(), true);
        }));
    }
}
