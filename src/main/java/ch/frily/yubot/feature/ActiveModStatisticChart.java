package ch.frily.yubot.feature;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import org.knowm.xchart.*;

import ch.frily.yubot.util.Color;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class ActiveModStatisticChart {

    byte[] chartBytes;

    public FileUpload generateChart(Map<Member, List<ActiveModTracking>> activeModTrackingMap) {
        return generateChart(activeModTrackingMap, null);
    }

    public FileUpload generateChart(Map<Member, List<ActiveModTracking>> activeModTrackingMap, @Nullable Member highlightMember) {
        List<ActiveModStatisticChartData> chartData = prepareData(activeModTrackingMap, highlightMember);
        ActiveModStatisticChart chart = generateLineChart(chartData, "Activemod History", "Monat", "Aktive Zeit [in h]");
        return chart.toFile("chart.png");
    }

    private List<ActiveModStatisticChartData> prepareData(Map<Member, List<ActiveModTracking>> activeModTrackingMap, @Nullable Member highlightMember) {
        List<ActiveModStatisticChartData> chartData = new ArrayList<>();
        YearMonth earliest = YearMonth.now().minusMonths(10);
        YearMonth latest = YearMonth.now();

        List<YearMonth> fullMonthRange = Stream.iterate(earliest, month -> !month.isAfter(latest), m -> m.plusMonths(1))
                .toList();

        AtomicBoolean suppress = new AtomicBoolean(false);

        if (highlightMember != null) {
            suppress.set(true);
        }

        activeModTrackingMap.forEach((member, trackings) -> {
            Map<YearMonth, Integer> byMonth = trackings.stream()
                    .collect(Collectors.toMap(ActiveModTracking::month, ActiveModTracking::activeTime));

            DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yy", Locale.GERMAN);
            List<String> months = fullMonthRange.stream()
                    .map(month -> month.format(labelFormat))
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

            if (member.equals(highlightMember)) {
                log.info("Highlighting member {}", highlightMember.getEffectiveName());
                suppress.set(false);
            }
            chartData.add(new ActiveModStatisticChartData(member.getEffectiveName(), suppress.get(), months, activeMinutes));

            if (highlightMember != null) {
                suppress.set(true);
            }
        });
        return chartData;
    }

    /**
     * Generate linechart for active mod tracking
     * @param title
     * @param xAxisTitle
     * @param yAxisTitle
     * @return
     */
    private ActiveModStatisticChart generateLineChart(List<ActiveModStatisticChartData> lineChartData, String title, String xAxisTitle, String yAxisTitle) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(1200)
                .height(400)
                .title(title)
                .xAxisTitle(xAxisTitle) // active mods
                .yAxisTitle(yAxisTitle) // hours
                .build();

        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setLegendBackgroundColor(new Color("242429").get());
        chart.getStyler().setChartBackgroundColor(new Color("242429").get());
        chart.getStyler().setPlotBackgroundColor(new Color("242429").get());
        chart.getStyler().setChartFontColor(Color.LIGHT_GRAY);
        chart.getStyler().setAxisTickLabelsColor(Color.LIGHT_GRAY);
        chart.getStyler().setPlotGridLinesColor(new Color("3b3b41").get());
        chart.getStyler().setMarkerSize(0);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Line);
        chart.getStyler().setOverlapped(true);
        chart.getStyler().setPlotContentSize(1.0);

        if (lineChartData.stream().anyMatch(ActiveModStatisticChartData::suppress)) {
            chart.getStyler().setLegendVisible(false);
        }

        lineChartData.forEach(data -> {
            CategorySeries series = chart.addSeries(data.label(), data.xData(), data.yData());
            if (data.suppress()) {
                series.setLineColor(new java.awt.Color(200, 200, 200, 100));
            }
        });

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BitmapEncoder.saveBitmap(chart, outputStream, BitmapEncoder.BitmapFormat.PNG);
            chartBytes = outputStream.toByteArray();
            return this;
        } catch (IOException e) {
            throw new RuntimeException("ActiveMod-Chart konnte nicht generiert werden", e);
        }
    }

    private FileUpload toFile(String fileName) {
        return FileUpload.fromData(chartBytes, fileName);
    }
}