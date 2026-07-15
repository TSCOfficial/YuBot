package ch.frily.yubot.util;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
public final class LineChart {

    byte[] chartBytes;

    /**
     * Generate linechart for active mod tracking
     * @param title
     * @param xAxisTitle
     * @param yAxisTitle
     * @return
     */
    public LineChart generateLineChart(List<LineChartData> lineChartData, String title, String xAxisTitle, String yAxisTitle) {
        XYChart chart = new XYChartBuilder()
                .width(1200)
                .height(400)
                .title(title)
                .xAxisTitle(xAxisTitle) // active mods
                .yAxisTitle(yAxisTitle) // hours
                .build();

        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setLegendBackgroundColor(new Color("2b2d31").get());
        chart.getStyler().setChartBackgroundColor(new Color("2b2d31").get());
        chart.getStyler().setPlotBackgroundColor(new Color("2b2d31").get());
        chart.getStyler().setChartFontColor(Color.ICON_FOREGROUND);
        chart.getStyler().setAxisTickLabelsColor(Color.ICON_FOREGROUND);
        chart.getStyler().setDatePattern("MMM yy");
        chart.getStyler().setPlotGridLinesColor(new Color("404249").get());
        chart.getStyler().setMarkerSize(0);

        lineChartData.forEach(data -> {
            XYSeries series = chart.addSeries(data.label(), data.xData(), data.yData());
        });

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BitmapEncoder.saveBitmap(chart, outputStream, BitmapEncoder.BitmapFormat.PNG);
            chartBytes = outputStream.toByteArray();
            return this;
        } catch (IOException e) {
            throw new RuntimeException("ActiveMod-Chart konnte nicht generiert werden", e);
        }
    }

    public FileUpload toFile(String fileName) {
        return FileUpload.fromData(chartBytes, fileName);
    }
}