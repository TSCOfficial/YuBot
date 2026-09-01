package ch.frily.yubot.container.absence;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.PaginationContainer;
import ch.frily.yubot.container.PaginationItem;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.absence.Absence;
import ch.frily.yubot.feature.absence.AbsenceRepository;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceAddBtn;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceDetailBtn;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceEditOwnBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AbsenceOverviewContainer extends PaginationContainer {

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
            EnvKey.ROLE_EVENT,
            EnvKey.ROLE_TWITCHMOD
    ).map(EnvResolver::getRoleById).toList();

    /**
     * All absences split up into day-absences, grouped by the day they belong to
     * <p></p>
     * A multi-day absence appears once per day it covers while sharing the same {@link Absence#id()}.
     */
    @Getter
    private Map<LocalDate, List<Absence>> dayAbsences = new TreeMap<>();

    public AbsenceOverviewContainer(ContainerContext context) {
        super(StaticContainerRegistry.ABSENCE_OVERVIEW, context);
        try {
            this.dayAbsences = AbsenceRepository.getAbsencesPerDay();

            PaginationItem header = new PaginationItem();
            header.addChild(TextDisplay.of("## Abwesenheiten"));
            setHeader(header);

            if (dayAbsences.isEmpty()) {
                addItem(TextDisplay.of("Es sind keine Abwesenheiten vorhanden."));
            }

            for (Map.Entry<LocalDate, List<Absence>> entry : dayAbsences.entrySet()) {
                addDaySection(entry.getKey(), entry.getValue());
            }

            PaginationItem footer = new PaginationItem();
            footer.addChild(Separator.createDivider(Separator.Spacing.LARGE));
            footer.addChild(ActionRow.of(
                    new AbsenceAddBtn().build(),
                    new AbsenceEditOwnBtn().build()
            ));
            setFooter(footer);

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Display all day-absences of a single day
     * @param day The day the absences belong to
     * @param absences The day-absences of that day
     */
    private void addDaySection(LocalDate day, List<Absence> absences) {
        PaginationItem daySection = new PaginationItem();

        String todayTag = day.equals(LocalDate.now(EnvResolver.getZoneId())) ? "*(Heute)*" : "";

        daySection.setTitle(String.format("""
                        ### %d. %s %d %s
                        -# *%s Abwesenheit%s insgesamt*
                        """,
                day.getDayOfMonth(), Util.translateMonth(day.getMonth()), day.getYear(), todayTag,
                absences.size(), absences.size() == 1 ? "" : "en"));

        absences.forEach(absence -> addAbsenceText(daySection, day, absence));

        addItem(daySection);
    }

    /**
     * Display a single day-absence
     * @param day The day the absence belongs to
     * @param absence The day-absence to display
     */
    private void addAbsenceText(PaginationItem pageItem, LocalDate day, Absence absence) {
        if (absence == null || absence.member() == null) {
            return;
        }
        String timeRange = "Ganzer Tag";
        if (!isWholeDay(day, absence)) {
            if (absence.toDateTime().toLocalTime() == LocalTime.MAX) {
                timeRange = String.format("Ab <t:%d:t> bis Tagesende",
                        Util.toEpochSeconds(absence.fromDateTime()));
            } else if (absence.fromDateTime().toLocalTime() == LocalTime.of(0, 0)) {
                timeRange = String.format("Tagesanfang bis <t:%d:t>",
                        Util.toEpochSeconds(absence.toDateTime()));
            } else {
                timeRange = String.format("<t:%d:t> - <t:%d:t>",
                        Util.toEpochSeconds(absence.fromDateTime()), Util.toEpochSeconds(absence.toDateTime()));

            }
        }

        AbsenceDetailBtn detailBtn = new AbsenceDetailBtn();
        detailBtn.addArgument("absence_id", absence.id().toString());

        pageItem.addChild(Section.of(detailBtn.build(), TextDisplay.ofFormat("""
                > **%s**
                > -# %s
                > %s %s
                """,
                absence.member().getEffectiveName(),
                getTeamRoles(absence.member()).stream().map(Role::getAsMention).collect(Collectors.joining(", ")),
                absence.type().getEmoji().getFormatted(), timeRange)
        ));
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
}
