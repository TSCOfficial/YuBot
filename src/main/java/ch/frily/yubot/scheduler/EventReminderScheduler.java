package ch.frily.yubot.scheduler;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.database.repository.EventReminderRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
public class EventReminderScheduler implements IScheduler{
    @Override
    public String cronExpression() {
        return "0/15 * * * *"; // 0/15
    }

    @Override
    public void execute() throws SQLException, ClassNotFoundException {
        log.info("Executing event reminder scheduler");
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        List<ScheduledEvent> upcomingEvents = guild.getScheduledEvents().stream().filter(event -> {
            try {
                log.info("Checking event: {}", event.getName());
                if (event.getStatus() != ScheduledEvent.Status.SCHEDULED) {
                    EventReminderRepository.delete(event.getId());
                    return false;
                }
                LocalDateTime startTime = event.getStartTime().atZoneSameInstant(ZoneId.of("Europe/Zurich")).toLocalDateTime();
                LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                if (event.getStatus().equals(ScheduledEvent.Status.SCHEDULED) && (!startTime.isBefore(now) && !startTime.isAfter(now.plusHours(2)))) {
                        Map<String, Boolean> reminderEntry = EventReminderRepository.getEvent(event.getId());
                        return reminderEntry != null ? !reminderEntry.get(event.getId()) : true;
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
            return false;
        }).toList();

        upcomingEvents.forEach(event -> {
            sendReminder(event);
        });
    }

    private void sendReminder(ScheduledEvent event) {
        TextChannel channel = EnvResolver.getChannelById(TextChannel.class, 1045025807065698314L, 1516079055693287545L);
        StringBuilder reminderSB = new StringBuilder();
        reminderSB.append(EnvResolver.getRoleById(EnvKey.ROLE_EVENTPING).getAsMention()).append("\n");
        reminderSB.append(String.format("# __%s__ startet in <t:%d:R>", event.getName(), Util.toEpochSeconds(event.getStartTime().atZoneSameInstant(ZoneId.of("Europe/Zurich")).toLocalDateTime()))).append("\n");
        reminderSB.append(event.getDescription()).append("\n");
        if (event.getType() == ScheduledEvent.Type.STAGE_INSTANCE || event.getType() == ScheduledEvent.Type.VOICE) {
            reminderSB.append(String.format("-# %s | %s", event.getChannel().getJumpUrl(), event.getJumpUrl()));
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
