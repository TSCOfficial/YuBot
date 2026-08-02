package ch.frily.yubot.container;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.interaction.button.btn.AbsenceAddBtn;
import ch.frily.yubot.interaction.button.btn.AbsenceDetailBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AbsenceOverviewContainer extends Container {

    /** max componentcount may be filled before the overview gets truncated */
    private static final int MAX_DISPLAYED_COMPONENTS = 30;

    /** Roles that are taken in account for when displaying the roles of an absenced member */
    private static final List<Role> DISPLAYABLE_ROLES = Stream.of(
            EnvKey.ROLE_OWNER,
            EnvKey.ROLE_SERVERLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SUPPORT,
            EnvKey.ROLE_AWARENESS,
            EnvKey.ROLE_DEVELOPER,
            EnvKey.ROLE_EVENT
    ).map(EnvResolver::getRoleById).toList();

    /**
     * All absences split up into day-absences, grouped by the day they belong to
     * <p></p>
     * A multi-day absence appears once per day it covers while sharing the same {@link Absence#id()}.
     */
    @Getter
    private Map<LocalDate, List<Absence>> dayAbsences = new TreeMap<>();

    public AbsenceOverviewContainer() {
        try {
            List<Absence> absences = AbsenceRepository.getAbsences();
            this.dayAbsences = groupByDay(absences);

            this.addTextDisplay("## Abwesenheiten");

            if (dayAbsences.isEmpty()) {
                this.addTextDisplay("Es sind keine Abwesenheiten vorhanden.");
                return;
            }

            for (Map.Entry<LocalDate, List<Absence>> entry : dayAbsences.entrySet()) {
                if (getComponents().size() > MAX_DISPLAYED_COMPONENTS) {
                    this.addTextDisplay("-# Es werden nicht alle Abwesenheiten angezeigt.");
                    break;
                }
                addDaySection(entry.getKey(), entry.getValue());
            }

            addComponent(ActionRow.of(
                    new AbsenceAddBtn().build()
            ));

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

    }

    /**
     * Split every absence into day-absences and group them by their day
     * @param absences The absences to convert - may contain absences spanning multiple days
     * @return The day-absences of each day, sorted by day and by start time within a day
     */
    private static Map<LocalDate, List<Absence>> groupByDay(List<Absence> absences) {
        Map<LocalDate, List<Absence>> groupedAbsences = new TreeMap<>();

        absences.stream()
                .flatMap(absence -> splitIntoDayAbsences(absence).stream())
                .sorted(Comparator.comparing(Absence::fromDateTime))
                .forEach(dayAbsence -> groupedAbsences
                        .computeIfAbsent(dayAbsence.fromDateTime().toLocalDate(), day -> new ArrayList<>())
                        .add(dayAbsence));

        return groupedAbsences;
    }

    /**
     * Convert an absence into one absence per day for multi-day absences
     * <p></p>
     * The original start- and end-time are kept on the first and the last day, every day in between
     * covers the whole day. A single-day absence is returned unchanged
     * @param absence The absence to split
     * @return One day-absence per covered day, empty if the absence ends before it starts
     */
    private static List<Absence> splitIntoDayAbsences(Absence absence) {
        List<Absence> splitAbsences = new ArrayList<>();

        LocalDate firstDay = absence.fromDateTime().toLocalDate();
        LocalDate lastDay = absence.toDateTime().toLocalDate();

        for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
            LocalDateTime fromDateTime = day.equals(firstDay) ? absence.fromDateTime() : day.atStartOfDay();
            LocalDateTime toDateTime = day.equals(lastDay) ? absence.toDateTime() : day.atTime(LocalTime.MAX);

            splitAbsences.add(new Absence(
                    absence.id(),
                    absence.member(),
                    fromDateTime,
                    toDateTime,
                    absence.reason(),
                    absence.absenceMessage()
            ));
        }

        return splitAbsences;
    }

    /**
     * Display all day-absences of a single day
     * @param day The day the absences belong to
     * @param absences The day-absences of that day
     */
    private void addDaySection(LocalDate day, List<Absence> absences) {
        this.addLineSeparator(Separator.Spacing.LARGE);

        String todayTag = day.equals(LocalDate.now(EnvResolver.getZoneId())) ? "*(Heute)*" : "";

        this.addFormatedText("### %d. %s %d %s",
                day.getDayOfMonth(), Util.translateMonth(day.getMonth()), day.getYear(), todayTag);

        absences.forEach(absence -> addAbsenceText(day, absence));
    }

    /**
     * Display a single day-absence
     * @param day The day the absence belongs to
     * @param absence The day-absence to display
     */
    private void addAbsenceText(LocalDate day, Absence absence) {
        String timeRange = "Ganzer Tag";
        if (!isWholeDay(day, absence)) {
            if (absence.toDateTime().toLocalTime() == LocalTime.MAX) {
                timeRange = String.format("Ab <t:%d:t> bis Tagesende",
                        toEpochSeconds(absence.fromDateTime()));
            } else if (absence.fromDateTime().toLocalTime() == LocalTime.of(0, 0)) {
                timeRange = String.format("Tagesanfang bis <t:%d:t>",
                        toEpochSeconds(absence.toDateTime()));
            } else {
                timeRange = String.format("<t:%d:t> - <t:%d:t>",
                        toEpochSeconds(absence.fromDateTime()), toEpochSeconds(absence.toDateTime()));

            }
        }

// prototype / experiment
//        String originalAbsenceTime = "";
//        try {
//            Absence originalAbsence = AbsenceRepository.getAbsenceById(absence.id());
//            originalAbsenceTime = String.format("Abwesend vom <t:%d:f> bis <t:%d:f> (%s)",
//                    toEpochSeconds(originalAbsence.fromDateTime()), toEpochSeconds(originalAbsence.toDateTime()), Util.calcDuration(originalAbsence.fromDateTime(), originalAbsence.toDateTime(), true));
//        } catch (Exception e) {
//
//        }

        AbsenceDetailBtn detailBtn = new AbsenceDetailBtn();
        detailBtn.addArgument("absence_id", absence.id().toString());
        log.info(detailBtn.getId());

        this.addSection(detailBtn.build(), TextDisplay.ofFormat("""
                > **[%s](https://discord.com/users/%s)**
                > -# %s
                > %s
                """,
                absence.member().getEffectiveName(), absence.member().getId(),
                getTeamRoles(absence.member()).stream().map(Role::getAsMention).collect(Collectors.joining(", ")),
                timeRange)
        );
    }

    /**
     * Get the team roles of a member to show what teams the member is part of
     * @param member
     * @return
     */
    private List<Role> getTeamRoles(Member member) {
        return member.getRoles().stream().filter(role -> DISPLAYABLE_ROLES.contains(role)).toList();
    }

    /**
     * Check if a day-absence covers its whole day
     * @param day The day the absence belongs to
     * @param absence The day-absence to check
     * @return True if it starts at the beginning of the day and ends at its end, false if not
     */
    private static boolean isWholeDay(LocalDate day, Absence absence) {
        return !absence.fromDateTime().isAfter(day.atStartOfDay())
                && !absence.toDateTime().isBefore(day.atTime(LocalTime.MAX).withNano(0));
    }

    private static long toEpochSeconds(LocalDateTime dateTime) {
        return dateTime.atZone(EnvResolver.getZoneId()).toEpochSecond();
    }
}
