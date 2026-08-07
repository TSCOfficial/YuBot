package ch.frily.yubot.listeners;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.*;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OnMessageReceived extends ListenerAdapter {

    private static OnMessageReceived instance;

    public static OnMessageReceived getInstance(){
        if (instance == null) {
            instance = new OnMessageReceived();
        }
        return instance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (event.getAuthor().isBot()) return;

            // Ticket funtions
            if (event.getChannel() instanceof TextChannel
                    && TicketManager.getInstance().isTicketchannel(event.getChannel().asTextChannel())) {
                Ticket ticket = TicketRepository.getTicketById(event.getChannel().getIdLong());

                // ticket claim function
                if (Util.isTeamMember(event.getMember())) {
                    ticket.claim(event.getMember());
                }

                // ticket owner activity
                if (ticket.isOwner(event.getMember())) {
                    TicketRepository.updateTicketLastActivityAt(ticket);
                }
            }

            // adtive-mod activity
            if (Util.isActiveMod(event.getMember())) {
                Closure.handleModActivity(event.getMember());
            }

            // Word-Chain game
            if (event.getChannel().getId().equals(EnvResolver.getString(EnvKey.CHANNEL_KETTENBRIEF))) {
                WordChainGame.handleWord(event);
            }

            // Handle absence notice
            int deleteNoticeDelay = 60; // in seconds

            event.getMessage().getMentions().getMembers().forEach(member -> {
                try {
                    List<Absence> todaysAbsences = AbsenceRepository.getAbsencesByMemberAndDateSpan(member, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
                    log.info("Found {} absences", todaysAbsences.size());
                    todaysAbsences = todaysAbsences.stream().filter(absence -> absence.absenceMessage() == true).toList();
                    log.info("Found {} absences with absence message", todaysAbsences.size());
                    todaysAbsences.stream().sorted(Comparator.comparing(Absence::toDateTime));
                    if (todaysAbsences.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        if (todaysAbsences.getFirst().toDateTime().equals(LocalDate.now().atTime(LocalTime.MAX))) {
                            sb.append(String.format("> **%s %s ist heute %s**", todaysAbsences.getFirst().type().getEmoji().getFormatted(), member.getAsMention(), todaysAbsences.getFirst().type().getLabel().toLowerCase())).append("\n");
                        } else {
                            sb.append(String.format("> **%s %s ist heute bis <t:%d:t> %s**", todaysAbsences.getFirst().type().getEmoji().getFormatted(), member.getAsMention(), Util.toEpochSeconds(todaysAbsences.getFirst().toDateTime()), todaysAbsences.getFirst().type().getLabel().toLowerCase())).append("\n");
                        }
                        if (Util.resolveCategory(event.getGuildChannel()) != null && Util.resolveCategory(event.getGuildChannel()).getId().equals(EnvResolver.getCategoryById(EnvKey.CATEGORY_TEAMBEREICH).getId())) {
                            sb.append(String.format("> Begründung: %s", todaysAbsences.getFirst().reason())).append("\n");
                        }
                        sb.append(String.format("-# *<:timer:1522290651742339122> Nachricht wird <t:%d:R> gelöscht.*", Util.toEpochSeconds(LocalDateTime.now().plusSeconds(deleteNoticeDelay)))).append("\n");

                        event.getMessage().reply(sb.toString()).setAllowedMentions(List.of()).queue(message -> {
                            message.delete().queueAfter(60, TimeUnit.SECONDS);
                        });
                    }

                } catch (Exception exception) {
                    ExceptionHandler.handle(exception);
                }

            });
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
