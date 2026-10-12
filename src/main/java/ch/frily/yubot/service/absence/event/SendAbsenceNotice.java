package ch.frily.yubot.service.absence.event;

import ch.frily.yubot.database.repository.AbsenceRepository;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.absence.Absence;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendAbsenceNotice implements IEvent<MessageReceivedEvent> {

    private static SendAbsenceNotice instance;

    public static SendAbsenceNotice getInstance() {
        if (instance == null) {
            instance = new SendAbsenceNotice();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;
        int deleteNoticeDelay = 60; // in seconds

        castEvent.getMessage().getMentions().getMembers().forEach(member -> {
            try {
                List<Absence> todaysAbsences = AbsenceRepository.getAbsencesByMemberAndDateSpan(member, LocalDateTime.now(), LocalDateTime.now());
                if (todaysAbsences.isEmpty()) return;

                Absence todaysAbsence = AbsenceRepository.groupByDay(todaysAbsences).get(LocalDate.now()).getFirst();
                if (todaysAbsence.absenceMessage()) {
                    StringBuilder sb = new StringBuilder();
                    if (todaysAbsence.toDateTime().equals(LocalDate.now().atTime(LocalTime.MAX))) {
                        sb.append(String.format("> **%s %s ist heute %s**", todaysAbsence.type().getEmoji().getFormatted(), member.getAsMention(), todaysAbsence.type().getLabel().toLowerCase())).append("\n");
                    } else {
                        sb.append(String.format("> **%s %s ist heute bis <t:%d:t> %s**", todaysAbsence.type().getEmoji().getFormatted(), member.getAsMention(), Util.toEpochSeconds(todaysAbsence.toDateTime()), todaysAbsence.type().getLabel().toLowerCase())).append("\n");
                    }
                    if (Util.resolveCategory(castEvent.getGuildChannel()) != null && Util.resolveCategory(castEvent.getGuildChannel()).getId().equals(EnvResolver.getCategoryById(EnvKey.CATEGORY_TEAMBEREICH).getId())) {
                        sb.append(String.format("> Begründung: %s", todaysAbsence.reason())).append("\n");
                    }


                    String customAbsenceNotice = SettingRepository.getSetting(member, Setting.ABSENCE_NOTICE, String.class);
                    if (customAbsenceNotice != null) {
                        sb.append(String.format("> -# %s\n", customAbsenceNotice));
                    }
                    sb.append(String.format("-# *<:timer:1522290651742339122> Nachricht wird <t:%d:R> gelöscht.*", Util.toEpochSeconds(LocalDateTime.now().plusSeconds(deleteNoticeDelay)))).append("\n");
                    castEvent.getMessage().reply(sb.toString()).setAllowedMentions(List.of()).queue(message -> {
                        message.delete().queueAfter(deleteNoticeDelay, TimeUnit.SECONDS);
                    });
                }

            } catch (Exception exception) {
                ExceptionHandler.handle(exception);
            }

        });
    }
}
