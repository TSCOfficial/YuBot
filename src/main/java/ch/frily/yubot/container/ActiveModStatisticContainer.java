package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveModRepository;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.interaction.select.select.ActiveModTrackingDetailSelect;
import ch.frily.yubot.util.LineChart;
import ch.frily.yubot.util.LineChartData;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.entities.SelectMenuMentions;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ActiveModStatisticContainer extends Container {

    // TODO display all members, even when they have no active mod tracking

    private final Member initiator;

    public ActiveModStatisticContainer(Map<Member, List<ActiveModTracking>> activeModTrackingMap, Member initiator) {
        this.initiator = initiator;

        addTextDisplay("## Statistik der aktiven Moderatoren");

        activeModTrackingMap.forEach(this::addModSection);

        if (activeModTrackingMap.isEmpty()) {
            addTextDisplay("Es sind keine aktiven Moderatoren vorhanden.");
        }

        ActiveModTrackingDetailSelect detailSelect = new ActiveModTrackingDetailSelect();
        detailSelect.setActiveModTrackingMap(activeModTrackingMap);

        this.addComponent(
                ActionRow.of(
                    detailSelect.build()
                )
        );

        this.addComponent(MediaGallery.of(
                MediaGalleryItem.fromFile(generateChart(activeModTrackingMap))
        ));
    }

    public void addModSection(Member member, List<ActiveModTracking> trackings){
        int totalActiveMinutes = trackings.stream().map(ActiveModTracking::activeTime).reduce(0, Integer::sum);
        ActiveModTracking currentMonthTracking = trackings.stream().filter(tracking -> Objects.equals(tracking.month(), YearMonth.now())).findFirst().orElse(null);
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        ActiveModTracking lastMonthTracking = trackings.stream().filter(tracking -> Objects.equals(tracking.month(), lastMonth)).findFirst().orElse(null);

        String thisMonth = "Diesen Monat: *Nicht registriert*";
        if (currentMonthTracking != null) {
            thisMonth = String.format("Diesen Monat: %s", Util.calcDuration(currentMonthTracking.activeTime()));
        }

        String tendency = "";
        if (lastMonthTracking != null) {
            int difference = currentMonthTracking.activeTime() - lastMonthTracking.activeTime();
            if (difference > 0) {
                tendency = "📈";
            } else if (difference < 0){
                tendency = "📉";
            }
        }

        String youTag = member == initiator ? "*(Du)*" : "";

        String activeTag = Util.isActiveMod(member) ? "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>" : "";

        addTextDisplay(String.format("""
                        ### [%s](https://discord.com/users/%s) %s %s
                        Total: %s
                        %s %s
                        -# ** **
                        """,
                member.getEffectiveName(), member.getId(), activeTag, youTag,
                Util.calcDuration(totalActiveMinutes),
                thisMonth, tendency)
        );
    }

    private FileUpload generateChart(Map<Member, List<ActiveModTracking>> activeModTrackingMap) {
        List<LineChartData> chartData = new ArrayList<>();

        YearMonth earliest = YearMonth.now().minusMonths(10);
        YearMonth latest = YearMonth.now();

        List<YearMonth> fullMonthRange = Stream.iterate(earliest, month -> !month.isAfter(latest), m -> m.plusMonths(1))
                .toList();

        activeModTrackingMap.forEach((member, trackings) -> {
            Map<YearMonth, Integer> byMonth = trackings.stream()
                    .collect(Collectors.toMap(ActiveModTracking::month, ActiveModTracking::activeTime));

            DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yy", Locale.GERMAN);
            List<Date> months = fullMonthRange.stream()
                    .map(month -> Date.from(month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .toList();

            // get the active time of each month. If a month does not have any tracking data, activetime is set to 0
            List<Double> activeMinutes = fullMonthRange.stream()
                    .map(month -> {
                        double totalMinutes = (double) byMonth.getOrDefault(month, 0);
                        if (totalMinutes == 0) {
                            return 0.0;
                        } else {
                            return totalMinutes / 60.0;
                        }
                    })
                    .toList();
                    chartData.add(new LineChartData(member.getEffectiveName(), null, months, activeMinutes));
        });



        return new LineChart().generateLineChart(chartData, "Activemod History", "Moderator*in", "Stunden").toFile("chart.png");
    }
}
