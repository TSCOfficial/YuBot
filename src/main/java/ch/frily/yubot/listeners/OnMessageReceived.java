package ch.frily.yubot.listeners;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.absence.Absence;
import ch.frily.yubot.feature.absence.AbsenceRepository;
import ch.frily.yubot.feature.activemod.Closure;
import ch.frily.yubot.feature.game.WordChainGame;
import ch.frily.yubot.feature.profile.ProfileRepository;
import ch.frily.yubot.feature.profile.Setting;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.feature.ticket.TicketManager;
import ch.frily.yubot.feature.ticket.TicketRepository;
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

            if (!event.isFromGuild()) return;
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
                        if (Util.resolveCategory(event.getGuildChannel()) != null && Util.resolveCategory(event.getGuildChannel()).getId().equals(EnvResolver.getCategoryById(EnvKey.CATEGORY_TEAMBEREICH).getId())) {
                            sb.append(String.format("> Begründung: %s", todaysAbsence.reason())).append("\n");
                        }


                        String customAbsenceNotice = ProfileRepository.getSetting(member, Setting.ABSENCE_NOTICE, String.class);
                        if (customAbsenceNotice != null) {
                            sb.append(String.format("> -# %s\n", customAbsenceNotice));
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
