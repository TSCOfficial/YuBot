package ch.frily.yubot.service.activemod;

import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.service.profile.SettingChoice;
import ch.frily.yubot.service.profile.modal.ProfileSettingActivemodColorModal;
import ch.frily.yubot.util.EnvKey;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import org.knowm.xchart.*;

import ch.frily.yubot.util.Color;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
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
            java.awt.Color color = null;
            try {
                String colorName = SettingRepository.getSetting(member, Setting.ACTIVEMOD_GRAPH_COLOR, String.class);
                if (colorName != null) {
                    log.info("Found saved Color {}", colorName);
                    color = EnvKey.valueOf(colorName).getAsColor();
                }

                // Take a random color that no member is already using
                if (color == null) {
                    Map<Member, String> activeSettings = SettingRepository.getSettings(Setting.ACTIVEMOD_GRAPH_COLOR, String.class);
                    List<String> usedColors = activeSettings.values().stream().toList();
                    List<SelectOption> allColors = Setting.ACTIVEMOD_GRAPH_COLOR.getChoices().stream().map(SettingChoice::toSelectOption).toList();
                    List<SelectOption> restColors = allColors.stream().filter(selectOption -> {
                        return !usedColors.contains(selectOption.getValue());
                    }).toList();

                    String colorKey = restColors.getFirst().getValue();
                    log.info("Setting color to {}", colorKey);
                    color = EnvKey.valueOf(colorKey).getAsColor();

                    // save this color to DB
                    SettingRepository.upsertSetting(member, Setting.ACTIVEMOD_GRAPH_COLOR, colorKey);
                }
            } catch (SQLException e) {
                ExceptionHandler.handle(e);
            }


            chartData.add(new ActiveModStatisticChartData(member.getEffectiveName(), suppress.get(), color, months, activeMinutes));

            if (highlightMember != null) {
                suppress.set(true);
            }
        });
        return chartData;
    }

    /**
     * Generate linechart for active mod tracking
     * @param title Chart title
     * @param xAxisTitle x-axis title
     * @param yAxisTitle y-axis title
     * @return a chart statistic
     */
    private ActiveModStatisticChart generateLineChart(List<ActiveModStatisticChartData> lineChartData,
                                                      String title, String xAxisTitle, String yAxisTitle) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(1200)
                .height(500)                       // etwas höher -> Linien haben mehr Luft
                .title(title)
                .xAxisTitle(xAxisTitle)
                .yAxisTitle(yAxisTitle)
                .build();

        java.awt.Color background = new Color("242429").get();
        java.awt.Color gridColor  = new Color("3b3b41").get();
        java.awt.Color textColor  = new java.awt.Color(0xD0D0D5);

        Font baseFont = new Font("DejaVu Sans", Font.PLAIN, 12);   // hat viele Sonderzeichen

        CategoryStyler styler = chart.getStyler();

        // Hintergrund & Rahmen
        styler.setChartBackgroundColor(background);
        styler.setPlotBackgroundColor(background);
        styler.setPlotBorderVisible(false);
        styler.setChartPadding(15);

        // Schrift
        styler.setChartFontColor(textColor);
        styler.setAxisTickLabelsColor(textColor);
        styler.setChartTitleFont(baseFont.deriveFont(Font.BOLD, 16f));
        styler.setAxisTitleFont(baseFont.deriveFont(Font.PLAIN, 12f));
        styler.setAxisTickLabelsFont(baseFont);
        styler.setChartTitleBoxVisible(false);

        // Gitter: nur horizontal, dezent gestrichelt
        styler.setPlotGridLinesColor(gridColor);
        styler.setPlotGridVerticalLinesVisible(false);
        styler.setPlotGridLinesStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0f, new float[]{4f, 4f}, 0f));
        styler.setAxisTickMarksColor(gridColor);

        // Achsen
        styler.setYAxisMin(0.0);
        styler.setYAxisDecimalPattern("#");

        // Linien
        styler.setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Line);
        styler.setOverlapped(true);   // <- wieder einfügen
        styler.setMarkerSize(6);                       // kleine Punkte zeigen die echten Monatswerte
        styler.setPlotContentSize(1.0);

        // Legende: rechts, ohne Rahmen, mit Linienlänge
        styler.setLegendVisible(lineChartData.stream().noneMatch(ActiveModStatisticChartData::suppress));
        styler.setLegendPosition(Styler.LegendPosition.OutsideE);
        styler.setLegendBackgroundColor(background);
        styler.setLegendBorderColor(background);
        styler.setLegendFont(baseFont);
        styler.setLegendSeriesLineLength(18);
        styler.setLegendPadding(10);

        // Legende nach letztem Wert sortieren (höchster oben) – ändert nur die Reihenfolge
        List<ActiveModStatisticChartData> sorted = lineChartData.stream()
                .sorted(Comparator.comparingDouble(ActiveModStatisticChart::lastValue).reversed())
                .toList();

        sorted.forEach(data -> {
            CategorySeries series = chart.addSeries(data.label(), data.xData(), data.yData());
            series.setLineWidth(2.5f);
            series.setMarker(SeriesMarkers.CIRCLE);

            if (data.suppress()) {
                series.setLineColor(new java.awt.Color(200, 200, 200, 100));
                series.setMarkerColor(new java.awt.Color(200, 200, 200, 100));
            } else {
                series.setLineColor(data.color());
                series.setMarkerColor(data.color());
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

    private static double lastValue(ActiveModStatisticChartData d) {
        List<? extends Number> y = d.yData();
        return y.isEmpty() ? 0 : y.get(y.size() - 1).doubleValue();
    }

    private FileUpload toFile(String fileName) {
        return FileUpload.fromData(chartBytes, fileName);
    }
}